package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;
import com.github.wenweihu86.rpc.server.ServiceManager;
import com.google.protobuf.GeneratedMessageV3;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import com.github.wenweihu86.rpc.server.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
@SuppressWarnings("unchecked")
public class ServerInvokeFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerInvokeFilter.class);

    @Override
    public void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                         RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                         FilterChain chain) {
        try {
            RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
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
                GeneratedMessageV3 requestBodyMessage = (GeneratedMessageV3) decodeMethod.invoke(
                        null, fullRequest.getBody());
                LOG.debug("serviceName={} methodName={}", serviceName, methodName);
                GeneratedMessageV3 responseBodyMessage =
                        (GeneratedMessageV3) serviceInfo.getMethod().invoke(
                                serviceInfo.getService(), requestBodyMessage);
                Method encodeMethod = responseBodyMessage.getClass().getMethod("toByteArray");
                byte[] responseBody = (byte[]) encodeMethod.invoke(responseBodyMessage);
                RPCHeader.ResponseHeader responseHeader = RPCHeader.ResponseHeader.newBuilder()
                        .setLogId(requestHeader.getLogId())
                        .setResCode(RPCHeader.ResCode.RES_SUCCESS)
                        .setResMsg("").build();
                fullResponse.setHeader(responseHeader);
                fullResponse.setBody(responseBody);
                fullResponse.setBodyMessage(responseBodyMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex.getMessage());
            }
        } finally {
            chain.doFilter(fullRequest, fullResponse);
        }
    }
}
