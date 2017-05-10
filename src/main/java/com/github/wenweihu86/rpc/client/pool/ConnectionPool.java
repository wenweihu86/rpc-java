package com.github.wenweihu86.rpc.client.pool;

import com.github.wenweihu86.rpc.client.RPCClient;
import com.github.wenweihu86.rpc.client.RPCClientOption;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wenweihu86 on 2017/4/29.
 */
public class ConnectionPool {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPool.class);

    private GenericObjectPool<Connection> pool;

    public ConnectionPool(RPCClient rpcClient, String host, int port) {
        RPCClientOption clientOption = RPCClient.getRpcClientOption();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(clientOption.getMaxIdleSize());
        config.setMinIdle(clientOption.getMinIdleSize());
        config.setMaxTotal(clientOption.getThreadPoolSize());
        config.setMaxWaitMillis(clientOption.getMaxWaitMillis());
        config.setMinEvictableIdleTimeMillis(clientOption.getMinEvictableIdleTimeMillis());
        config.setTestOnCreate(clientOption.isTestOnCreate());
        config.setTestOnBorrow(clientOption.isTestOnBorrow());
        config.setTestOnReturn(clientOption.isTestOnReturn());
        config.setLifo(clientOption.isLifo());

        PooledObjectFactory<Connection> objectFactory = new ConnectionPooledObjectFactory(rpcClient, host, port);
        this.pool = new GenericObjectPool<>(objectFactory, config);
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = this.pool.borrowObject();
        } catch (Exception ex) {
            LOG.warn("get connectin failed, msg={}", ex.getMessage());
        }
        return connection;
    }

    public void returnConnection(Connection connection) {
        try {
            pool.returnObject(connection);
        } catch (Exception ex) {
            LOG.warn("return connection failed, msg={}", ex.getMessage());
        }
    }

    public void stop() {
        try {
            if (pool != null) {
                pool.clear();
                pool.close();
            }
        } catch (Exception e) {
            LOG.warn("stop connection pool failed!", e);
        }
    }

}
