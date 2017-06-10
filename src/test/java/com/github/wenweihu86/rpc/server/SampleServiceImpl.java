package com.github.wenweihu86.rpc.server;

import com.github.wenweihu86.rpc.api.Sample;
import com.github.wenweihu86.rpc.api.SampleService;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class SampleServiceImpl implements SampleService {

    @Override
    public Sample.SampleResponse sampleRPC(Sample.SampleRequest request) {
        String c = request.getB() + request.getA();
        Sample.SampleResponse response = Sample.SampleResponse.newBuilder()
                .setC(c).build();
        if (response != null) {
            System.out.println(response.getC());
        }
        return response;
    }
}
