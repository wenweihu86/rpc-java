package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public interface Filter {

    void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                  RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                  FilterChain chain);
}
