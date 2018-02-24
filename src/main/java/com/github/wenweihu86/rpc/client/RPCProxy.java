package com.github.wenweihu86.rpc.client;

import com.github.wenweihu86.rpc.protocol.ProtocolProcessor;
import com.github.wenweihu86.rpc.protocol.standard.StandardProtocol;
import com.github.wenweihu86.rpc.utils.IDGenerator;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
@SuppressWarnings("unchecked")
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
        Long callId = IDGenerator.instance().getId();
        ProtocolProcessor protocol = StandardProtocol.instance();
        RPCCallback callback;
        Class<?> responseClass;
        Object fullRequest;
        if (args.length > 1) {
            callback = (RPCCallback) args[1];
            Method syncMethod = method.getDeclaringClass().getMethod(
                    method.getName(), method.getParameterTypes()[0]);
            responseClass = syncMethod.getReturnType();
            fullRequest = protocol.newRequest(callId, syncMethod, args[0], callback);
        } else {
            callback = null;
            responseClass = method.getReturnType();
            fullRequest = protocol.newRequest(callId, method, args[0], null);
        }

        int currentTryTimes = 0;
        Object response = null;
        while (currentTryTimes++ < rpcClient.getRPCClientOptions().getMaxTryTimes()) {
            Future future = rpcClient.sendRequest(callId, fullRequest, responseClass, callback);
            if (future == null) {
                continue;
            }
            if (callback != null) {
                return future;
            } else {
                response = future.get(
                        rpcClient.getRPCClientOptions().getReadTimeoutMillis(),
                        TimeUnit.MILLISECONDS);
                if (response != null) {
                    break;
                }
            }
        }
        return response;
    }

}
