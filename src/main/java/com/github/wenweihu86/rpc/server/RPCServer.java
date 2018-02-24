package com.github.wenweihu86.rpc.server;

import com.github.wenweihu86.rpc.protocol.RPCMeta;
import com.github.wenweihu86.rpc.protocol.RPCReponseEncoder;
import com.github.wenweihu86.rpc.protocol.RPCRequestDecoder;
import com.github.wenweihu86.rpc.server.handler.RPCServerChannelIdleHandler;
import com.github.wenweihu86.rpc.server.handler.RPCServerHandler;
import com.github.wenweihu86.rpc.server.handler.WorkThreadPool;
import com.github.wenweihu86.rpc.utils.CustomThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCServer {

    private static final Logger LOG = LoggerFactory.getLogger(RPCServer.class);

    private RPCServerOptions rpcServerOptions;

    // 端口
    private int port;

    // netty bootstrap
    private ServerBootstrap bootstrap;

    // netty acceptor thread pool
    private EventLoopGroup bossGroup;

    // netty io thread pool
    private EventLoopGroup workerGroup;

    // business handler thread pool
    private WorkThreadPool workThreadPool;

    public RPCServer(int port) {
        this(port, new RPCServerOptions());
    }

    public RPCServer(int port, final RPCServerOptions options) {
        this.port = port;
        this.rpcServerOptions = options;
        this.workThreadPool = new WorkThreadPool(options.getWorkThreadNum());

        bootstrap = new ServerBootstrap();
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(
                    rpcServerOptions.getAcceptorThreadNum(),
                    new CustomThreadFactory("server-acceptor-thread"));
            workerGroup = new EpollEventLoopGroup(
                    rpcServerOptions.getIoThreadNum(),
                    new CustomThreadFactory("server-io-thread"));
            ((EpollEventLoopGroup) bossGroup).setIoRatio(100);
            ((EpollEventLoopGroup) workerGroup).setIoRatio(100);
            bootstrap.channel(EpollServerSocketChannel.class);
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            LOG.info("use epoll edge trigger mode");
        } else {
            bossGroup = new NioEventLoopGroup(
                    rpcServerOptions.getAcceptorThreadNum(),
                    new CustomThreadFactory("server-acceptor-thread"));
            workerGroup = new NioEventLoopGroup(
                    rpcServerOptions.getIoThreadNum(),
                    new CustomThreadFactory("server-io-thread"));
            ((NioEventLoopGroup) bossGroup).setIoRatio(100);
            ((NioEventLoopGroup) workerGroup).setIoRatio(100);
            bootstrap.channel(NioServerSocketChannel.class);
            LOG.info("use normal mode");
        }

        bootstrap.option(ChannelOption.SO_BACKLOG, rpcServerOptions.getBacklog());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, rpcServerOptions.isKeepAlive());
        bootstrap.childOption(ChannelOption.TCP_NODELAY, rpcServerOptions.isTCPNoDelay());
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_LINGER, rpcServerOptions.getSoLinger());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, rpcServerOptions.getSendBufferSize());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, rpcServerOptions.getReceiveBufferSize());

        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                        "idleStateAwareHandler", new IdleStateHandler(
                                rpcServerOptions.getReaderIdleTime(),
                                rpcServerOptions.getWriterIdleTime(),
                                rpcServerOptions.getKeepAliveTime()));
                ch.pipeline().addLast("idle", new RPCServerChannelIdleHandler());
                ch.pipeline().addLast("decoder", new RPCRequestDecoder());
                ch.pipeline().addLast("handler", new RPCServerHandler(RPCServer.this));
                ch.pipeline().addLast("encoder", new RPCReponseEncoder());
            }
        };
        bootstrap.group(bossGroup, workerGroup).childHandler(initializer);
    }

    public void registerService(Object service) {
        Class[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length != 1) {
            LOG.error("service must implement one interface only");
            throw new RuntimeException("service must implement one interface only");
        }
        Class clazz = interfaces[0];
        Method[] methods = clazz.getDeclaredMethods();
        ServiceManager serviceManager = ServiceManager.getInstance();
        for (Method method : methods) {
            ServiceInfo serviceInfo = new ServiceInfo();

            String serviceName;
            RPCMeta rpcMeta = method.getAnnotation(RPCMeta.class);
            if (rpcMeta != null && StringUtils.isNotBlank(rpcMeta.serviceName())) {
                serviceName = rpcMeta.serviceName();
            } else {
                serviceName = method.getDeclaringClass().getName();
            }
            String methodName;
            if (rpcMeta != null && StringUtils.isNotBlank(rpcMeta.methodName())) {
                methodName = rpcMeta.methodName();
            } else {
                methodName = method.getName();
            }

            serviceInfo.setServiceName(serviceName);
            serviceInfo.setMethodName(methodName);
            serviceInfo.setService(service);
            serviceInfo.setMethod(method);
            serviceInfo.setRequestClass(method.getParameterTypes()[0]);
            serviceInfo.setResponseClass(method.getReturnType());
            try {
                Method parseFromMethod = serviceInfo.getRequestClass().getMethod("parseFrom", byte[].class);
                serviceInfo.setParseFromForRequest(parseFromMethod);
            } catch (Exception ex) {
                throw new RuntimeException("getMethod failed, register failed");
            }
            serviceManager.registerService(serviceInfo);
            LOG.info("register service, serviceName={}, methodName={}",
                    serviceInfo.getServiceName(), serviceInfo.getMethodName());
        }
    }

    public void start() {
        try {
            bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            LOG.error("server failed to start, {}", e.getMessage());
        }
        LOG.info("server started on port={} success", port);
    }

    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        workThreadPool.getExecutor().shutdown();
    }

    public RPCServerOptions getRpcServerOptions() {
        return rpcServerOptions;
    }

    public void setRpcServerOptions(RPCServerOptions rpcServerOptions) {
        this.rpcServerOptions = rpcServerOptions;
    }

    public WorkThreadPool getWorkThreadPool() {
        return workThreadPool;
    }
}
