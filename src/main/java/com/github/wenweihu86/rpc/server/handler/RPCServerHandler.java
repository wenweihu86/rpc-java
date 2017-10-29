package com.github.wenweihu86.rpc.server.handler;

import com.github.wenweihu86.rpc.server.RPCServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCServerHandler.class);
    private RPCServer rpcServer;

    public RPCServerHandler(RPCServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             Object request) throws Exception {
        WorkThreadPool.WorkTask task = new WorkThreadPool.WorkTask(ctx, request);
        rpcServer.getWorkThreadPool().getExecutor().submit(task);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.debug("meet exception, may be client closed the connection, msg={}", cause.getMessage());
        ctx.close();
    }
}
