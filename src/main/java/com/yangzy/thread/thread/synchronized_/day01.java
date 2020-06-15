package com.yangzy.thread.thread.synchronized_;

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

    /***
     * 锁的作用范围
     * 首先要明确一点，锁是加在对象上的，我们是在对象上加锁。
     * 1.修饰非静态方法：
     * 锁的是当前对象this。
     * 2.修饰静态方法：
     * 锁的是当前类对象，一切皆对象。
     * 3.修饰代码块：
     * 锁的是指定对象
     *
     * 当锁修饰方法的时候，系统底层是通过检查方法上的ACC_SYNCHRONIZED标识来进行加锁的，如果有该标识，则加锁。
     * 当锁修饰代码块的时候，系统底层是通过monitorenter和monitorexit来实现加锁和释放锁的。
     */

    /***
     * 对象的构成
     * 对象由3部分构成，分别是对象头、对象体和对齐填充。
     * 1.对象头：
     * 对象头由Mark Word、Klass和数组长度（只有数组对象才有）组成，其中Mark Word中储存的是对象的hashcode、锁信息、分代年龄已经GC标识，
     * Klass储存的是指向该对象所属的类的指针，JVM就是通过该指针确定当前对象的所属类。
     * 2.对象体：
     * 对象体中储存的是当前对象的实例数据。
     * 3.对齐填充：
     * JVM要求对象占用的空间必须是8的倍数，方便内存分配，所以这部分就是填充不够的部分。
     */

    /***
     * monitor监视器
     * 每个对象都有一个与之关联的monitor监视器，线程通过执行monitorenter和monitorexit这两条指令来实现获取锁和释放锁。
     * 当执行monitorenter时，如果目标锁对象的count计数器为0，那么说明它没有被其他线程所持有。在这个情况下，Java虚拟机会将monitor对象中owner属性设置为当前线程，并且count自增。
     * 在目标对象锁的count不为0的情况下，如果对象锁的持有线程是当前线程，那么JVM会将count自增，否则则需要等待，直到持有线程释放锁。
     * 当执行monitorexit时，JVM会将count自减，当count减为0时，那就代表线程已经释放锁了。
     * 之所以采用这种计数器的方式，是为了允许同一个线程重复获取同一把锁。
     * 举个例子，如果一个Java类中拥有多个synchronized方法，那么这些方法之间相互调用，不管直接的还是间接的，都会涉及对同一把锁的重复加锁操作。
     * 因此，就需要设计这么一个可重入性的特性，来避免编程里的隐式约束。
     */

    /***
     * synchronized加锁过程（JDK1.6之前）
     * 当A、B两个线程竞争锁时，这时线程调度到A执行，A线程就抢先持有了monitor对象，具体步骤为：
     * 1.将monitor中owner属性设置成A线程，count属性+1。
     * 2.因为锁是加在对象上的，所以这时会将对象中的mark word设置为monitor地址，锁标识修改为10。
     * 3.将线程B阻塞放到队列中。
     */

    /***
     * synchronized加锁过程（JDK1.6之后，膨胀过程）
     * 当没有竞争时，默认会使用偏向锁。JVM会利用CAS操作，在对象头上的mark word部分设置线程ID，以表示这个对象偏向于当前线程，所以并不涉及真正的互斥锁。
     * 这样做的假设是基于在很多应用场景中，大部分对象生命周期中最多会被同一个线程锁定，使用偏向锁可以降低竞争开销。
     * 如果有另外的线程试图锁定某个被偏向过的对象，JVM就需要撤销偏向锁，并切换到轻量级锁实现。轻量级锁依赖CAS操作mark word来试图获取锁，如果重试成功，
     * 就使用轻量级锁，否则，就进一步升级为重量级锁。
     *
     * 偏向锁：
     * 1.线程A第一次访问同步块时，先检测对象头Mark Word中的标志位是否为01，依此判断此时对象锁是否处于无锁状态或者偏向锁状态。
     * 2.然后判断偏向锁标志位是否为1，如果不是，则进入轻量级锁逻辑（使用CAS竞争锁），如果是，则进入下一步流程。
     * 3.判断是偏向锁时，检查对象头Mark Word中记录的ThreadID是否是当前线程ID。
     * 如果是，则表明当前线程已经获得对象锁，以后该线程进入同步块时，不需要CAS进行加锁，只会让monitor对象中的count自增。退出同步块时，count自减，ThreadID不做更新。
     * 4.如果对象头Mark Word中ThreadID不是当前线程ID，则进行CAS操作，企图将当前ThreadID替换进Mark Word。
     * 如果当前对象锁状态处于匿名偏向锁状态（可偏向未锁定），则会替换成功，获取到锁，执行同步代码块。
     * 5.如果对象锁已经被其他线程占用，则会替换失败，开始进行偏向锁撤销，这也是偏向锁的特点，一旦出现线程竞争，就会撤销偏向锁。
     * 6.偏向锁的撤销需要等待全局安全点（在该状态下所有线程都是暂停的，没有字节码执行）。
     * 暂停持有偏向锁的线程，检查持有偏向锁的线程状态，如果线程还存活，则检查线程是否在执行同步代码块中的代码，如果是，则升级为轻量级锁，进行CAS竞争锁。
     * 7.如果持有偏向锁的线程未存活，或者持有偏向锁的线程未在执行同步代码块中的代码，则进行校验是否允许重偏向（根据epoch值判断）。
     * 如果不允许重偏向，则撤销偏向锁，将Mark Word设置为无锁状态，然后升级为轻量级锁，进行CAS竞争锁。
     * 8.如果允许重偏向，设置为匿名偏向锁状态，CAS将偏向锁重新指向线程A。
     *
     * 偏向锁在JDK 6及以后的JVM里是默认启用的。可以通过JVM参数关闭偏向锁：-XX:-UseBiasedLocking=false，关闭之后程序默认会进入轻量级锁状态。
     * 偏向锁代码的验证查看BiasedLock。
     */

}
