package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;
import com.github.wenweihu86.rpc.codec.RPCMessage;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
public abstract class AbstractClientFilter implements Filter {

    protected RPCClient rpcClient;

    public void setRPCClient(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public abstract void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                  RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                  FilterChain chain);

}
