package com.wenweihu86.rpc.server;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCServerTest {
    public static void main(String[] args) {
        RPCServer rpcServer = new RPCServer(8766);
        rpcServer.registerService(new SampleServiceImpl());
        rpcServer.start();
    }
}
