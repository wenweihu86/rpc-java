package com.wenweihu86.rpc.server.handler;

import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.filter.chain.FilterChain;
import com.wenweihu86.rpc.filter.chain.ServerFilterChain;
import com.wenweihu86.rpc.server.RPCServer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class WorkHandler {

    private static final Logger LOG = LoggerFactory.getLogger(WorkHandler.class);

    private static ThreadPoolExecutor executor;
    private static BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();

    public static void init() {
        executor = new ThreadPoolExecutor(
                RPCServer.getRpcServerOption().getWorkThreadNum(),
                RPCServer.getRpcServerOption().getWorkThreadNum(),
                60L, TimeUnit.SECONDS, blockingQueue);
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static class WorkTask implements Runnable {
        private RPCMessage<RPCHeader.RequestHeader> fullRequest;
        private ChannelHandlerContext ctx;
        private RPCServer rpcServer;

        public WorkTask(ChannelHandlerContext ctx,
                        RPCMessage<RPCHeader.RequestHeader> fullRequest,
                        RPCServer rpcServer) {
            this.fullRequest = fullRequest;
            this.ctx = ctx;
            this.rpcServer = rpcServer;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            RPCMessage<RPCHeader.ResponseHeader> fullResponse = new RPCMessage<>();
            try {
                FilterChain filterChain = new ServerFilterChain(rpcServer.getFilters());
                filterChain.doFilter(fullRequest, fullResponse);
            } catch (Exception ex) {
                LOG.warn("server run failed, exception={}", ex.getMessage());
                RPCHeader.ResponseHeader responseHeader = RPCHeader.ResponseHeader.newBuilder()
                        .setLogId(fullRequest.getHeader().getLogId())
                        .setResCode(RPCHeader.ResCode.RES_FAIL)
                        .setResMsg(ex.getMessage()).build();
                fullResponse.setHeader(responseHeader);
            }
            ctx.channel().writeAndFlush(fullResponse);

            long endTime = System.currentTimeMillis();
            try {
                RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
                LOG.info("elapseMS={} service={} method={} logId={}",
                        endTime - startTime, requestHeader.getServiceName(),
                        requestHeader.getMethodName(), requestHeader.getLogId());
            } catch (Exception ex) {
                LOG.warn("log exception={}", ex.getMessage());
            }
        }

    }

}
