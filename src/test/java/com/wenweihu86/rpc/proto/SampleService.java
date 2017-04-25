package com.wenweihu86.rpc.proto;

import com.wenweihu86.rpc.proto.Sample;

/**
 * Created by baidu on 2017/4/25.
 */
public interface SampleService {
    Sample.SampleResponse sampleRPC(Sample.SampleRequest request);
}
