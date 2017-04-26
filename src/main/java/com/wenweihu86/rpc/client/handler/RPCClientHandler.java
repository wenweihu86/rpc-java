package com.wenweihu86.rpc.client.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.wenweihu86.rpc.client.RPCClient;
import com.wenweihu86.rpc.client.RPCFuture;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Header;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class RPCClientHandler extends SimpleChannelInboundHandler<ProtoV3Message<ProtoV3Header.ResponseHeader>> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             ProtoV3Message<ProtoV3Header.ResponseHeader> response) throws Exception {
        String logId = response.getHeader().getLogId();
        RPCFuture future = RPCClient.getRPCFuture(logId);
        if (future == null) {
            LOG.warn("Receive msg from server but no request found, logId={}", logId);
            return;
        }
        RPCClient.removeRPCFuture(logId);
        Method decodeMethod = future.getResponseClass().getMethod("parseFrom", byte[].class);
        GeneratedMessageV3 responseBody = (GeneratedMessageV3) decodeMethod.invoke(
                future.getResponseClass(), response.getBody());
        future.success(responseBody);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.close();
    }

}
