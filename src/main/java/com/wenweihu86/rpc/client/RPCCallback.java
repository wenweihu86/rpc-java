package com.wenweihu86.rpc.client;

public interface RPCCallback<T> {

    void success(T result);

    void fail(Throwable e);

}
