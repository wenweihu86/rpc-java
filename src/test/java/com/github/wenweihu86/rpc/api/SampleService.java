package com.github.wenweihu86.rpc.api;

import com.github.wenweihu86.rpc.protocol.RPCMeta;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public interface SampleService {
    /**
     * 当需要定制serviceName和methodName时，用RPCMeta注解。
     * RPCMeta可选，默认是通过反射获取。
     */
    @RPCMeta(serviceName = "SampleService", methodName = "sampleRPC")
    Sample.SampleResponse sampleRPC(Sample.SampleRequest request);
}
