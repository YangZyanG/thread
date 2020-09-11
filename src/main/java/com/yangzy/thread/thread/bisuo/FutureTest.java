package com.yangzy.thread.thread.bisuo;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author yangziyang
 * @since 2020-09-11
 * Future可以获得线程运行结果
 */
public class FutureTest {

    private final FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            System.out.println("开始执行任务...");
            int a = 2 + 3;
            Thread.currentThread().sleep(5000);
            System.out.println("任务执行完成");
            return a;
        }
    });

    private final Thread thread = new Thread(future);

    public void start(){
        thread.start();
    }

    /**
     * 只要任务没执行完成，线程会一直阻塞在get方法上
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Integer get() throws ExecutionException, InterruptedException {
        return future.get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTest test = new FutureTest();
        test.start();
        System.out.println(test.get());
    }
}
