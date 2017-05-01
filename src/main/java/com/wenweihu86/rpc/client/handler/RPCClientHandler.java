package com.wenweihu86.rpc.client.handler;

import com.google.protobuf.GeneratedMessageV3;
import com.wenweihu86.rpc.client.RPCClient;
import com.wenweihu86.rpc.client.RPCFuture;
import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class RPCClientHandler extends SimpleChannelInboundHandler<RPCMessage<RPCHeader.ResponseHeader>> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClientHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             RPCMessage<RPCHeader.ResponseHeader> fullResponse) throws Exception {
        String logId = fullResponse.getHeader().getLogId();
        RPCFuture future = RPCClient.getRPCFuture(logId);
        if (future == null) {
            LOG.debug("receive msg from server but no request found, logId={}", logId);
            return;
        }
        RPCClient.removeRPCFuture(logId);

        if (fullResponse.getHeader().getResCode() == RPCHeader.ResCode.RES_SUCCESS) {
            Method decodeMethod = future.getResponseClass().getMethod("parseFrom", byte[].class);
            GeneratedMessageV3 responseBody = (GeneratedMessageV3) decodeMethod.invoke(
                    future.getResponseClass(), fullResponse.getBody());
            fullResponse.setBodyMessage(responseBody);
            future.success(fullResponse);
        } else {
            future.fail(new RuntimeException(fullResponse.getHeader().getResMsg()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.close();
    }

}
