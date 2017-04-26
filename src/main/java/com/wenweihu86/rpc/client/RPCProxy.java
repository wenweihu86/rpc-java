package com.wenweihu86.rpc.client;

import com.wenweihu86.rpc.codec.proto3.ProtoV3Header;
import com.wenweihu86.rpc.codec.proto3.ProtoV3Message;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class RPCProxy implements MethodInterceptor {

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
        ProtoV3Message<ProtoV3Header.RequestHeader> fullRequest = new ProtoV3Message<>();

        ProtoV3Header.RequestHeader.Builder headerBuilder = ProtoV3Header.RequestHeader.newBuilder();
        String logId = UUID.randomUUID().toString();
        headerBuilder.setLogId(logId);
        headerBuilder.setServiceName(method.getDeclaringClass().getSimpleName());
        headerBuilder.setMethodName(method.getName());
        fullRequest.setHeader(headerBuilder.build());

        Method encodeMethod = args[0].getClass().getMethod("toByteArray");
        byte[] bodyBytes = (byte[]) encodeMethod.invoke(args[0]);
        fullRequest.setBody(bodyBytes);

        RPCFuture future = new RPCFuture(method.getReturnType());
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
        return response;
    }

}
