package com.github.wenweihu86.rpc.server;

import com.github.wenweihu86.rpc.codec.RPCDecoder;
import com.github.wenweihu86.rpc.codec.RPCEncoder;
import com.github.wenweihu86.rpc.filter.Filter;
import com.github.wenweihu86.rpc.server.handler.RPCServerChannelIdleHandler;
import com.github.wenweihu86.rpc.server.handler.WorkHandler;
import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.server.handler.RPCServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCServer {

    private static final Logger LOG = LoggerFactory.getLogger(RPCServer.class);

    private static RPCServerOptions rpcServerOptions;

    // 端口
    private int port;

    // netty的服务启动对象
    private ServerBootstrap bootstrap;

    // 接受客户端请求的线程
    private EventLoopGroup bossGroup;

    // 处理业务逻辑的线程
    private EventLoopGroup workerGroup;

    private List<Filter> filters;

    public RPCServer(int port) {
        this(port, null, null);
    }

    public RPCServer(int port, final RPCServerOptions options) {
        this(port, options, null);
    }

    public RPCServer(int port, List<Filter> filters) {
        this(port, null, filters);
    }

    public RPCServer(int port, final RPCServerOptions options, List<Filter> filters) {
        this.port = port;
        // use default conf otherwise use specified one
        if (options != null) {
            rpcServerOptions = options;
        } else {
            rpcServerOptions = new RPCServerOptions();
        }
        this.filters = filters;

        bootstrap = new ServerBootstrap();
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(rpcServerOptions.getAcceptorThreadNum());
            workerGroup = new EpollEventLoopGroup(rpcServerOptions.getIOThreadNum());
            ((EpollEventLoopGroup) bossGroup).setIoRatio(100);
            ((EpollEventLoopGroup) workerGroup).setIoRatio(100);
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(rpcServerOptions.getAcceptorThreadNum());
            workerGroup = new NioEventLoopGroup(rpcServerOptions.getIOThreadNum());
            ((NioEventLoopGroup) bossGroup).setIoRatio(100);
            ((NioEventLoopGroup) workerGroup).setIoRatio(100);
            bootstrap.channel(NioServerSocketChannel.class);
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
                ch.pipeline().addLast("decoder", new RPCDecoder(true));
                ch.pipeline().addLast("handler", new RPCServerHandler(RPCServer.this));
                ch.pipeline().addLast("encoder", new RPCEncoder<RPCHeader.ResponseHeader>());
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
            serviceInfo.setServiceName(clazz.getSimpleName());
            serviceInfo.setMethodName(method.getName());
            serviceInfo.setService(service);
            serviceInfo.setMethod(method);
            serviceInfo.setRequestClass(method.getParameterTypes()[0]);
            serviceInfo.setResponseClass(method.getReturnType());
            serviceManager.registerService(serviceInfo);
        }
    }

    public void start() {
        WorkHandler.init();
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
        WorkHandler.getExecutor().shutdown();
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public static RPCServerOptions getRpcServerOptions() {
        return rpcServerOptions;
    }

    public static void setRpcServerOptions(RPCServerOptions rpcServerOptions) {
        RPCServer.rpcServerOptions = rpcServerOptions;
    }

}
