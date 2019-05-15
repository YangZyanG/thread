package com.yangzy.thread.thread.createThread;

import org.junit.Test;

/***
 * Java中如何创建线程
 * 在java中如果要创建线程的话，一般有两种方式
 * 1.继承Thread类
 * 2.实现Runnable接口
 */
public class day01 {

    /***
     * 继承Thread类
     * 继承Thread类的话，必须重写run()方法，在run()方法中定义需要执行的任务。
     */
    class MyThread extends Thread{

        private int num = 0;

        public MyThread(){
            ++num;
        }

        @Override
        public void run(){
            System.out.println("主动创建的第"+ num +"个线程");
        }
    }

    /***
     * 创建好自己的线程类之后，就可以创建线程对象了，然后通过start()方法去启动线程。
     */
    @Test
    public void method1(){
        MyThread myThread = new MyThread();
        myThread.start();
    }

    /***
     * 实现Runnable接口
     */
    class MyRunnable implements Runnable{

        private int num = 0;

        public MyRunnable(){
            ++num;
        }

        @Override
        public void run() {
            System.out.println("主动创建的第"+ num +"个线程");
        }
    }

    /***
     * Runnable的中文意思是"任务"
     * 顾名思义，通过实现Runnable接口，我们定义了一个子任务，然后将子任务交给Thread去执行。
     */
    @Test
    public void method2(){
        MyRunnable myRunnable = new MyRunnable();
        Thread thread = new Thread(myRunnable);
        thread.start();
    }
}
