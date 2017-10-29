package com.github.wenweihu86.rpc.server.handler;

import com.github.wenweihu86.rpc.protocol.ProtocolProcessor;
import com.github.wenweihu86.rpc.protocol.standard.StandardProtocol;
import com.github.wenweihu86.rpc.utils.CustomThreadFactory;
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
public class WorkThreadPool {

    private static final Logger LOG = LoggerFactory.getLogger(WorkThreadPool.class);

    private ThreadPoolExecutor executor;
    private BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();

    public WorkThreadPool(int workThreadNum) {
        executor = new ThreadPoolExecutor(
                workThreadNum,
                workThreadNum,
                60L, TimeUnit.SECONDS, blockingQueue,
                new CustomThreadFactory("worker-thread"));
        executor.prestartAllCoreThreads();
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public static class WorkTask implements Runnable {
        private Object fullRequest;
        private ChannelHandlerContext ctx;

        public WorkTask(ChannelHandlerContext ctx,
                        Object fullRequest) {
            this.fullRequest = fullRequest;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            ProtocolProcessor protocol = StandardProtocol.instance();
            Object fullResponse = protocol.processRequest(fullRequest);
            ctx.channel().writeAndFlush(fullResponse);
        }

    }

}
