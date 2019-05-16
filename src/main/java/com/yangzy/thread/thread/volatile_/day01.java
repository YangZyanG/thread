package com.yangzy.thread.thread.volatile_;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/***
 * volatile关键字解析
 */
public class day01 {

    /***
     * 并发编程中的3个概念
     * 在并发编程中，我们通常会遇到以下三个问题：原子性问题、可见性问题、有序性问题，我们先看具体看一下这三个概念。
     */

    /***
     * 1.原子性
     * 原子性：即一个操作或者多个操作 要么全部执行并且执行的过程不会被任何因素打断，要么就都不执行。
     * 举个最简单的例子，大家想一下假如为一个32位的变量赋值过程不具备原子性的话，会发生什么后果？
     * i = 9;
     * 假若一个线程执行到这个语句时，我暂且假设为一个32位的变量赋值包括两个过程：为低16位赋值，为高16位赋值。
     * 那么就可能发生一种情况：当将低16位数值写入之后，突然被中断，而此时又有一个线程去读取i的值，那么读取到的就是错误的数据。
     */

    /***
     * 2.可见性
     * 可见性是指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值。
     *
     * //线程1执行的代码
     * int i = 0;
     * i = 10;
     *
     * //线程2执行的代码
     * j = i;
     *
     * 假若执行线程1的是CPU1，执行线程2的是CPU2。
     * 由上面的分析可知，当线程1执行 i =10这句时，会先把i的初始值加载到CPU1的高速缓存中，然后赋值为10，那么在CPU1的高速缓存当中i的值变为10了，却没有立即写入到主存当中。
     * 此时线程2执行 j = i，它会先去主存读取i的值并加载到CPU2的缓存当中，注意此时内存当中i的值还是0，那么就会使得j的值为0，而不是10。
     * 这就是可见性问题，线程1对变量i修改了之后，线程2没有立即看到线程1修改的值。
     */

    /***
     * 3.有序性
     * 有序性：即程序执行的顺序按照代码的先后顺序执行。举个简单的例子，看下面这段代码：
     *
     * int i = 0;
     * boolean flag = false;
     * i = 1;                //语句1
     * flag = true;          //语句2
     *
     * 上面代码定义了一个int型变量，定义了一个boolean类型变量，然后分别对两个变量进行赋值操作。
     * 从代码顺序上看，语句1是在语句2前面的，那么JVM在真正执行这段代码的时候会保证语句1一定会在语句2前面执行吗？
     * 不一定，为什么呢？这里可能会发生指令重排序（Instruction Reorder）。
     *
     * 下面解释一下什么是指令重排序，一般来说，处理器为了提高程序运行效率，可能会对输入代码进行优化。
     * 它不保证程序中各个语句的执行先后顺序同代码中的顺序一致，但是它会保证程序最终执行结果和代码顺序执行的结果是一致的。
     * 比如上面的代码中，语句1和语句2谁先执行对最终的程序结果并没有影响，那么就有可能在执行过程中，语句2先执行而语句1后执行。
     *
     * 但是要注意，虽然处理器会对指令进行重排序，但是它会保证程序最终结果会和代码顺序执行结果相同，那么它靠什么保证的呢？再看下面一个例子：
     *
     * int a = 10;    //语句1
     * int r = 2;    //语句2
     * a = a + 3;    //语句3
     * r = a*a;     //语句4
     *
     * 这段代码有可能的一个执行顺序是： 语句2   语句1    语句3   语句4
     * 那么可不可能是这个执行顺序呢： 语句2   语句1    语句4   语句3
     * 不可能，因为处理器在进行重排序时是会考虑指令之间的数据依赖性。
     * 如果一个指令Instruction 2必须用到Instruction 1的结果，那么处理器会保证Instruction 1会在Instruction 2之前执行。即语句4依赖了语句3，那么3肯定在4前面执行。
     *
     * 虽然重排序不会影响单个线程内程序执行的结果，但是多线程呢？下面看一个例子：
     *
     * //线程1:
     * context = loadContext();   //语句1
     * inited = true;             //语句2
     *
     * //线程2:
     * while(!inited ){
     *   sleep()
     * }
     * doSomethingWithConfig(context);
     *
     * 上面代码中，由于语句1和语句2没有数据依赖性，因此可能会被重排序。
     * 假如发生了重排序，在线程1执行过程中先执行语句2，而此是线程2会以为初始化工作已经完成，那么就会跳出while循环，去执行doSomethingwithconfig(context)方法，而此时context并没有被初始化，就会导致程序出错。
     * 从上面可以看出，指令重排序不会影响单个线程的执行，但是会影响到线程并发执行的正确性。
     *
     */

    /***
     * 总结
     * 要想并发程序正确地执行，必须要保证原子性、可见性以及有序性。只要有一个没有被保证，就有可能会导致程序运行不正确。
     */

    /***
     * volatile关键字的两层语义
     * 一旦一个共享变量（类的成员变量、类的静态成员变量）被volatile修饰之后，那么就具备了两层语义：
     * 1.保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。
     * 2.禁止进行指令重排序。
     */

    /***
     * volatile能保证原子性吗？
     * volatile能保证可见性和有序性，那么原子性呢？看下面这个例子：
     * 大家想一下这段程序的输出结果是多少？也许有些朋友认为是10000。但是事实上运行它会发现每次运行结果都不一致，都是一个小于10000的数字。
     * 可能有的朋友就会有疑问，不对啊，上面是对变量inc进行自增操作，由于volatile保证了可见性，那么在每个线程中对inc自增完之后，在其他线程中都能看到修改后的值啊，所以有10个线程分别进行了1000次操作，那么最终inc的值应该是1000*10=10000。
     * 这里面就有一个误区了，volatile关键字能保证可见性没有错，但是上面的程序错在没能保证原子性。可见性只能保证每次读取的是最新的值，但是volatile没办法保证对变量的操作的原子性。
     * 在前面已经提到过，自增操作是不具备原子性的，它包括读取变量的原始值、进行加1操作、写入工作内存。那么就是说自增操作的三个子操作可能会分割开执行。
     * 线程1对变量进行读取操作之后，被阻塞了的话，并没有对inc值进行修改。然后虽然volatile能保证线程2对变量inc的值读取是从内存中读取的，但是线程1没有进行修改，所以线程2根本就不会看到修改的值。
     */

    public volatile int inc = 0;

    public void increase(){
        ++inc;
    }

    public static void main(String[] args) {
        final day01 day01 = new day01();

        for (int i=0; i<10; ++i){
            new Thread(){
                @Override
                public void run() {
                    for (int j=0; j<1000; ++j){
                        day01.increase();
                    }
                }
            }.start();
        }

        //保证前面的线程都执行完
        while (Thread.activeCount() > 2)
            Thread.yield();

        System.out.println(day01.inc);
    }

    /***
     * 为了保证原子性，以下对increase()方法的修改都可以实现原子性
     */

    public synchronized void increase1(){
        ++inc;
    }

    Lock lock = new ReentrantLock();
    public void increase2(){
        lock.lock();
        try{
            ++inc;
        }finally {
            lock.unlock();
        }
    }

    AtomicInteger aInc = new AtomicInteger(0);
    public void increase3(){
        aInc.getAndIncrement();
    }
}