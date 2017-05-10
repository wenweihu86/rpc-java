package com.github.wenweihu86.rpc.codec;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCEncoder<T extends GeneratedMessageV3> extends MessageToMessageEncoder<RPCMessage<T>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RPCMessage<T> object, List<Object> out)
            throws Exception {
        byte[] headerBytes = object.getHeader().toByteArray();
        int totalLength = 4 + 4 + headerBytes.length + object.getBody().length;
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(totalLength);
        byteBuf.writeInt(headerBytes.length);
        byteBuf.writeInt(object.getBody().length);
        byteBuf.writeBytes(headerBytes);
        byteBuf.writeBytes(object.getBody());
        out.add(byteBuf);
    }
}
