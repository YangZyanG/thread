package com.yangzy.thread.thread.threadPool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Pool {

    private static ThreadPoolExecutor threadPoolExecutor;

    private Pool(){

    }

    public static synchronized ThreadPoolExecutor getInstance(){

        if (threadPoolExecutor == null){
            threadPoolExecutor =
                    new ThreadPoolExecutor(16, 16, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(50));
        }

        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }
}
