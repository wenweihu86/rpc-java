package com.github.wenweihu86.rpc.server;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCServerTest {
    public static void main(String[] args) {
        int port = 8766;
        if (args.length == 1) {
            port = Integer.valueOf(args[0]);
        }

        RPCServer rpcServer = new RPCServer(port);
        rpcServer.registerService(new SampleServiceImpl());
        rpcServer.start();

        // make server keep running
        synchronized (RPCServerTest.class) {
            try {
                RPCServerTest.class.wait();
            } catch (Throwable e) {
            }
        }
    }
}
