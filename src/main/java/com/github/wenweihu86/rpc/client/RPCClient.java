package com.github.wenweihu86.rpc.client;

import com.github.wenweihu86.rpc.client.handler.RPCClientHandler;
import com.github.wenweihu86.rpc.client.loadbalance.RandomStrategy;
import com.github.wenweihu86.rpc.protocol.RPCRequestEncoder;
import com.github.wenweihu86.rpc.protocol.RPCResponseDecoder;
import com.github.wenweihu86.rpc.utils.CustomThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
@SuppressWarnings("unchecked")
public class RPCClient {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClient.class);

    private RPCClientOptions rpcClientOptions;
    private Bootstrap bootstrap;
    private ConcurrentMap<Long, RPCFuture> pendingRPC;
    private ScheduledExecutorService timeoutTimer;
    private CopyOnWriteArrayList<EndPoint> endPoints;
    private CopyOnWriteArrayList<RPCChannelGroup> allConnections;

    // first group constructor
    public RPCClient(EndPoint endPoint) {
        this(endPoint, new RPCClientOptions());
    }

    public RPCClient(EndPoint endPoint, RPCClientOptions options) {
        this.init(options);
        List<EndPoint> endPoints = new ArrayList<EndPoint>(1);
        endPoints.add(endPoint);
        this.updateEndPoints(endPoints);
    }

    // second group constructor
    public RPCClient(List<EndPoint> endPoints) {
        this(endPoints, new RPCClientOptions());
    }

    public RPCClient(List<EndPoint> endPoints, RPCClientOptions options) {
        this.init(options);
        this.updateEndPoints(endPoints);
    }

    // third group constructor
    // the right ipPorts format is 10.1.1.1:8888,10.2.2.2:9999
    public RPCClient(String ipPorts) {
        this(ipPorts, new RPCClientOptions());
    }

    public RPCClient(String ipPorts, RPCClientOptions options) {
        List<EndPoint> endPoints = parseEndPoints(ipPorts);
        this.init(options);
        this.updateEndPoints(endPoints);
    }

    public void stop() {
        if (bootstrap.config().group() != null) {
            bootstrap.config().group().shutdownGracefully();
        }
        for (RPCChannelGroup connectionsPerHost : allConnections) {
            connectionsPerHost.close();
        }
        if (timeoutTimer != null) {
            timeoutTimer.shutdown();
        }
    }

    public <T> Future<T> sendRequest(
            final Long callId, Object fullRequest,
            Class<T> responseClass, RPCCallback<T> callback) {
        RPCChannelGroup.ChannelInfo channelInfo = RandomStrategy.instance().selectChannel(allConnections);
        if (channelInfo == null || channelInfo.getChannel() == null || !channelInfo.getChannel().isActive()) {
            return null;
        }
        // add request to RPCFuture and add timeout task
        final long readTimeout = getRPCClientOptions().getReadTimeoutMillis();
        ScheduledFuture scheduledFuture = timeoutTimer.schedule(new Runnable() {
            @Override
            public void run() {
                RPCFuture rpcFuture = removeRPCFuture(callId);
                if (rpcFuture != null) {
                    LOG.debug("request timeout, callId={}", callId);
                    rpcFuture.timeout();
                }
            }
        }, readTimeout, TimeUnit.MILLISECONDS);
        RPCFuture future = new RPCFuture(scheduledFuture, callId, fullRequest,
                responseClass, callback, channelInfo.getChannelGroup());
        addRPCFuture(callId, future);
        channelInfo.getChannel().writeAndFlush(fullRequest);
        return future;
    }

    private void init(RPCClientOptions options) {
        Validate.notNull(options);
        this.rpcClientOptions = options;
        pendingRPC = new ConcurrentHashMap<Long, RPCFuture>();
        timeoutTimer = Executors.newScheduledThreadPool(1,
                new CustomThreadFactory("timeout-timer-thread"));
        this.endPoints = new CopyOnWriteArrayList<EndPoint>();
        this.allConnections = new CopyOnWriteArrayList<RPCChannelGroup>();

        // init netty bootstrap
        bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcClientOptions.getConnectTimeoutMillis());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, rpcClientOptions.isKeepAlive());
        bootstrap.option(ChannelOption.SO_REUSEADDR, rpcClientOptions.isReuseAddr());
        bootstrap.option(ChannelOption.TCP_NODELAY, rpcClientOptions.isTCPNoDelay());
        bootstrap.option(ChannelOption.SO_RCVBUF, rpcClientOptions.getReceiveBufferSize());
        bootstrap.option(ChannelOption.SO_SNDBUF, rpcClientOptions.getSendBufferSize());
        ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RPCRequestEncoder(RPCClient.this));
                ch.pipeline().addLast(new RPCResponseDecoder(RPCClient.this));
                ch.pipeline().addLast(new RPCClientHandler(RPCClient.this));
            }
        };
        bootstrap.group(new NioEventLoopGroup(
                options.getIoThreadNum(),
                new CustomThreadFactory("client-io-thread")))
                .handler(initializer);
    }

    public void updateEndPoints(List<EndPoint> newEndPoints) {
        Collection<EndPoint> addList = CollectionUtils.subtract(newEndPoints, endPoints);
        Collection<EndPoint> deleteList = CollectionUtils.subtract(endPoints, newEndPoints);
        for (EndPoint endPoint : addList) {
            addEndPoint(endPoint);
        }

        for (EndPoint endPoint : deleteList) {
            deleteEndPoint(endPoint);
        }
    }

    private void addEndPoint(EndPoint endPoint) {
        boolean exist = false;
        for (RPCChannelGroup channelGroup : allConnections) {
            if (channelGroup.getIp().equals(endPoint.getIp())
                    && channelGroup.getPort() == endPoint.getPort()) {
                exist = true;
                break;
            }
        }
        if (exist) {
            LOG.warn("ip={}, port={} already exist", endPoint.getIp(), endPoint.getPort());
            return;
        }
        allConnections.add(new RPCChannelGroup(endPoint.getIp(),
                endPoint.getPort(), rpcClientOptions.getMaxConnectionNumPerHost(), bootstrap));
        endPoints.add(endPoint);
    }

    private void deleteEndPoint(EndPoint endPoint) {
        RPCChannelGroup channelGroup = null;
        for (RPCChannelGroup item : allConnections) {
            if (item.getIp().equals(endPoint.getIp()) && item.getPort() == endPoint.getPort()) {
                channelGroup = item;
                break;
            }
        }
        if (channelGroup != null) {
            channelGroup.close();
            allConnections.remove(channelGroup);
        }
        endPoints.remove(endPoint);
    }

    private List<EndPoint> parseEndPoints(String serviceList) {
        if (serviceList == null || serviceList.length() == 0) {
            LOG.error("ipPorts format error, the right format is 10.1.1.1:8888,10.2.2.2:9999");
            throw new IllegalArgumentException("ipPorts format error");
        }

        String[] ipPortSplits = serviceList.split(",");
        List<EndPoint> endPoints = new ArrayList<EndPoint>(ipPortSplits.length);
        for (String ipPort : ipPortSplits) {
            String[] ipPortSplit = ipPort.split(":");
            if (ipPortSplit.length != 2) {
                LOG.error("ipPorts format error, the right format is 10.1.1.1:8888;10.2.2.2:9999");
                throw new IllegalArgumentException("ipPorts format error");
            }
            String ip = ipPortSplit[0];
            int port = Integer.valueOf(ipPortSplit[1]);
            EndPoint endPoint = new EndPoint(ip, port);
            endPoints.add(endPoint);
        }
        return endPoints;
    }

    public void addRPCFuture(Long callId, RPCFuture future) {
        pendingRPC.put(callId, future);
    }

    public RPCFuture getRPCFuture(Long callId) {
        return pendingRPC.get(callId);
    }

    public RPCFuture removeRPCFuture(long logId) {
        return pendingRPC.remove(logId);
    }

    public RPCClientOptions getRPCClientOptions() {
        return rpcClientOptions;
    }

}
