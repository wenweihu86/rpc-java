package com.github.wenweihu86.rpc.protocol;

import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.protocol.standard.StandardProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCRequestEncoder extends MessageToMessageEncoder<Object> {

    private RPCClient rpcClient;

    public RPCRequestEncoder(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object object, List<Object> out)
            throws Exception {
        ProtocolProcessor protocol = StandardProtocol.instance();
        protocol.encodeRequest(ctx, object, out);
    }
}
