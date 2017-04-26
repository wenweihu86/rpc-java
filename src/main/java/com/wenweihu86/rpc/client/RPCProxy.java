package com.wenweihu86.rpc.client;

import com.wenweihu86.rpc.codec.proto3.ProtoV3Header;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Message;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCProxy implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RPCProxy.class);

    private RPCClient rpcClient;

    public RPCProxy(RPCClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public static <T> T getProxy(RPCClient rpcClient, Class clazz) {
        Enhancer en = new Enhancer();
        en.setSuperclass(clazz);
        en.setCallback(new RPCProxy(rpcClient));
        return (T) en.create();
    }

    public Object intercept(Object obj, Method method, Object[] args,
                            MethodProxy proxy) throws Throwable {
        long startTime = System.currentTimeMillis();

        ProtoV3Message<ProtoV3Header.RequestHeader> fullRequest = new ProtoV3Message<>();

        ProtoV3Header.RequestHeader.Builder headerBuilder = ProtoV3Header.RequestHeader.newBuilder();
        final String logId = UUID.randomUUID().toString();
        headerBuilder.setLogId(logId);
        final String serviceName = method.getDeclaringClass().getSimpleName();
        headerBuilder.setServiceName(serviceName);
        final String methodName = method.getName();
        headerBuilder.setMethodName(methodName);
        fullRequest.setHeader(headerBuilder.build());

        Method encodeMethod = args[0].getClass().getMethod("toByteArray");
        byte[] bodyBytes = (byte[]) encodeMethod.invoke(args[0]);
        fullRequest.setBody(bodyBytes);

        final ScheduledExecutorService scheduledExecutor = RPCClient.getScheduledExecutor();
        final long readWriteTimeout = RPCClient.getRpcClientOption().getReadTimeoutMillis()
                + RPCClient.getRpcClientOption().getWriteTimeoutMillis();
        ScheduledFuture scheduledFuture = scheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                RPCFuture rpcFuture = RPCClient.removeRPCFuture(logId);
                if (rpcFuture != null) {
                    LOG.warn("request timeout, logId={}, service={}, method={}",
                            logId, serviceName, methodName);
                    rpcFuture.timeout();
                } else {
                    LOG.warn("request logId={} not found", logId);
                }
            }
        }, readWriteTimeout, TimeUnit.MILLISECONDS);

        RPCFuture future = new RPCFuture(scheduledFuture, method.getReturnType());
        RPCClient.addRPCFuture(logId, future);
        try {
            rpcClient.sendRequest(fullRequest);
        } catch (RuntimeException ex) {
            RPCClient.removeRPCFuture(logId);
            return null;
        }
        Object response = future.get(
                RPCClient.getRpcClientOption().getReadTimeoutMillis(),
                TimeUnit.MILLISECONDS);

        long endTime = System.currentTimeMillis();
        LOG.info("elapse={}ms service={} method={} logId={} request={} response={}",
                endTime - startTime, serviceName, methodName, logId,
                args[0].toString(), response.toString());
        return response;
    }

}
