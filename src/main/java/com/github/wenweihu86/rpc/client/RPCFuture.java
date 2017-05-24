package com.github.wenweihu86.rpc.client;

import com.github.wenweihu86.rpc.codec.RPCHeader;
import com.github.wenweihu86.rpc.codec.RPCMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class RPCFuture<T> implements Future<RPCMessage<RPCHeader.ResponseHeader>> {

    private static final Logger LOG = LoggerFactory.getLogger(RPCFuture.class);

    private CountDownLatch latch;
    private ScheduledFuture scheduledFuture;
    private RPCMessage<RPCHeader.RequestHeader> fullRequest;
    private RPCCallback<T> callback;

    private RPCMessage<RPCHeader.ResponseHeader> fullResponse;
    private Throwable error;
    private boolean isDone;

    public RPCFuture(ScheduledFuture scheduledFuture,
                     RPCMessage<RPCHeader.RequestHeader> fullRequest,
                     RPCCallback<T> callback) {
        if (fullRequest.getResponseBodyClass() == null && callback == null) {
            LOG.error("responseClass or callback must have one not null only");
            return;
        }
        this.fullRequest = fullRequest;
        this.scheduledFuture = scheduledFuture;
        this.callback = callback;
        if (this.fullRequest.getResponseBodyClass() == null) {
            Type type = callback.getClass().getGenericInterfaces()[0];
            Class clazz = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            this.fullRequest.setResponseBodyClass(clazz);
        }
        this.latch = new CountDownLatch(1);
    }

    public void success(RPCMessage<RPCHeader.ResponseHeader> fullResponse) {
        this.fullResponse = fullResponse;
        scheduledFuture.cancel(true);
        latch.countDown();
        if (callback != null) {
            callback.success((T) fullResponse.getBodyMessage());
        }
        isDone = true;
    }

    public void fail(Throwable error) {
        this.error = error;
        scheduledFuture.cancel(true);
        latch.countDown();
        if (callback != null) {
            callback.fail(error);
        }
        isDone = true;
    }

    public void timeout() {
        this.fullResponse = null;
        latch.countDown();
        if (callback != null) {
            callback.fail(new RuntimeException("timeout"));
        }
        isDone = true;
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
        return isDone;
    }

    @Override
    public RPCMessage<RPCHeader.ResponseHeader> get() throws InterruptedException {
        latch.await();
        if (error != null) {
            LOG.warn("error occurs due to {}", error.getMessage());
            RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
            fullResponse = newResponse(requestHeader.getLogId(),
                    RPCHeader.ResCode.RES_FAIL, error.getMessage());
        }
        if (fullResponse == null) {
            fullResponse = newResponse(fullRequest.getHeader().getLogId(),
                    RPCHeader.ResCode.RES_FAIL, "time out");
        }
        return fullResponse;
    }

    @Override
    public RPCMessage<RPCHeader.ResponseHeader> get(long timeout, TimeUnit unit) {
        RPCHeader.RequestHeader requestHeader = fullRequest.getHeader();
        try {
            if (latch.await(timeout, unit)) {
                if (error != null) {
                    LOG.warn("error occurrs due to {}", error.getMessage());
                    fullResponse = newResponse(requestHeader.getLogId(),
                            RPCHeader.ResCode.RES_FAIL, error.getMessage());
                }
            } else {
                LOG.warn("sync call time out");
                fullResponse = newResponse(requestHeader.getLogId(),
                        RPCHeader.ResCode.RES_FAIL, "time out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("sync call is interrupted, {}", e);
            fullResponse = newResponse(requestHeader.getLogId(),
                    RPCHeader.ResCode.RES_FAIL, "time out");
        }
        if (fullResponse == null) {
            fullResponse = newResponse(requestHeader.getLogId(),
                    RPCHeader.ResCode.RES_FAIL, "time out");
        }
        return fullResponse;
    }

    public Class getResponseClass() {
        return fullRequest.getResponseBodyClass();
    }

    private RPCMessage<RPCHeader.ResponseHeader> newResponse(
            String logId, RPCHeader.ResCode resCode, String resMsg) {
        RPCMessage<RPCHeader.ResponseHeader> fullResponse = new RPCMessage<>();
        RPCHeader.ResponseHeader responseHeader = RPCHeader.ResponseHeader.newBuilder()
                .setLogId(logId)
                .setResCode(resCode)
                .setResMsg(resMsg).build();
        fullResponse.setHeader(responseHeader);
        return fullResponse;
    }

}