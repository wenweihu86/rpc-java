package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public class ClientCustomParamFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientCustomParamFilter.class);

    @Override
    public void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                         RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                         FilterChain chain) {
        try {
            RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
            LOG.info("in ClientCustomParamFilter, logId={}, serviceName={}, methodName={}",
                    requestHeader.getLogId(),
                    requestHeader.getServiceName(),
                    requestHeader.getMethodName());
            RPCHeader.RequestHeader newRequestHeader =
                    RPCHeader.RequestHeader.newBuilder().mergeFrom(requestHeader)
                    .putCustomParam("testKey", "testValue").build();
            fullRequest.setHeader(newRequestHeader);
        } finally {
            chain.doFilter(fullRequest, fullResponse);
        }
    }

}
