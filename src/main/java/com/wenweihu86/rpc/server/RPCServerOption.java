package com.wenweihu86.rpc.server;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.nio.ByteOrder;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCServerOption {

    // The keep alive
    private boolean keepAlive;

    // 字节顺序
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    private boolean tcpNoDelay = true;

    // so linger
    private int soLinger = 5;

    // backlog
    private int backlog = 100;

    // receive buffer size
    private int receiveBufferSize = 1024 * 64;

    // send buffer size
    private int sendBufferSize = 1024 * 64;

    /**
     * an {@link IdleStateEvent} whose state is {@link IdleState#READER_IDLE}
     * will be triggered when no read was performed for the specified period of time.
     * Specify {@code 0} to disable.
     */
    private int readerIdleTime = 60;

    /**
     * an {@link IdleStateEvent} whose state is {@link IdleState#WRITER_IDLE}
     * will be triggered when no write was performed for the specified period of time.
     * Specify {@code 0} to disable.
     */
    private int writerIdleTime = 60;

    // connect timeout, in milliseconds
    private int connectTimeout;

    // keepAlive时间（second）
    private int keepAliveTime;

    // acceptor threads, default use Netty default value
    private int acceptorThreadNum = 0;

    // io threads, default use Netty default value
    private int ioThreadNum = 0;

    // real work threads
    private int workThreadNum = Runtime.getRuntime().availableProcessors() * 2;

    // The max size
    private int maxSize = Integer.MAX_VALUE;

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
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

    public int getReaderIdleTime() {
        return readerIdleTime;
    }

    public void setReaderIdleTime(int readerIdleTime) {
        this.readerIdleTime = readerIdleTime;
    }

    public int getWriterIdleTime() {
        return writerIdleTime;
    }

    public void setWriterIdleTime(int writerIdleTime) {
        this.writerIdleTime = writerIdleTime;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
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

    public int getWorkThreadNum() {
        return workThreadNum;
    }

    public void setWorkThreadNum(int workThreadNum) {
        this.workThreadNum = workThreadNum;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

}
