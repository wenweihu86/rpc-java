package com.wenweihu86.rpc.filter;

import com.wenweihu86.rpc.client.RPCClient;
import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.filter.chain.FilterChain;

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
