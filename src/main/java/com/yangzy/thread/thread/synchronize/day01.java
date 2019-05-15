package com.yangzy.thread.thread.synchronize;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/***
 * 虽然多线程编程极大地提高了效率，但是也会带来一定的隐患。
 */
public class day01 {

    /***
     * 什么时候会出现线程安全问题？
     * 在单线程中不会出现线程安全问题，而在多线程中，有可能就会出现多个线程访问同一个资源。
     */

    /***
     * 如何解决线程安全问题？
     * 基本上所有的并发模式在解决线程安全问题时，都采用"序列化访问临界资源"的方案，即在同一时刻，只能有一个线程访问临界资源，也称作同步互斥访问。
     * 通常来说，是在访问临界资源的代码前面加上一个锁，当访问完临界资源后释放锁，让其他线程继续访问。
     * 在Java中，提供了两种方式来实现同步互斥访问：synchronized和Lock。
     */

    /***
     * synchronized同步发饭方法或同步块
     * 在了解synchronized关键字的使用方法之前，我们先来看一个概念：互斥锁，顾名思义：能到达到互斥访问目的的锁。
     * 举个简单的例子：如果对临界资源加上互斥锁，当一个线程在访问该临界资源时，其他线程便只能等待。
     * 在Java中，每一个对象都拥有一个锁标记（monitor），也称为监视器，多线程同时访问某个对象时，线程只有获取了该对象的锁才能访问。
     * 在Java中，可以使用synchronized关键字来标记一个方法或者代码块，当某个线程调用该对象的synchronized方法或者访问synchronized代码块时，这个线程便获得了该对象的锁。
     * 其他线程暂时无法访问这个方法，只有等待这个方法执行完毕或者代码块执行完毕，这个线程才会释放该对象的锁，其他线程才能执行这个方法或者代码块。
     * 下面举几个简单例子
     */

    /***
     * 同步方法
     */
    private List<Integer> list = new ArrayList<Integer>(10);

    @Test
    public void method1(){
        day01 day01 = new day01();

        new Thread(){
            public void run(){
                day01.insert(Thread.currentThread());
            }
        }.start();

        new Thread(){
            public void run(){
                day01.insert(Thread.currentThread());
            }
        }.start();
    }

    /***
     * 对方法加上锁之后，只有当一个线程插入完后，另一个线程才能开始插入
     * @param thread
     */
    public synchronized void insert(Thread thread){
        for (int i=0; i<5; ++i){
            System.out.println(thread.getName() +"插入数据"+ i);
            list.add(i);
        }
    }

    /***
     * 关于synchronized修饰的方法，有几点需要注意：
     * 1.当一个线程正在访问一个对象的synchronized方法，那么其他线程不能访问该对象的其他synchronized方法。
     * 这个原因很简单，因为一个对象只有一把锁，当一个线程获取了该对象的锁之后，其他线程无法获取该对象的锁，所以无法访问该对象的其他synchronized方法。
     * 2.当一个线程正在访问一个对象的synchronized方法，那么其他线程能访问该对象的非synchronized方法。
     * 这个原因很简单，访问非synchronized方法不需要获得该对象的锁，假如一个方法没用synchronized关键字修饰，说明它不会使用到临界资源，那么其他线程是可以访问这个方法的。
     * 3.如果一个线程A需要访问对象object1的synchronized方法fun1，另外一个线程B需要访问对象object2的synchronized方法fun1，
     * 即使object1和object2是同一类型，也不会产生线程安全问题，因为他们访问的是不同的对象，所以不存在互斥问题。
     */

    /***
     * 同步块
     * 比如将上面的insert改成一下两种形式
     */
    public void insert1(Thread thread){
        synchronized (this){
            for (int i=0; i<5; ++i){
                System.out.println(thread.getName() +"插入数据"+ i);
                list.add(i);
            }
        }
    }

    Object object = new Object();
    public void insert2(Thread thread){
        synchronized (object){
            for (int i=0; i<5; ++i){
                System.out.println(thread.getName() +"插入数据"+ i);
                list.add(i);
            }
        }
    }

    /***
     * 从上面可以看出，synchronized代码块使用起来比synchronized方法要灵活得多。
     * 因为也许一个方法中只有一部分代码只需要同步，如果此时对整个方法用synchronized进行同步，会影响程序执行效率。
     * 而使用synchronized代码块就可以避免这个问题，synchronized代码块可以实现只对需要同步的地方进行同步。
     */

    /***
     * 另外，每个类也会有一个锁，它可以用来控制对static数据成员的并发访问。
     * 并且如果一个线程执行一个对象的非static synchronized方法，另外一个线程需要执行这个对象所属类的static synchronized方法，此时不会发生互斥现象。
     * 因为访问static synchronized方法占用的是类锁，而访问非static synchronized方法占用的是对象锁，所以不存在互斥现象。
     */
}
