package com.github.wenweihu86.rpc.client;

import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.nio.ByteOrder;

/**
 * Created by wenweihu86 on 2017/4/24.
 */
public class RPCClientOptions {

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

    // connection pool
    /**
     * max ilde connection size
     * @see GenericObjectPool#getMaxIdle()
     */
    private int maxIdleSize = 8;

    /**
     * min idle connection size
     * @see GenericObjectPool#getMinIdle()
     */
    private int minIdleSize = 2;

    /**
     * max connection size that connection pool can make
     * @see GenericObjectPool#getMaxTotal()
     */
    private int threadPoolSize = -1;

    /**
     * max wait time when borrow connection from pool, in milliseconds
     * @see GenericObjectPool#getMaxWaitMillis()
     */
    private int maxWaitMillis = -1;

    // The min evictable idle time
    private int minEvictableIdleTimeMillis = 1000 * 60 * 2;

    /**
     * The default value for the {@code testOnCreate} configuration attribute.
     * @see GenericObjectPool#getTestOnCreate()
     */
    private boolean testOnCreate = true;

    /**
     * The default value for the {@code testOnBorrow} configuration attribute.
     * @see GenericObjectPool#getTestOnBorrow()
     */
    private boolean testOnBorrow = true;

    /**
     * The default value for the {@code testOnReturn} configuration attribute.
     * @see GenericObjectPool#getTestOnReturn()
     */
    private boolean testOnReturn = false;

    /**
     * The default value for the {@code lifo} configuration attribute.
     * @see GenericObjectPool#getLifo()
     */
    private boolean lifo = false;

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

    public int getMaxIdleSize() {
        return maxIdleSize;
    }

    public void setMaxIdleSize(int maxIdleSize) {
        this.maxIdleSize = maxIdleSize;
    }

    public int getMinIdleSize() {
        return minIdleSize;
    }

    public void setMinIdleSize(int minIdleSize) {
        this.minIdleSize = minIdleSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(int maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public int getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public boolean isTestOnCreate() {
        return testOnCreate;
    }

    public void setTestOnCreate(boolean testOnCreate) {
        this.testOnCreate = testOnCreate;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isLifo() {
        return lifo;
    }

    public void setLifo(boolean lifo) {
        this.lifo = lifo;
    }
}
