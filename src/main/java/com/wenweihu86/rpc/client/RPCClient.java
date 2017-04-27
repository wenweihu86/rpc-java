package com.wenweihu86.rpc.client;

import com.google.protobuf.GeneratedMessageV3;
import com.wenweihu86.rpc.client.handler.RPCClientHandler;
import com.wenweihu86.rpc.codec.ProtoV3Header;
import com.wenweihu86.rpc.codec.ProtoV3Message;
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

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wenweihu86.rpc.codec.ProtoV3Decoder;
import com.wenweihu86.rpc.codec.ProtoV3Encoder;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCClient {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClient.class);

    private static AtomicBoolean isInit = new AtomicBoolean(false);
    private static RPCClientOption rpcClientOption;
    private static Bootstrap bootstrap;
    private static Map<String, RPCFuture> pendingRPC;
    private static ScheduledExecutorService scheduledExecutor;

    private String host;
    private int port;
    private Channel channel;

    public RPCClient(String host, int port) {
        this(host, port, null);
    }

    public RPCClient(String host, int port, RPCClientOption option) {
        if (isInit.compareAndSet(false, true)) {
            pendingRPC = new ConcurrentHashMap<>();
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

            if (option != null) {
                this.rpcClientOption = option;
            } else {
                this.rpcClientOption = new RPCClientOption();
            }

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
                    ch.pipeline().addLast(new ProtoV3Encoder<ProtoV3Header.RequestHeader>());
                    ch.pipeline().addLast(new ProtoV3Decoder(false));
                    ch.pipeline().addLast(new RPCClientHandler());
                }
            };
            bootstrap.group(new NioEventLoopGroup()).handler(initializer);
        }

        this.host = host;
        this.port = port;
        connect();
    }

    public ChannelFuture connect() {
        try {
            final ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
            future.awaitUninterruptibly();
            this.channel = future.channel();
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        LOG.info("Connection {} is established", channelFuture.channel());
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

    public void asyncCall(String serviceMethodName,
                          Object request,
                          RPCCallback callback) {
        String[] splitArray = serviceMethodName.split("\\.");
        if (splitArray.length != 2) {
            LOG.error("serviceMethodName={} is not valid", serviceMethodName);
            return;
        }
        String serviceName = splitArray[0];
        String methodName = splitArray[1];
        final String logId = UUID.randomUUID().toString();
        this.sendRequest(logId, serviceName, methodName, request, null, callback);
    }

    public <T> RPCFuture sendRequest(final String logId,
                                     final String serviceName,
                                     final String methodName,
                                     Object request,
                                     Class responseClass,
                                     RPCCallback<T> callback) {
        ProtoV3Message<ProtoV3Header.RequestHeader> fullRequest = new ProtoV3Message<>();

        ProtoV3Header.RequestHeader.Builder headerBuilder = ProtoV3Header.RequestHeader.newBuilder();
        headerBuilder.setLogId(logId);
        headerBuilder.setServiceName(serviceName);
        headerBuilder.setMethodName(methodName);
        fullRequest.setHeader(headerBuilder.build());

        if (!GeneratedMessageV3.class.isAssignableFrom(request.getClass())) {
            LOG.error("request must be protobuf message");
            return null;
        }
        try {
            Method encodeMethod = request.getClass().getMethod("toByteArray");
            byte[] bodyBytes = (byte[]) encodeMethod.invoke(request);
            fullRequest.setBody(bodyBytes);
        } catch (Exception ex) {
            LOG.error("request object has no method toByteArray");
            return null;
        }

        final ScheduledExecutorService scheduledExecutor = RPCClient.getScheduledExecutor();
        final long readWriteTimeout = RPCClient.getRpcClientOption().getReadTimeoutMillis()
                + RPCClient.getRpcClientOption().getWriteTimeoutMillis();
        ScheduledFuture scheduledFuture = scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                RPCFuture rpcFuture = RPCClient.removeRPCFuture(logId);
                if (rpcFuture != null) {
                    LOG.warn("request timeout, logId={}, service={}, method={}",
                            logId, serviceName, methodName);
                    rpcFuture.timeout();
                } else {
                    LOG.warn("request logId={} not found", logId);
                }
            }
        }, readWriteTimeout, TimeUnit.MILLISECONDS);

        RPCFuture future = new RPCFuture(scheduledFuture, responseClass, callback);
        RPCClient.addRPCFuture(logId, future);
        try {
            this.doSend(fullRequest);
        } catch (RuntimeException ex) {
            RPCClient.removeRPCFuture(logId);
            return null;
        }
        return future;
    }

    public void doSend(ProtoV3Message<ProtoV3Header.RequestHeader> fullRequest) {
        if (this.channel == null || !this.channel.isActive()) {
            try {
                connect();
            } catch (Exception ex) {
                LOG.error("connect to {}:{} failed", this.host, this.port);
                throw new RuntimeException("connect failed");
            }
        }
        LOG.debug("channel isActive={}", this.channel.isActive());
        if (this.channel.isActive()) {
            this.channel.writeAndFlush(fullRequest);
        } else {
            LOG.error("connect to {}:{} failed", this.host, this.port);
            throw new RuntimeException("connect failed");
        }
    }

    public static void addRPCFuture(String logId, RPCFuture future) {
        pendingRPC.put(logId, future);
    }

    public static RPCFuture getRPCFuture(String logId) {
        return pendingRPC.get(logId);
    }

    public static RPCFuture removeRPCFuture(String logId) {
        return pendingRPC.remove(logId);
    }

    public static ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public static RPCClientOption getRpcClientOption() {
        return rpcClientOption;
    }

    public static void setRpcClientOption(RPCClientOption rpcClientOption) {
        RPCClient.rpcClientOption = rpcClientOption;
    }
}
