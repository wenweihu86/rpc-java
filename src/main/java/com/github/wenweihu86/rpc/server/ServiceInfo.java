package com.github.wenweihu86.rpc.server;

import java.lang.reflect.Method;

/**
 * Created by wenweihu86 on 2017/4/25.
 */
public class ServiceInfo {

    private String serviceName;

    private String methodName;

    private Object service;

    private Method method;

    private Class requestClass;

    private Class responseClass;

    private Method parseFromForRequest;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class getRequestClass() {
        return requestClass;
    }

    public void setRequestClass(Class requestClass) {
        this.requestClass = requestClass;
    }

    public Class getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(Class responseClass) {
        this.responseClass = responseClass;
    }

    public Method getParseFromForRequest() {
        return parseFromForRequest;
    }

    public void setParseFromForRequest(Method parseFromForRequest) {
        this.parseFromForRequest = parseFromForRequest;
    }
}
