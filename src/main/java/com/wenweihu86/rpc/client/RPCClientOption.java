package com.wenweihu86.rpc.client;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.nio.ByteOrder;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCClientOption {

    private int connectTimeoutMillis = 100;

    private int readTimeoutMillis = 1000;

    private int writeTimeoutMillis = 100;

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

    // acceptor threads, default use Netty default value
    private int acceptorThreadNum = 0;

    // io threads, default use Netty default value
    private int ioThreadNum = 0;

    // The max size
    private int maxSize = Integer.MAX_VALUE;

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

    public int getAcceptorThreadNum() {
        return acceptorThreadNum;
    }

    public void setAcceptorThreadNum(int acceptorThreadNum) {
        this.acceptorThreadNum = acceptorThreadNum;
    }

    public int getIOThreadNum() {
        return ioThreadNum;
    }

    public void setIOThreadNum(int ioThreadNum) {
        this.ioThreadNum = ioThreadNum;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

}
