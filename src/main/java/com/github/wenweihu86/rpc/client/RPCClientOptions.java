package com.github.wenweihu86.rpc.client;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCClientOptions {

    private int connectTimeoutMillis = 1000;

    private int readTimeoutMillis = 1000;

    private int writeTimeoutMillis = 1000;

    private int maxConnectionNumPerHost = 8;

    private int maxTryTimes = 3;

    private int namingServiceUpdateIntervalMillis = 1000;

    // The keep alive
    private boolean keepAlive = true;

    private boolean reuseAddr = true;

    private boolean tcpNoDelay = true;

    // so linger
    private int soLinger = 5;

    // backlog
    private int backlog = 100;

    // receive buffer size
    private int receiveBufferSize = 1024 * 64;

    // send buffer size
    private int sendBufferSize = 1024 * 64;

    // keepAlive时间（second）
    private int keepAliveTime;

    // io threads, default use Netty default value
    private int ioThreadNum = 0;

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public int getWriteTimeoutMillis() {
        return writeTimeoutMillis;
    }

    public void setWriteTimeoutMillis(int writeTimeoutMillis) {
        this.writeTimeoutMillis = writeTimeoutMillis;
    }

    public int getMaxConnectionNumPerHost() {
        return maxConnectionNumPerHost;
    }

    public void setMaxConnectionNumPerHost(int maxConnectionNumPerHost) {
        this.maxConnectionNumPerHost = maxConnectionNumPerHost;
    }

    public int getMaxTryTimes() {
        return maxTryTimes;
    }

    public void setMaxTryTimes(int maxTryTimes) {
        this.maxTryTimes = maxTryTimes;
    }

    public int getNamingServiceUpdateIntervalMillis() {
        return namingServiceUpdateIntervalMillis;
    }

    public void setNamingServiceUpdateIntervalMillis(int namingServiceUpdateIntervalMillis) {
        this.namingServiceUpdateIntervalMillis = namingServiceUpdateIntervalMillis;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isReuseAddr() {
        return reuseAddr;
    }

    public void setReuseAddr(boolean reuseAddr) {
        this.reuseAddr = reuseAddr;
    }

    public boolean isTCPNoDelay() {
        return tcpNoDelay;
    }

    public void setTCPNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public int getSoLinger() {
        return soLinger;
    }

    public void setSoLinger(int soLinger) {
        this.soLinger = soLinger;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public int getIoThreadNum() {
        return ioThreadNum;
    }

    public void setIoThreadNum(int ioThreadNum) {
        this.ioThreadNum = ioThreadNum;
    }

}
