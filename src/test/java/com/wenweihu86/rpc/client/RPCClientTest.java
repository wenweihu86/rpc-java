package com.wenweihu86.rpc.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
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

        String ipPorts = "127.0.0.1:8766";
        if (args.length == 1) {
            ipPorts = args[0];
        }
        RPCClient rpcClient = new RPCClient(ipPorts, clientOption);

        // build request
        Sample.SampleRequest request = Sample.SampleRequest.newBuilder()
                .setA(1)
                .setB("hello").build();

        final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
        // sync call
        SampleService sampleService = RPCProxy.getProxy(rpcClient, SampleService.class);
        Sample.SampleResponse response = sampleService.sampleRPC(request);
        if (response != null) {
            try {
                System.out.printf("sync call service=SampleService.sampleRPC success, " +
                                "request=%s response=%s\n",
                        printer.print(request), printer.print(response));
            } catch (InvalidProtocolBufferException ex) {
                System.out.println(ex.getMessage());
            }

        } else {
            System.out.println("server error, service=SampleService.sampleRPC");
        }

        // async call
        RPCCallback callback = new RPCCallback<Sample.SampleResponse>() {
            @Override
            public void success(Sample.SampleResponse response) {
                try {
                    System.out.printf("async call SampleService.sampleRPC success, response=%s\n",
                            printer.print(response));
                } catch (InvalidProtocolBufferException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            @Override
            public void fail(Throwable e) {
                System.out.printf("async call SampleService.sampleRPC failed, %s\n", e.getMessage());
            }
        };
        rpcClient.asyncCall("SampleService.sampleRPC", request, callback);
    }
}
