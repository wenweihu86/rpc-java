package com.wenweihu86.rpc.codec.proto3;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ProtoV3Decoder extends ByteToMessageDecoder {

    public static final int FIXED_LEN = 8;

    private boolean isRequest;

    public ProtoV3Decoder(boolean isRequest) {
        this.isRequest = isRequest;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 解决半包问题，此时头部8字节长度还没有接收全，channel中留存的字节流不做处理
        if (in.readableBytes() < FIXED_LEN) {
            return;
        }
        in.markReaderIndex();
        int headerLen = in.readInt();
        int bodyLen = in.readInt();
        // 解决半包问题，此时header和body还没有接收全，channel中留存的字节流不做处理，重置readerIndex
        if (in.readableBytes() < headerLen + bodyLen) {
            in.resetReaderIndex();
            return;
        }
        in.markReaderIndex();
        byte[] headBytes = new byte[headerLen];
        in.readBytes(headBytes, 0, headerLen);
        byte[] bodyBytes = new byte[bodyLen];
        in.readBytes(bodyBytes, 0, bodyLen);

        if (isRequest) {
            ProtoV3Message<ProtoV3Header.RequestHeader> object = new ProtoV3Message<>();
            ProtoV3Header.RequestHeader header = ProtoV3Header.RequestHeader.parseFrom(headBytes);
            object.setHeader(header);
            object.setBody(bodyBytes);
            out.add(object);
        } else  {
            ProtoV3Message<ProtoV3Header.ResponseHeader> object = new ProtoV3Message<>();
            ProtoV3Header.ResponseHeader header = ProtoV3Header.ResponseHeader.parseFrom(headBytes);
            object.setHeader(header);
            object.setBody(bodyBytes);
            out.add(object);
        }
    }

}
