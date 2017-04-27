package com.wenweihu86.rpc.client;

import io.netty.channel.Channel;

/**
 * Created by wenweihu86 on 2017/4/27.
 */
public class Connection {

    private String ip;
    private int port;
    private Channel channel;

    public Connection(String ip, int port, Channel channel) {
        this.ip = ip;
        this.port = port;
        this.channel = channel;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
