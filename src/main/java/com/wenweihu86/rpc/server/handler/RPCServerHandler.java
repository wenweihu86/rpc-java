package com.wenweihu86.rpc.server.handler;

import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.server.RPCServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCServerHandler extends SimpleChannelInboundHandler<RPCMessage<RPCHeader.RequestHeader>> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCServerHandler.class);

    private RPCServer rpcServer;

    public RPCServerHandler(RPCServer rpcServer) {
        this.rpcServer = rpcServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,
                             RPCMessage<RPCHeader.RequestHeader> request) throws Exception {
        WorkHandler.WorkTask task = new WorkHandler.WorkTask(ctx, request, rpcServer);
        WorkHandler.getExecutor().submit(task);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        RPCMessage<RPCHeader.ResponseHeader> response = new RPCMessage<>();
        RPCHeader.ResponseHeader header = RPCHeader.ResponseHeader.newBuilder()
                .setResCode(RPCHeader.ResCode.RES_FAIL).setResMsg(cause.getMessage()).build();
        response.setHeader(header);
        response.setBody(new byte[]{});
        ctx.fireChannelRead(response);
    }
}
