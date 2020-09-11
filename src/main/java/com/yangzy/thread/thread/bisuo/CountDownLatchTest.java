package com.yangzy.thread.thread.bisuo;

import java.util.concurrent.CountDownLatch;

/**
 * @author yangziyang
 * @since 2020-09-11
 */
public class CountDownLatchTest implements Runnable {

    public void latch(int nThread, Runnable task) throws InterruptedException {
        final CountDownLatch start = new CountDownLatch(1); //构造函数值表示需要等待的时间数量
        final CountDownLatch end = new CountDownLatch(nThread);

        for (int i=0; i<nThread; ++i){
            Thread t = new Thread(() -> {
                try {
                    System.out.println("线程" + Thread.currentThread().getId() + "已启动，等待其他线程启动...");
                    start.await();
                    try {
                        task.run();
                    }finally {
                        System.out.println("线程" + Thread.currentThread().getId() + "任务完成");
                        end.countDown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }

        start.countDown();  //countDown()方法会是初始化时的计数减一，只要计数不为0，那么线程会一直阻塞在await()方法中
        System.out.println("所有线程启动完成");
        end.await();
        System.out.println("所有线程任务执行完成");
    }

    @Override
    public void run() {
        System.out.println("线程" + Thread.currentThread().getId() + "执行任务ing...");
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatchTest test = new CountDownLatchTest();
        test.latch(10, test);
    }
}
