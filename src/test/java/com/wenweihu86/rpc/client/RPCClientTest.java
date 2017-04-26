package com.wenweihu86.rpc.client;

import com.wenweihu86.rpc.proto.Sample;
import com.wenweihu86.rpc.proto.SampleService;

/**
 * Created by wenweihu86 on 2017/4/26.
 */
public class RPCClientTest {

    public static void main(String[] args) {
        RPCClientOption clientOption = new RPCClientOption();
        clientOption.setWriteTimeoutMillis(200);
        clientOption.setReadTimeoutMillis(500);
        RPCClient rpcClient = new RPCClient("127.0.0.1", 8766, clientOption);
        SampleService sampleService = RPCProxy.getProxy(rpcClient, SampleService.class);
        Sample.SampleRequest request = Sample.SampleRequest.newBuilder()
                .setA(1)
                .setB("hello").build();
        Sample.SampleResponse response = sampleService.sampleRPC(request);
        if (response != null) {
            System.out.printf("service=SampleService.sampleRPC, request=%s response=%s",
                    request.toString(), response.toString());
        } else {
            System.out.println("server error, service=SampleService.sampleRPC");
        }
    }
}
