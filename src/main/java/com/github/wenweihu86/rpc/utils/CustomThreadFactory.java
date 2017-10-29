package com.github.wenweihu86.rpc.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

    private AtomicInteger threadNumber = new AtomicInteger(1);
    private String namePrefix;
    private ThreadGroup group;

    public CustomThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
