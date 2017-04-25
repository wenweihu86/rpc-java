package com.wenweihu86.rpc.codec.proto3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ProtoV3Encoder extends MessageToMessageEncoder<ProtoV3Response> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtoV3Response response, List<Object> out)
            throws Exception {
        byte[] headerBytes = response.getHeader().toByteArray();
        int totalLength = 4 + 4 + headerBytes.length + response.getBody().length;
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(totalLength);
        byteBuf.writeInt(headerBytes.length);
        byteBuf.writeInt(response.getBody().length);
        byteBuf.writeBytes(headerBytes);
        byteBuf.writeBytes(response.getBody());
        out.add(byteBuf);
    }
}
