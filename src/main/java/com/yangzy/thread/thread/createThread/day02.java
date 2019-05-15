package com.yangzy.thread.thread.createThread;

import org.junit.Test;

/***
 * Thread类的使用
 */
public class day02 {

    /***
     * 线程的状态
     * 线程从创建到最终消亡，要经历若干个状态。
     * 一般来说，线程包括以下几个状态：创建(new)、就绪(runnable)、运行(running)、阻塞(blocked)、time waiting、waiting、消亡(dead)。
     *
     * 当需要新起一个线程来执行子任务时，就创建一个线程。但是线程创建之后不会立即进入就绪状态，因为线程的运行需要一些状态(比如为线程分配私有空间)，条件满足后，才进入就绪状态。
     * 当线程进入就绪状态后，不代表就能立刻获取CPU分配的执行时间，也许CPU正在执行其他任务，当获得CPU执行时间之后，线程便进入了运行状态。
     * 线程在运行状态过程中，可能有多个原因导致导致当前线程不继续运行下去，比如用户主动让线程睡眠(time waiting)、用户主动让线程等待(waiting)或者被同步块阻塞(blocked)。
     * 当线程突然中断或者子任务运行完毕，线程就会被消亡。
     */

    /***
     * 上下文切换
     * 对于单核CPU来说(对于多核CPU，此处就理解为一个核)，CPU一个时刻只能运行一个线程，当在运行一个线程中转去运行另一个线程，这个叫做线程上下文切换。
     * 由于当前线程可能没执行完毕，所以需要记录当前线程的运行状态，所以上下文切换过程中会记录程序计数器、CPU寄存器状态等数据。
     */

    /***
     * Thread类中的方法
     *
     * start()方法
     * 用来启动一个线程，当调用start()方法后，系统才会开启一个新的线程来执行用户定义的子任务
     *
     * run()方法
     * run()不需要用户来调用，调用start()方法后，当线程获得了CPU执行时间，便进入run()方法体去执行具体的任务。
     */

    /***
     * sleep()方法
     * sleep方法有两个重载版本：
     * sleep(long millis)     //参数为毫秒
     * sleep(long millis,int nanoseconds)    //第一参数为毫秒，第二个参数为纳秒
     * sleep相当于让线程睡眠，交出CPU，让CPU去执行其他任务。
     * 但是有一点要非常注意，sleep方法不会释放锁，也就是说如果当前线程拥有某个对象锁时，即使调用了sleep方法，其他线程也无法访问这个对象。如下：
     */
    private int i = 0;
    private Object object = new Object();

    /***
     * 从结果可以看出，当Thread-0睡眠后，Thread-1并没有执行任务，而是等Thread-0执行完后，释放了锁，Thread-1才开始执行。
     * @param args
     */
    public static void main(String[] args) {
        day02 day02 = new day02();
        Method1 method1 = day02.new Method1();
        Thread thread1 = new Thread(method1);
        Thread thread2 = new Thread(method1);
        thread1.start();
        thread2.start();
    }

    class Method1 implements Runnable{

        @Override
        public void run() {

            synchronized (object){
                ++i;
                System.out.println("i="+i);

                try {
                    System.out.println("线程"+ Thread.currentThread().getName() +"进入睡眠状态");
                    Thread.currentThread().sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("线程"+ Thread.currentThread().getName() +"睡眠状态结束");
                ++i;
                System.out.println("i="+i);
            }
        }
    }

    /***
     * yield()方法
     * 调用yield方法会让当前线程交出CPU权限，让CPU去执行其他的线程。它跟sleep方法类似，同样不会释放锁。
     * 但是yield不能控制具体的交出CPU的时间，另外，yield方法只能让拥有相同优先级的线程有获取CPU执行时间的机会。
     * 注意，调用yield方法并不会让线程进入阻塞状态，而是让线程重回就绪状态，它只需要等待重新获取CPU执行时间，这一点是和sleep方法不一样的。
     */

    /***
     * join()方法
     *
     */

}
