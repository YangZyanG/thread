package com.yangzy.thread.thread.bisuo;

import java.util.concurrent.Semaphore;

/**
 * @author yangziyang
 * @since 2020-09-11
 */
public class SemaphoreTest implements Runnable{

    private final Semaphore semaphore;

    public SemaphoreTest(int nAcquire){
        semaphore = new Semaphore(nAcquire);
    }

    public void task(int nThread, Runnable task) throws InterruptedException {
        for (int i=0; i<nThread; ++i){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        semaphore.acquire();
                        task.run();
                        System.out.println("线程" + Thread.currentThread().getId() + "任务执行完成");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        semaphore.release();
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("线程" + Thread.currentThread().getId() + "开始执行任务...");
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SemaphoreTest test = new SemaphoreTest(3);
        test.task(20, test);
    }
}
