package com.github.wenweihu86.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RPCChannelGroup class keeps fixed connections with one server
 */
public class RPCChannelGroup {

    public static class ChannelInfo {
        private RPCChannelGroup channelGroup;
        private Channel channel;

        public ChannelInfo(RPCChannelGroup channelGroup, Channel channel) {
            this.channelGroup = channelGroup;
            this.channel = channel;
        }

        public RPCChannelGroup getChannelGroup() {
            return channelGroup;
        }

        public Channel getChannel() {
            return channel;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(RPCChannelGroup.class);

    private Bootstrap bootstrap;
    private String ip;
    private int port;
    private int connectionNum;
    private ChannelFuture[] channelFutures;
    private ReentrantLock[] locks;
    private volatile long failedNum;

    public RPCChannelGroup(String ip, int port, int connectionNum, Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.ip = ip;
        this.port = port;
        this.connectionNum = connectionNum;
        this.channelFutures = new ChannelFuture[connectionNum];
        this.locks = new ReentrantLock[connectionNum];
        for (int i = 0; i < connectionNum; i++) {
            this.locks[i] = new ReentrantLock();
            this.channelFutures[i] = connect(ip, port);
        }
    }

    public Channel getChannel(int index) {
        Validate.isTrue(index >=0 && index < connectionNum);
        if (isChannelValid(channelFutures[index])) {
            return channelFutures[index].channel();
        }

        ReentrantLock lock = locks[index];
        lock.lock();
        try {
            if (isChannelValid(channelFutures[index])) {
                return channelFutures[index].channel();
            }
            channelFutures[index] = connect(ip, port);
            if (channelFutures[index] == null) {
                return null;
            } else {
                channelFutures[index].sync();
                if (channelFutures[index].isSuccess()) {
                    return channelFutures[index].channel();
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            LOG.warn("connect to {}:{} failed, msg={}", ip, port, ex.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        for (int i = 0; i < connectionNum; i++) {
            if (channelFutures[i] != null) {
                channelFutures[i].channel().close();
            }
        }
    }

    private ChannelFuture connect(final String ip, final int port) {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    LOG.info("connect to {}:{} success, channel={}",
                            ip, port, channelFuture.channel());
                } else {
                    LOG.warn("future callback, connect to {}:{} failed due to {}",
                            ip, port, channelFuture.cause().getMessage());
                }
            }
        });
        return future;
    }

    private boolean isChannelValid(ChannelFuture channelFuture) {
        if (channelFuture != null && channelFuture.isSuccess()) {
            Channel channel = channelFuture.channel();
            if (channel != null && channel.isOpen() && channel.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        boolean flag = false;
        if (obj != null && RPCChannelGroup.class.isAssignableFrom(obj.getClass())) {
            RPCChannelGroup f = (RPCChannelGroup) obj;
            flag = new EqualsBuilder()
                    .append(ip, f.getIp())
                    .append(port, f.getPort())
                    .isEquals();
        }
        return flag;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ip)
                .append(port)
                .toHashCode();
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getConnectionNum() {
        return connectionNum;
    }

    public long getFailedNum() {
        return failedNum;
    }

    public void incFailedNum() {
        this.failedNum++;
    }
}
