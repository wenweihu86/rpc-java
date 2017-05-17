package com.github.wenweihu86.rpc.client;

/**
 * Created by wenweihu86 on 2017/5/17.
 */
public class EndPoint {
    String ip;
    int port;

    public EndPoint(String ip, int port) {
        this.ip = ip;
        this.port = port;
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

}
