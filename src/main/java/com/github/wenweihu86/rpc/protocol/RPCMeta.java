package com.github.wenweihu86.rpc.protocol;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCMeta {
    /**
     * 发给服务端的服务名称
     */
    String serviceName() default "";

    /**
     * 发给服务端的方法名称
     */
    String methodName() default "";
}
