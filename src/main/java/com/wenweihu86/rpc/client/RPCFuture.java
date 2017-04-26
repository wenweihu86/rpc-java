package com.wenweihu86.rpc.client;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RPCFuture {

    private static final Logger LOG = LoggerFactory.getLogger(RPCFuture.class);

    private CountDownLatch latch;
    private ScheduledFuture scheduledFuture;
    private Class responseClass;
    private GeneratedMessageV3 response;

    private Throwable error;

    public RPCFuture(ScheduledFuture scheduledFuture, Class responseClass) {
        this.scheduledFuture = scheduledFuture;
        this.responseClass = responseClass;
        this.latch = new CountDownLatch(1);
    }

    public void success(GeneratedMessageV3 response) {
        this.response = response;
        scheduledFuture.cancel(true);
        latch.countDown();
    }

    public void fail(Throwable error) {
        this.error = error;
        scheduledFuture.cancel(true);
        latch.countDown();
    }

    public void timeout() {
        this.response = null;
        latch.countDown();
    }

    public GeneratedMessageV3 get() throws InterruptedException {
        latch.await();
        if (error != null) {
            LOG.warn("error occurs due to {}", error.getMessage());
            return null;
        }
        return response;
    }

    public GeneratedMessageV3 get(long timeout, TimeUnit unit) {
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