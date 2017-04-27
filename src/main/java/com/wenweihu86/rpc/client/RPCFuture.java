package com.wenweihu86.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RPCFuture<T> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCFuture.class);

    private CountDownLatch latch;
    private ScheduledFuture scheduledFuture;
    private Class responseClass;
    private T response;
    private RPCCallback<T> callback;

    private Throwable error;

    public RPCFuture(ScheduledFuture scheduledFuture,
                     Class responseClass,
                     RPCCallback<T> callback) {
        if (responseClass == null && callback == null) {
            LOG.error("responseClass or callback must have one not null only");
            return;
        }
        this.scheduledFuture = scheduledFuture;
        this.callback = callback;
        if (responseClass != null) {
            this.responseClass = responseClass;
        } else {
            Type type = callback.getClass().getGenericInterfaces()[0];
            Class clazz = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            this.responseClass = clazz;
        }
        this.latch = new CountDownLatch(1);
    }

    public void success(T response) {
        this.response = response;
        scheduledFuture.cancel(true);
        latch.countDown();
        if (callback != null) {
            callback.success(response);
        }
    }

    public void fail(Throwable error) {
        this.error = error;
        scheduledFuture.cancel(true);
        latch.countDown();
        if (callback != null) {
            callback.fail(error);
        }
    }

    public void timeout() {
        this.response = null;
        latch.countDown();
        if (callback != null) {
            callback.fail(new RuntimeException("timeout"));
        }
    }

    public T get() throws InterruptedException {
        latch.await();
        if (error != null) {
            LOG.warn("error occurs due to {}", error.getMessage());
            return null;
        }
        return response;
    }

    public T get(long timeout, TimeUnit unit) {
        try {
            if (latch.await(timeout, unit)) {
                if (error != null) {
                    LOG.warn("error occurrs due to {}", error.getMessage());
                    return null;
                }
                return response;
            } else {
                LOG.warn("sync call time out");
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("sync call is interrupted, {}", e);
            return null;
        }
    }

    public Class getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class responseClass) {
        this.responseClass = responseClass;
    }
}