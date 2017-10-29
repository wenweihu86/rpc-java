package com.github.wenweihu86.rpc.client;

import com.github.wenweihu86.rpc.api.Sample;
import com.github.wenweihu86.rpc.api.SampleService;
import com.google.protobuf.util.JsonFormat;

/**
 * Created by wenweihu86 on 2017/5/1.
 */
public class BenchmarkTest {

    private static volatile int totalRequestNum = 0;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("muse have one argument: threadNum");
            System.exit(-1);
        }
        RPCClient rpcClient = new RPCClient("127.0.0.1:8766");
        int threadNum = Integer.parseInt(args[0]);
        Thread[] threads = new Thread[threadNum];
        for (int i = 0; i < threadNum; i++) {
            threads[i] = new Thread(new ThreadTask(rpcClient));
            threads[i].start();
        }
        while (true) {
            int lastRequestNum = totalRequestNum;
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("qps=" + (totalRequestNum - lastRequestNum));
        }
    }

    public static class ThreadTask implements Runnable {

        private RPCClient rpcClient;
        private SampleService sampleService;

        public ThreadTask(RPCClient rpcClient) {
            this.rpcClient = rpcClient;
            this.sampleService = RPCProxy.getProxy(rpcClient, SampleService.class);
        }

        public void run() {
            int currentRequestNum = 0;
            int maxRequestNum = 10000;
            while (true) {
                long beginTime = System.currentTimeMillis();
                // build request
                Sample.SampleRequest request = Sample.SampleRequest.newBuilder()
                        .setA(1)
                        .setB("hello").build();
                // sync call
                Sample.SampleResponse response = sampleService.sampleRPC(request);
                long endTime = System.currentTimeMillis();
                if (response != null) {
                    currentRequestNum++;
                    totalRequestNum++;
                    if (currentRequestNum == maxRequestNum) {
                        float averageTime = ((float) (endTime - beginTime)) % maxRequestNum;
                        System.out.println("average elpaseMs=" + averageTime);
                        currentRequestNum = 0;
                    }
                } else {
                    System.out.println("server error");
                }
            }
        }

    }
}
