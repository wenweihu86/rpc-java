package com.github.wenweihu86.rpc.client;

import com.github.wenweihu86.rpc.api.Sample;
import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import com.github.wenweihu86.rpc.filter.Filter;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.github.wenweihu86.rpc.api.SampleService;
import com.github.wenweihu86.rpc.filter.ClientCustomParamFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by wenweihu86 on 2017/4/26.
 */
@SuppressWarnings("unchecked")
public class RPCClientTest {

    public static void main(String[] args) {
        RPCClientOptions clientOption = new RPCClientOptions();
        clientOption.setWriteTimeoutMillis(200);
        clientOption.setReadTimeoutMillis(500);

        String ipPorts = "127.0.0.1:8766";
        if (args.length == 1) {
            ipPorts = args[0];
        }

        List<Filter> filters = new ArrayList<>();
        ClientCustomParamFilter customParamFilter = new ClientCustomParamFilter();
        filters.add(customParamFilter);
        RPCClient rpcClient = new RPCClient(ipPorts, clientOption, filters);

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
        Future future = rpcClient.asyncCall("SampleService.sampleRPC", request, callback);
        try {
            if (future != null) {
                future.get();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        rpcClient.stop();
    }

}
