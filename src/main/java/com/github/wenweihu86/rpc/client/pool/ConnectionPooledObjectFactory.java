package com.github.wenweihu86.rpc.client.pool;

import com.github.wenweihu86.rpc.client.RPCClient;
import io.netty.channel.Channel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by wenweihu86 on 2017/4/29.
 */
public class ConnectionPooledObjectFactory extends BasePooledObjectFactory<Connection> {

    private RPCClient rpcClient;
    private String host;
    private int port;

    public ConnectionPooledObjectFactory(RPCClient rpcClient, String host, int port) {
        this.rpcClient = rpcClient;
        this.host = host;
        this.port = port;
    }

    @Override
    public Connection create() throws Exception {
        return fetchConnection();
    }

    @Override
    public PooledObject<Connection> wrap(Connection obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        Connection c = p.getObject();
        Channel channel = c.getChannel();
        if (channel != null && channel.isOpen() && channel.isActive()) {
            channel.close();
        }
    }

    public boolean validateObject(PooledObject<Connection> p) {
        Connection c = p.getObject();
        Channel channel = c.getChannel();
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public Connection fetchConnection() {
        Channel channel = rpcClient.connect(host, port);
        return new Connection(host, port, channel);
    }

}
