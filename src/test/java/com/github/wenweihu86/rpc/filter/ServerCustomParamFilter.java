package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;
import com.google.protobuf.util.JsonFormat;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public class ServerCustomParamFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerCustomParamFilter.class);

    @Override
    public void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                         RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                         FilterChain chain) {
        try {
            RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
            JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
            LOG.info("in ServerCustomParamFilter, requestHeader={}",
                    printer.print(requestHeader));
        } catch (Exception ex) {
            LOG.warn("exception={} in ServerCustomParamFilter", ex.getMessage());
        } finally {
            chain.doFilter(fullRequest, fullResponse);
        }
    }

}
