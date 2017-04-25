package com.wenweihu86.rpc.client;

import com.wenweihu86.rpc.proto.Sample;
import com.wenweihu86.rpc.proto.SampleService;

/**
 * Created by wenweihu86 on 2017/4/26.
 */
public class RPCClientTest {

    public static void main(String[] args) {
        RPCClient rpcClient = new RPCClient("127.0.0.1", 8766);
        SampleService sampleService = RPCProxy.getProxy(rpcClient, SampleService.class);
        Sample.SampleRequest request = Sample.SampleRequest.newBuilder()
                .setA(1)
                .setB("hello").build();
        Sample.SampleResponse response = sampleService.sampleRPC(request);
        System.out.println(response.getC());
    }
}
