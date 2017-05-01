package com.wenweihu86.rpc.filter;

import com.wenweihu86.rpc.client.RPCClient;
import com.wenweihu86.rpc.client.RPCFuture;
import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.filter.chain.FilterChain;

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
                    RPCClient.getRpcClientOption().getReadTimeoutMillis(),
                    TimeUnit.MILLISECONDS);
            fullResponse.copyFrom(fullResponse2);
        } finally {
            chain.doFilter(fullRequest, fullResponse);
        }
    }

}
