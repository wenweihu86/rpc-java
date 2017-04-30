package com.wenweihu86.rpc.filter.chain;

import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public interface FilterChain {

    void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                  RPCMessage<RPCHeader.ResponseHeader> fullResponse);
}
