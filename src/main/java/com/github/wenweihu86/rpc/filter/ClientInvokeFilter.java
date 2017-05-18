package com.github.wenweihu86.rpc.filter;

import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.client.RPCFuture;
import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.filter.chain.FilterChain;
import com.github.wenweihu86.rpc.codec.RPCMessage;

import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/30.
 */
@SuppressWarnings("unchecked")
public class ClientInvokeFilter extends AbstractClientFilter {

    private RPCClient rpcClient;

    public void setRPCClient(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    public void doFilter(RPCMessage<RPCHeader.RequestHeader> fullRequest,
                         RPCMessage<RPCHeader.ResponseHeader> fullResponse,
                         FilterChain chain) {
        try {
            RPCFuture future = rpcClient.sendRequest(
                    fullRequest, null);

            if (future == null) {
                RPCHeader.ResponseHeader responseHeader = RPCHeader.ResponseHeader.newBuilder()
                        .setLogId(fullRequest.getHeader().getLogId())
                        .setResCode(RPCHeader.ResCode.RES_FAIL)
                        .setResMsg("send request failed").build();
                fullResponse.setHeader(responseHeader);
                return;
            }
            RPCMessage<RPCHeader.ResponseHeader> fullResponse2 = future.get(
                    rpcClient.getRpcClientOptions().getReadTimeoutMillis(),
                    TimeUnit.MILLISECONDS);
            fullResponse.copyFrom(fullResponse2);
        } finally {
            chain.doFilter(fullRequest, fullResponse);
        }
    }

}
