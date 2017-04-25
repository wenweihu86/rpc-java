package com.wenweihu86.rpc.client;

import com.wenweihu86.rpc.client.handler.RPCClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wenweihu86.rpc.codec.proto3.ProtoV3Decoder;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Encoder;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Request;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCClient {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClient.class);

    private static RPCClientOption rpcClientOption;
    private static Bootstrap bootstrap;
    private static AtomicBoolean isInit = new AtomicBoolean(false);

    private String host;
    private int port;

    private Channel channel;
    private static Map<String, RPCFuture> pendingRPC;

    public RPCClient(String host, int port) {
        this(host, port, null);
    }

    public RPCClient(String host, int port, RPCClientOption option) {
        if (isInit.compareAndSet(false, true)) {
            if (option != null) {
                this.rpcClientOption = option;
            } else {
                this.rpcClientOption = new RPCClientOption();
            }
            pendingRPC = new ConcurrentHashMap<>();

            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.rpcClientOption.getConnectTimeoutMillis());
            bootstrap.option(ChannelOption.SO_KEEPALIVE, this.rpcClientOption.isKeepAlive());
            bootstrap.option(ChannelOption.SO_REUSEADDR, this.rpcClientOption.isReuseAddr());
            bootstrap.option(ChannelOption.TCP_NODELAY, this.rpcClientOption.isTCPNoDelay());
            bootstrap.option(ChannelOption.SO_RCVBUF, this.rpcClientOption.getReceiveBufferSize());
            bootstrap.option(ChannelOption.SO_SNDBUF, this.rpcClientOption.getSendBufferSize());

            ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProtoV3Encoder());
                    ch.pipeline().addLast(new ProtoV3Decoder());
                    ch.pipeline().addLast(new RPCClientHandler());
                }
            };
            bootstrap.group(new NioEventLoopGroup()).handler(initializer);
        }

        this.host = host;
        this.port = port;

    }

    public ChannelFuture connect() {
        try {
            final ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        LOG.info("Connection {} is established", channelFuture.channel());
                        channel = future.channel();
                    } else {
                        LOG.warn(String.format("Connection get failed on {} due to {}",
                                channelFuture.cause().getMessage(), channelFuture.cause()));
                    }
                }
            });
            return future;
        } catch (Exception e) {
            LOG.error("Failed to connect to {}:{} due to {}", host, port, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void addRPCFuture(String logId, RPCFuture future) {
        pendingRPC.put(logId, future);
    }

    public static RPCFuture getRPCFuture(String logId) {
        return pendingRPC.get(logId);
    }

    public static void removeRPCFuture(String logId) {
        pendingRPC.remove(logId);
    }

    public void sendRequest(ProtoV3Request fullRequest) {
        if (this.channel == null || !this.channel.isActive()) {
            try {
                ChannelFuture channelFuture = connect().sync();
                this.channel = channelFuture.channel();
            } catch (Exception ex) {
                LOG.error("connect to {}:{} failed", this.host, this.port);
                throw new RuntimeException("connect failed");
            }
        }
        this.channel.writeAndFlush(fullRequest);
    }

    public static RPCClientOption getRpcClientOption() {
        return rpcClientOption;
    }

    public static void setRpcClientOption(RPCClientOption rpcClientOption) {
        RPCClient.rpcClientOption = rpcClientOption;
    }
}
