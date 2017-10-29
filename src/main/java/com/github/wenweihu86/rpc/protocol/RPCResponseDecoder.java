package com.github.wenweihu86.rpc.protocol;

import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.protocol.standard.StandardProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RPCResponseDecoder extends ByteToMessageDecoder {

    private RPCClient rpcClient;

    public RPCResponseDecoder(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ProtocolProcessor protocol = StandardProtocol.instance();
        protocol.decodeResponse(ctx, in, out);
    }
}
