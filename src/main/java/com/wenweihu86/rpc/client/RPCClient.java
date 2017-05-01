package com.wenweihu86.rpc.client;

import com.google.protobuf.GeneratedMessageV3;
import com.wenweihu86.rpc.client.handler.RPCClientHandler;
import com.wenweihu86.rpc.client.pool.Connection;
import com.wenweihu86.rpc.client.pool.ConnectionPool;
import com.wenweihu86.rpc.codec.RPCHeader;
import com.wenweihu86.rpc.codec.RPCMessage;
import com.wenweihu86.rpc.filter.Filter;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.wenweihu86.rpc.codec.RPCDecoder;
import com.wenweihu86.rpc.codec.RPCEncoder;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
@SuppressWarnings("unchecked")
public class RPCClient {

    private static final Logger LOG = LoggerFactory.getLogger(RPCClient.class);

    private static volatile boolean isInit = false;
    private static RPCClientOption rpcClientOption;
    private static Bootstrap bootstrap;
    private static Map<String, RPCFuture> pendingRPC;
    private static ScheduledExecutorService scheduledExecutor;

    private List<ConnectionPool> connectionPoolList;
    private List<Filter> filters;

    public RPCClient(String ipPorts) {
        this(ipPorts, null, null);
    }

    public RPCClient(String ipPorts, RPCClientOption option) {
        this(ipPorts, option, null);
    }

    public RPCClient(String ipPorts, List<Filter> filters) {
        this(ipPorts, null, filters);
    }

    public RPCClient(String ipPorts, RPCClientOption option, List<Filter> filters) {
        if (!isInit) {
            RPCClient.init(option);
        }
        if (ipPorts == null || ipPorts.length() == 0) {
            LOG.error("ipPorts format error, the right format is 10.1.1.1:8888;10.2.2.2:9999");
            throw new IllegalArgumentException("ipPorts format error");
        }
        String[] ipPortSplits = ipPorts.split(";");
        this.connectionPoolList = new ArrayList<>(ipPortSplits.length);
        for (String ipPort : ipPortSplits) {
            String[] ipPortSplit = ipPort.split(":");
            if (ipPortSplit.length != 2) {
                LOG.error("ipPorts format error, the right format is 10.1.1.1:8888;10.2.2.2:9999");
                throw new IllegalArgumentException("ipPorts format error");
            }
            String ip = ipPortSplit[0];
            int port = Integer.valueOf(ipPortSplit[1]);
            ConnectionPool connectionPool = new ConnectionPool(this, ip, port);
            connectionPoolList.add(connectionPool);
        }
        this.filters = filters;
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
        if (callback == null) {
            LOG.error("callback of async call can not be null");
            throw new IllegalArgumentException("callback of async call can not be null");
        }
        Type type = callback.getClass().getGenericInterfaces()[0];
        Class responseBodyClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        final String logId = UUID.randomUUID().toString();
        RPCMessage<RPCHeader.RequestHeader> fullRequest = this.buildFullRequest(
                logId, serviceName, methodName, request, responseBodyClass);
        this.sendRequest(fullRequest, callback);
    }

    public RPCMessage<RPCHeader.RequestHeader> buildFullRequest(
            final String logId,
            final String serviceName,
            final String methodName,
            Object requestBody,
            Class responseBodyClass) {
        RPCMessage<RPCHeader.RequestHeader> fullRequest = new RPCMessage<>();

        RPCHeader.RequestHeader.Builder headerBuilder = RPCHeader.RequestHeader.newBuilder();
        headerBuilder.setLogId(logId);
        headerBuilder.setServiceName(serviceName);
        headerBuilder.setMethodName(methodName);
        fullRequest.setHeader(headerBuilder.build());
        fullRequest.setResponseBodyClass(responseBodyClass);

        if (!GeneratedMessageV3.class.isAssignableFrom(requestBody.getClass())) {
            LOG.error("request must be protobuf message");
            return null;
        }
        fullRequest.setBodyMessage((GeneratedMessageV3) requestBody);

        try {
            Method encodeMethod = requestBody.getClass().getMethod("toByteArray");
            byte[] bodyBytes = (byte[]) encodeMethod.invoke(requestBody);
            fullRequest.setBody(bodyBytes);
        } catch (Exception ex) {
            LOG.error("request object has no method toByteArray");
            return null;
        }

        return fullRequest;
    }

    public <T> RPCFuture sendRequest(final RPCMessage<RPCHeader.RequestHeader> fullRequest,
                                     RPCCallback<T> callback) {
        final String logId = fullRequest.getHeader().getLogId();
        try {
            this.doSend(fullRequest);
            // add request to RPCFuture and add timeout task
            final ScheduledExecutorService scheduledExecutor = RPCClient.getScheduledExecutor();
            final long readTimeout = RPCClient.getRpcClientOption().getReadTimeoutMillis();
            final String serviceName = fullRequest.getHeader().getServiceName();
            final String methodName = fullRequest.getHeader().getMethodName();
            ScheduledFuture scheduledFuture = scheduledExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    RPCFuture rpcFuture = RPCClient.removeRPCFuture(logId);
                    if (rpcFuture != null) {
                        LOG.debug("request timeout, logId={}, service={}, method={}",
                                logId, serviceName, methodName);
                        rpcFuture.timeout();
                    } else {
                        LOG.debug("request logId={} not found", logId);
                    }
                }
            }, readTimeout, TimeUnit.MILLISECONDS);

            RPCFuture future = new RPCFuture(scheduledFuture, fullRequest, callback);
            RPCClient.addRPCFuture(logId, future);
            return future;
        } catch (RuntimeException ex) {
            RPCClient.removeRPCFuture(logId);
            return null;
        }
    }

    public Channel connect(String ip, int port) {
        try {
            final ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port));
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        LOG.debug("Connection {} is established", channelFuture.channel());
                    } else {
                        LOG.warn(String.format("Connection get failed on {} due to {}",
                                channelFuture.cause().getMessage(), channelFuture.cause()));
                    }
                }
            });
            future.awaitUninterruptibly();
            if (future.isSuccess()) {
                LOG.debug("connect {}:{} success", ip, port);
                return future.channel();
            } else {
                LOG.warn("connect {}:{} failed", ip, port);
                return null;
            }
        } catch (Exception e) {
            LOG.error("failed to connect to {}:{} due to {}", ip, port, e.getMessage());
            return null;
        }
    }

    private synchronized static void init(RPCClientOption option) {
        if (!isInit) {
            pendingRPC = new ConcurrentHashMap<>();
            scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

            if (option != null) {
                RPCClient.rpcClientOption = option;
            } else {
                RPCClient.rpcClientOption = new RPCClientOption();
            }

            bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcClientOption.getConnectTimeoutMillis());
            bootstrap.option(ChannelOption.SO_KEEPALIVE, rpcClientOption.isKeepAlive());
            bootstrap.option(ChannelOption.SO_REUSEADDR, rpcClientOption.isReuseAddr());
            bootstrap.option(ChannelOption.TCP_NODELAY, rpcClientOption.isTCPNoDelay());
            bootstrap.option(ChannelOption.SO_RCVBUF, rpcClientOption.getReceiveBufferSize());
            bootstrap.option(ChannelOption.SO_SNDBUF, rpcClientOption.getSendBufferSize());

            ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RPCEncoder<RPCHeader.RequestHeader>());
                    ch.pipeline().addLast(new RPCDecoder(false));
                    ch.pipeline().addLast(new RPCClientHandler());
                }
            };
            bootstrap.group(new NioEventLoopGroup()).handler(initializer);
            isInit = true;
        }
    }

    private void doSend(RPCMessage<RPCHeader.RequestHeader> fullRequest) {
        int maxTryNum = 3;
        int currentTry = 0;
        Set<Integer> excludedSet = new HashSet<>(maxTryNum);
        while (currentTry < maxTryNum) {
            int index = this.selectConnectionIndex(excludedSet);
            excludedSet.add(index);
            ConnectionPool connectionPool = this.connectionPoolList.get(index);
            Connection connection = connectionPool.getConnection();
            if (connection == null
                    || !connection.getChannel().isOpen()
                    || !connection.getChannel().isActive()) {
                if (currentTry < maxTryNum - 1) {
                    currentTry++;
                    connectionPool.returnConnection(connection);
                    continue;
                } else {
                    connectionPool.returnConnection(connection);
                    throw new RuntimeException("connect failed");
                }
            }
            Channel channel = connection.getChannel();
            LOG.debug("channel isActive={}", channel.isActive());
            connection.setChannel(channel);
            channel.writeAndFlush(fullRequest);
            connectionPool.returnConnection(connection);
            break;
        }
    }

    private int selectConnectionIndex(Set<Integer> excludedSet) {
        int maxConnectNum = this.connectionPoolList.size();
        int tryNum = 0;
        int randIndex = ThreadLocalRandom.current().nextInt(0, maxConnectNum);
        while (excludedSet.contains(randIndex) && tryNum < maxConnectNum) {
            randIndex = ThreadLocalRandom.current().nextInt(0, maxConnectNum);
            tryNum++;
        }
        return randIndex;
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

    public List<Filter> getFilters() {
        return filters;
    }

}
