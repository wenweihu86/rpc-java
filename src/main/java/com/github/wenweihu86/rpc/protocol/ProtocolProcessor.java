package com.github.wenweihu86.rpc.protocol;

import com.github.wenweihu86.rpc.client.RPCCallback;
import com.github.wenweihu86.rpc.client.RPCClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by baidu on 2017/9/22.
 */
public interface ProtocolProcessor {

    Object newRequest(Long callId, Method method, Object request, RPCCallback callback);

    boolean decodeRequest(ChannelHandlerContext ctx, ByteBuf in, List<Object> out);

    Object processRequest(Object request);

    void encodeResponse(ChannelHandlerContext ctx, Object object, List<Object> out) throws Exception;

    void encodeRequest(ChannelHandlerContext ctx, Object object, List<Object> out) throws Exception;

    void decodeResponse(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    void processResponse(RPCClient rpcClient, Object object) throws Exception;
}
