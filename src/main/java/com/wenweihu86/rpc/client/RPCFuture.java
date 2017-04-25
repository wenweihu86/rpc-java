package com.wenweihu86.rpc.client;

import com.google.protobuf.GeneratedMessageV3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RPCFuture implements Future<GeneratedMessageV3>, RPCCallback<GeneratedMessageV3> {

    private final CountDownLatch latch = new CountDownLatch(1);

    private Class responseClass;
    private GeneratedMessageV3 result;

    private Throwable error;

    public RPCFuture(Class responseClass) {
        this.responseClass = responseClass;
    }

    @Override
    public void success(GeneratedMessageV3 result) {
        this.result = result;
        latch.countDown();
    }

    @Override
    public void fail(Throwable error) {
        this.error = error;
        latch.countDown();
    }

    public GeneratedMessageV3 getResult() {
        return result;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public GeneratedMessageV3 get() throws InterruptedException {
        latch.await();
        if (error != null) {
            throw new RuntimeException("Error occurrs due to " + error.getMessage(), error);
        }
        return result;
    }

    @Override
    public GeneratedMessageV3 get(long timeout, TimeUnit unit) {
        try {
            if (latch.await(timeout, unit)) {
                if (error != null) {
                    throw new RuntimeException("Error occurrs due to " + error.getMessage(), error);
                }
                return result;
            } else {
                throw new RuntimeException("CallFuture async get time out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("CallFuture is interuptted", e);
        }
    }

    public void await() throws InterruptedException {
        latch.await();
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException {
        if (!latch.await(timeout, unit)) {
            throw new RuntimeException("timeout");
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() <= 0;
    }

    public Class getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class responseClass) {
        this.responseClass = responseClass;
    }
}