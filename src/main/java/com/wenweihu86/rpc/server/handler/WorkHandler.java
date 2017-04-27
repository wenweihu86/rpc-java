package com.wenweihu86.rpc.server.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.wenweihu86.rpc.codec.ProtoV3Header;
import com.wenweihu86.rpc.codec.ProtoV3Message;
import com.wenweihu86.rpc.server.RPCServer;
import com.wenweihu86.rpc.server.ServiceInfo;
import com.wenweihu86.rpc.server.ServiceManager;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class WorkHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WorkHandler.class);

    private static ThreadPoolExecutor executor;
    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();

    public static void init() {
        executor = new ThreadPoolExecutor(
                RPCServer.getRpcServerOption().getWorkThreadNum(),
                RPCServer.getRpcServerOption().getWorkThreadNum(),
                60L, TimeUnit.SECONDS, blockingQueue);
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static class WorkTask implements Runnable {
        private ProtoV3Message<ProtoV3Header.RequestHeader> request;
        private ChannelHandlerContext ctx;

        public WorkTask(ChannelHandlerContext ctx, ProtoV3Message<ProtoV3Header.RequestHeader> request) {
            this.request = request;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            ProtoV3Header.RequestHeader requestHeader = request.getHeader();
            String serviceName = requestHeader.getServiceName();
            String methodName = requestHeader.getMethodName();
            ServiceManager serviceManager = ServiceManager.getInstance();
            ServiceInfo serviceInfo = serviceManager.getService(serviceName, methodName);
            if (serviceInfo == null) {
                LOG.error("can not find service info, serviceName={}, methodName={}", serviceInfo, methodName);
                throw new RuntimeException("can not find service info");
            }
            Class requestClass = serviceInfo.getRequestClass();
            try {
                Method decodeMethod = requestClass.getMethod("parseFrom", byte[].class);
                GeneratedMessageV3 protoRequest = (GeneratedMessageV3) decodeMethod.invoke(
                        requestClass, request.getBody());
                GeneratedMessageV3 protoResponse =
                        (GeneratedMessageV3) serviceInfo.getMethod().invoke(serviceInfo.getService(), protoRequest);
                Method encodeMethod = protoResponse.getClass().getMethod("toByteArray");
                byte[] responseBody = (byte[]) encodeMethod.invoke(protoResponse);
                ProtoV3Message<ProtoV3Header.ResponseHeader> response = new ProtoV3Message<>();
                ProtoV3Header.ResponseHeader responseHeader = ProtoV3Header.ResponseHeader.newBuilder()
                        .setLogId(requestHeader.getLogId())
                        .setResCode(ProtoV3Header.ResCode.RES_SUCCESS)
                        .setResMsg("").build();
                response.setHeader(responseHeader);
                response.setBody(responseBody);
                ctx.channel().writeAndFlush(response);

                long endTime = System.currentTimeMillis();
                LOG.info("elapse={}ms service={} method={} logId={} request={} response={}",
                        endTime - startTime, serviceName, methodName, requestHeader.getLogId(),
                        protoRequest.toString(), protoResponse.toString());
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }

    }

}
