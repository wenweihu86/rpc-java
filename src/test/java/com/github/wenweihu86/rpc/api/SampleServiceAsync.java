package com.github.wenweihu86.rpc.api;

import com.github.wenweihu86.rpc.client.RPCCallback;

import java.util.concurrent.Future;

public interface SampleServiceAsync extends SampleService {
    Future<Sample.SampleResponse> sampleRPC(Sample.SampleRequest request,
                                       RPCCallback<Sample.SampleResponse> callback);
}
