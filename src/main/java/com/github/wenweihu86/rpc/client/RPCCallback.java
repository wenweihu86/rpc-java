package com.github.wenweihu86.rpc.client;

public interface RPCCallback<T> {

    void success(T response);

    void fail(Throwable e);

}
