package com.github.wenweihu86.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ServiceManager {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);
    private static volatile ServiceManager instance;

    private Map<String, ServiceInfo> serviceMap;

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
        this.serviceMap = new HashMap<String, ServiceInfo>();
    }

    public void registerService(ServiceInfo serviceInfo) {
        String key = buildServiceKey(serviceInfo.getServiceName(), serviceInfo.getMethodName());
        serviceMap.put(key, serviceInfo);
        LOG.info("register service, {}", key);
    }

    public ServiceInfo getService(String serviceName, String methodName) {
        String key = buildServiceKey(serviceName, methodName);
        return serviceMap.get(key);
    }

    public ServiceInfo getService(String serviceMethodName) {
        return serviceMap.get(serviceMethodName);
    }

    private String buildServiceKey(String serviceName, String methodName) {
        return serviceName + "." + methodName;
    }
}
