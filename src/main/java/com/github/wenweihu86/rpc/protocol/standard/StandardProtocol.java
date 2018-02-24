package com.github.wenweihu86.rpc.protocol.standard;

import com.github.wenweihu86.rpc.client.RPCCallback;
import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.client.RPCFuture;
import com.github.wenweihu86.rpc.protocol.ProtocolProcessor;
import com.github.wenweihu86.rpc.protocol.RPCMeta;
import com.github.wenweihu86.rpc.server.ServiceInfo;
import com.github.wenweihu86.rpc.server.ServiceManager;
import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

public class StandardProtocol<T> implements ProtocolProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardProtocol.class);

    private static final int FIXED_LEN = 8;

    private static StandardProtocol instance = new StandardProtocol();

    public static ProtocolProcessor instance() {
        return instance;
    }

    @Override
    public Object newRequest(Long callId, Method method, Object request, RPCCallback callback) {
        RPCMessage<RPCHeader.RequestHeader> fullRequest = new RPCMessage<RPCHeader.RequestHeader>();
        RPCHeader.RequestHeader.Builder headerBuilder = RPCHeader.RequestHeader.newBuilder();
        headerBuilder.setCallId(callId);

        String serviceName;
        RPCMeta rpcMeta = method.getAnnotation(RPCMeta.class);
        if (rpcMeta != null && StringUtils.isNotBlank(rpcMeta.serviceName())) {
            serviceName = rpcMeta.serviceName();
        } else {
            serviceName = method.getDeclaringClass().getName();
        }
        String methodName;
        if (rpcMeta != null && StringUtils.isNotBlank(rpcMeta.methodName())) {
            methodName = rpcMeta.methodName();
        } else {
            methodName = method.getName();
        }
        LOG.debug("serviceName={}, methodName={}", serviceName, methodName);

        headerBuilder.setServiceName(serviceName);
        headerBuilder.setMethodName(methodName);
        fullRequest.setHeader(headerBuilder.build());

        if (!MessageLite.class.isAssignableFrom(request.getClass())) {
            LOG.error("request must be protobuf message");
            return null;
        }
        try {
            Method encodeMethod = request.getClass().getMethod("toByteArray");
            byte[] bodyBytes = (byte[]) encodeMethod.invoke(request);
            fullRequest.setBody(bodyBytes);
        } catch (Exception ex) {
            LOG.error("request object has no method toByteArray");
            return null;
        }

        return fullRequest;
    }

    @Override
    public boolean decodeRequest(ChannelHandlerContext ctx,
                             ByteBuf in, List<Object> out) {
        if (decode(true, ctx, in, out)) {
            return true;
        } else {
            in.resetReaderIndex();
            return false;
        }
    }

    @Override
    public Object processRequest(Object request) {
        long startTime = System.currentTimeMillis();
        RPCMessage<RPCHeader.RequestHeader> fullRequest = (RPCMessage<RPCHeader.RequestHeader>) request;
        RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
        // default response
        RPCHeader.ResponseHeader.Builder responseHeader = RPCHeader.ResponseHeader.newBuilder()
                .setCallId(fullRequest.getHeader().getCallId())
                .setResCode(RPCHeader.ResCode.RES_FAIL);
        RPCMessage<RPCHeader.ResponseHeader> fullResponse = new RPCMessage<RPCHeader.ResponseHeader>();

        String serviceName = requestHeader.getServiceName();
        String methodName = requestHeader.getMethodName();
        ServiceManager serviceManager = ServiceManager.getInstance();
        ServiceInfo serviceInfo = serviceManager.getService(serviceName, methodName);
        if (serviceInfo == null) {
            LOG.error("can not find service info, serviceName={}, methodName={}", serviceName, methodName);
            responseHeader.setResCode(RPCHeader.ResCode.RES_FAIL);
            responseHeader.setResMsg("can not find service info");
            fullResponse.setHeader(responseHeader.build());
            return fullResponse;
        }
        try {
            Method parseMethod = serviceInfo.getParseFromForRequest();
            MessageLite requestMessage = (MessageLite) parseMethod.invoke(null, fullRequest.getBody());
            MessageLite responseMessage =
                    (MessageLite) serviceInfo.getMethod().invoke(
                            serviceInfo.getService(), requestMessage);
            byte[] responseBody = responseMessage.toByteArray();

            responseHeader.setResCode(RPCHeader.ResCode.RES_SUCCESS).setResMsg("");
            fullResponse.setHeader(responseHeader.build());
            fullResponse.setBody(responseBody);
            long endTime = System.currentTimeMillis();
            LOG.debug("elapseMS={} service={} method={} callId={}",
                    endTime - startTime, requestHeader.getServiceName(),
                    requestHeader.getMethodName(), requestHeader.getCallId());
            return fullResponse;
        } catch (Exception ex) {
            LOG.error("invoke method failed, msg={}", ex.getMessage());
            responseHeader.setResCode(RPCHeader.ResCode.RES_FAIL);
            responseHeader.setResMsg("invoke method failed");
            fullResponse.setHeader(responseHeader.build());
            return fullResponse;
        }
    }

    @Override
    public void encodeResponse(ChannelHandlerContext ctx, Object object, List<Object> out) throws Exception {
        RPCMessage<RPCHeader.ResponseHeader> response = (RPCMessage<RPCHeader.ResponseHeader>) object;
        byte[] headerBytes = response.getHeader().toByteArray();

        // length buffer
        ByteBuf lengthBuf = Unpooled.buffer(8);
        lengthBuf.writeInt(headerBytes.length);
        lengthBuf.writeInt(response.getBody().length);

        ByteBuf outBuf = Unpooled.wrappedBuffer(lengthBuf.array(), headerBytes, response.getBody());
        out.add(outBuf);
    }

    @Override
    public void encodeRequest(ChannelHandlerContext ctx, Object object, List<Object> out) throws Exception {
        RPCMessage<RPCHeader.RequestHeader> request = (RPCMessage<RPCHeader.RequestHeader>) object;
        byte[] headerBytes = request.getHeader().toByteArray();

        // length buffer
        ByteBuf lengthBuf = Unpooled.buffer(8);
        lengthBuf.writeInt(headerBytes.length);
        lengthBuf.writeInt(request.getBody().length);

        ByteBuf outBuf = Unpooled.wrappedBuffer(lengthBuf.array(), headerBytes, request.getBody());
        out.add(outBuf);
    }

    @Override
    public void decodeResponse(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decode(false, ctx, in, out);
    }

    @Override
    public void processResponse(RPCClient rpcClient, Object object) throws Exception {
        RPCMessage<RPCHeader.ResponseHeader> fullResponse = (RPCMessage<RPCHeader.ResponseHeader>) object;
        Long callId = fullResponse.getHeader().getCallId();
        RPCFuture future = rpcClient.getRPCFuture(callId);
        if (future == null) {
            return;
        }
        rpcClient.removeRPCFuture(callId);

        if (fullResponse.getHeader().getResCode() == RPCHeader.ResCode.RES_SUCCESS) {
            Method decodeMethod = future.getResponseClass().getMethod("parseFrom", byte[].class);
            MessageLite responseBody = (MessageLite) decodeMethod.invoke(
                    null, fullResponse.getBody());
            future.success(responseBody);
        } else {
            future.fail(new RuntimeException(fullResponse.getHeader().getResMsg()));
        }
    }

    private boolean decode(boolean isRequest, ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) {
        // 解决半包问题，此时头部8字节长度还没有接收全，channel中留存的字节流不做处理
        if (in.readableBytes() < FIXED_LEN) {
            return false;
        }
        in.markReaderIndex();
        int headerLen = in.readInt();
        int bodyLen = in.readInt();
        // 解决半包问题，此时header和body还没有接收全，channel中留存的字节流不做处理，重置readerIndex
        if (in.readableBytes() < headerLen + bodyLen) {
            in.resetReaderIndex();
            return false;
        }
        in.markReaderIndex();
        byte[] headBytes = new byte[headerLen];
        in.readBytes(headBytes, 0, headerLen);
        byte[] bodyBytes = new byte[bodyLen];
        in.readBytes(bodyBytes, 0, bodyLen);

        try {
            if (isRequest) {
                RPCMessage<RPCHeader.RequestHeader> object = new RPCMessage<RPCHeader.RequestHeader>();
                RPCHeader.RequestHeader header = RPCHeader.RequestHeader.parseFrom(headBytes);
                object.setHeader(header);
                object.setBody(bodyBytes);
                out.add(object);
                return true;
            } else {
                RPCMessage<RPCHeader.ResponseHeader> object = new RPCMessage<RPCHeader.ResponseHeader>();
                RPCHeader.ResponseHeader header = RPCHeader.ResponseHeader.parseFrom(headBytes);
                object.setHeader(header);
                object.setBody(bodyBytes);
                out.add(object);
                return true;
            }
        } catch (Exception ex) {
            LOG.debug("decode failed, ex={}", ex.getMessage());
            return false;
        }
    }

}
