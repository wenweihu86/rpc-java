package com.github.wenweihu86.rpc.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ServiceManager {

    private static volatile ServiceManager instance;

    private Map<String, ServiceInfo> serviceInfoMap;

    public static ServiceManager getInstance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) {
                    instance = new ServiceManager();
                }
            }
        }
        return instance;
    }

    public ServiceManager() {
        this.serviceInfoMap = new HashMap<String, ServiceInfo>();
    }

    public void registerService(ServiceInfo serviceInfo) {
        String key = buildServiceKey(serviceInfo.getServiceName(), serviceInfo.getMethodName());
        serviceInfoMap.put(key, serviceInfo);
    }

    public ServiceInfo getService(String serviceName, String methodName) {
        String key = buildServiceKey(serviceName, methodName);
        return serviceInfoMap.get(key);
    }

    private String buildServiceKey(String serviceName, String methodName) {
        return serviceName + ":" + methodName;
    }
}
