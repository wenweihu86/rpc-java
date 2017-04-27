package com.wenweihu86.rpc.client;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;
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

        final String logId = UUID.randomUUID().toString();
        final String serviceName = method.getDeclaringClass().getSimpleName();
        final String methodName = method.getName();
        RPCFuture future = rpcClient.sendRequest(
                logId, serviceName, methodName,
                args[0], method.getReturnType(), null);

        if (future == null) {
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
