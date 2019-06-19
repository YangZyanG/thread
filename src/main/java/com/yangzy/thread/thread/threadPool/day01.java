package com.yangzy.thread.thread.threadPool;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/***
 * 我们使用线程的时候就去创建一个线程，这样实现起来非常简便，但是就会有一个问题：
 * 如果并发的线程数量很多，并且每个线程都是执行一个时间很短的任务就结束了，这样频繁创建线程就会大大降低系统的效率，因为频繁创建线程和销毁线程需要时间。
 * 那么有没有一种办法使得线程可以复用，就是执行完一个任务，并不被销毁，而是可以继续执行其他的任务？
 * 在Java中可以通过线程池来达到这样的效果。今天我们就来详细讲解一下Java的线程池，首先我们从最核心的ThreadPoolExecutor类中的方法讲起，
 * 然后再讲述它的实现原理，接着给出了它的使用示例，最后讨论了一下如何合理配置线程池的大小。
 */
public class day01 {

    /***
     * Java中的ThreadPoolExecutor类
     * java.uitl.concurrent.ThreadPoolExecutor类是线程池中最核心的一个类，因此如果要透彻地了解Java中的线程池，必须先了解这个类。
     * 在ThreadPoolExecutor类中提供了四个构造方法：
     * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,
     *             BlockingQueue<Runnable> workQueue);
     *
     * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,
     *             BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory);
     *
     * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,
     *             BlockingQueue<Runnable> workQueue,RejectedExecutionHandler handler);
     *
     * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit,
     *         BlockingQueue<Runnable> workQueue,ThreadFactory threadFactory,RejectedExecutionHandler handler);
     * 事实上，通过观察每个构造器的源码具体实现，发现前面三个构造器都是调用的第四个构造器进行的初始化工作。
     *
     * 下面解释下一下构造器中各个参数的含义：
     * corePoolSize：核心池的大小，这个参数跟后面讲述的线程池的实现原理有非常大的关系。
     * 在创建了线程池后，默认情况下，线程池中并没有任何线程，而是等待有任务到来才创建线程去执行任务。
     * 除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法，从这2个方法的名字就可以看出，是预创建线程的意思，即在没有任务到来之前就创建corePoolSize个线程或者一个线程。
     * 默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中。
     *
     * maximumPoolSize：线程池最大线程数，这个参数也是一个非常重要的参数，它表示在线程池中最多能创建多少个线程。
     *
     * keepAliveTime：表示线程没有任务执行时最多保持多久时间会终止。
     * 默认情况下，只有当线程池中的线程数大于corePoolSize时，keepAliveTime才会起作用，直到线程池中的线程数不大于corePoolSize。
     * 即当线程池中的线程数大于corePoolSize时，如果一个线程空闲的时间达到keepAliveTime，则会终止，直到线程池中的线程数不超过corePoolSize。
     * 但是如果调用了allowCoreThreadTimeOut(boolean)方法，在线程池中的线程数不大于corePoolSize时，keepAliveTime参数也会起作用，直到线程池中的线程数为0。
     *
     * unit：参数keepAliveTime的时间单位，有7种取值。
     * TimeUnit.DAYS;               //天
     * TimeUnit.HOURS;             //小时
     * TimeUnit.MINUTES;           //分钟
     * TimeUnit.SECONDS;           //秒
     * TimeUnit.MILLISECONDS;      //毫秒
     * TimeUnit.MICROSECONDS;      //微妙
     * TimeUnit.NANOSECONDS;       //纳秒
     *
     * workQueue：一个阻塞队列，用来存储等待执行的任务，这个参数的选择也很重要，会对线程池的运行过程产生重大影响。
     * 一般来说，这里的阻塞队列有以下几种选择：
     * ArrayBlockingQueue;
     * LinkedBlockingQueue;
     * SynchronousQueue;
     * ArrayBlockingQueue和PriorityBlockingQueue使用较少，一般使用LinkedBlockingQueue和Synchronous。线程池的排队策略与BlockingQueue有关。
     *
     * threadFactory：线程工厂，主要用来创建线程。
     *
     * handler：表示当拒绝处理任务时的策略，有以下四种取值：
     * ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
     * ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
     * ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
     * ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
     */

    /***
     * 线程池的状态
     * 在ThreadPoolExecutor中定义了一个volatile变量，另外定义了几个static final变量表示线程池的各个状态：
     * volatile int runState;
     * static final int RUNNING    = 0;
     * static final int SHUTDOWN   = 1;
     * static final int STOP       = 2;
     * static final int TERMINATED = 3;
     * runState表示当前线程池的状态，它是一个volatile变量用来保证线程之间的可见性。
     * 下面的几个static final变量表示runState可能的几个取值。
     * 当创建线程池后，初始时，线程池处于RUNNING状态。
     * 如果调用了shutdown()方法，则线程池处于SHUTDOWN状态，此时线程池不能够接受新的任务，它会等待所有任务执行完毕。
     * 如果调用了shutdownNow()方法，则线程池处于STOP状态，此时线程池不能接受新的任务，并且会去尝试终止正在执行的任务。
     * 当线程池处于SHUTDOWN或STOP状态，并且所有工作线程已经销毁，任务缓存队列已经清空或执行结束后，线程池被设置为TERMINATED状态。
     */

    /***
     * 任务的执行
     * 在了解将任务提交给线程池到任务执行完毕整个过程之前，我们先来看一下ThreadPoolExecutor类中其他的一些比较重要成员变量：
     * private final BlockingQueue<Runnable> workQueue;              //任务缓存队列，用来存放等待执行的任务
     * private final ReentrantLock mainLock = new ReentrantLock();   //线程池的主要状态锁，对线程池状态（比如线程池大小、runState等）的改变都要使用这个锁
     * private final HashSet<Worker> workers = new HashSet<Worker>();  //用来存放工作集
     *
     * private volatile long  keepAliveTime;    //线程存活时间
     * private volatile boolean allowCoreThreadTimeOut;   //是否允许为核心线程设置存活时间
     * private volatile int   corePoolSize;     //核心池的大小（即线程池中的线程数目大于这个参数时，提交的任务会被放进任务缓存队列）
     * private volatile int   maximumPoolSize;   //线程池最大能容忍的线程数
     *
     * private volatile int   poolSize;       //线程池中当前的线程数
     *
     * private volatile RejectedExecutionHandler handler; //任务拒绝策略
     *
     * private volatile ThreadFactory threadFactory;   //线程工厂，用来创建线程
     *
     * private int largestPoolSize;   //用来记录线程池中曾经出现过的最大线程数
     *
     * private long completedTaskCount;   //用来记录已经执行完毕的任务个数
     */

    /***
     * 任务提交给线程池之后到被执行的整个过程
     * 1.首先，要清楚corePoolSize和maximumPoolSize的含义。
     * 2.其次，要知道Worker是用来起到什么作用的。
     * 3.要知道任务提交给线程池之后的处理策略，这里总结一下主要有4点。
     *
     * 过程总结：
     * 1.如果当前线程池中的线程数目小于corePoolSize，则每来一个任务，就会创建一个线程去执行这个任务。
     * 2.如果当前线程池中的线程数目>=corePoolSize，则每来一个任务，会尝试将其添加到任务缓存队列当中。
     * 若添加成功，则该任务会等待空闲线程将其取出去执行，若添加失败（一般来说是任务缓存队列已满），则会尝试创建新的线程去执行这个任务。
     * 3.如果当前线程池中的线程数目达到maximumPoolSize，则会采取任务拒绝策略进行处理。
     * 4.如果线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直至线程池中的线程数目不大于corePoolSize。
     * 如果允许为核心池中的线程设置存活时间，那么核心池中的线程空闲时间超过keepAliveTime，线程也会被终止。
     */

    /***
     * 线程池中的线程初始化
     * 默认情况下，创建线程池之后，线程池中是没有线程的，需要提交任务之后才会创建线程。
     * 在实际中如果需要线程池创建之后立即创建线程，可以通过以下两个方法办到：
     * prestartCoreThread()：初始化一个核心线程。
     * prestartAllCoreThreads()：初始化所有核心线程。
     */

    /***
     * 任务缓存队列及排队策略
     * 在前面我们多次提到了任务缓存队列，即workQueue，它用来存放等待执行的任务。workQueue的类型为BlockingQueue<Runnable>，通常可以取下面三种类型：
     * 1.ArrayBlockingQueue：基于数组的先进先出队列，此队列创建时必须指定大小。
     * 2.LinkedBlockingQueue：基于链表的先进先出队列，如果创建时没有指定此队列大小，则默认为Integer.MAX_VALUE。
     * 3.synchronousQueue：这个队列比较特殊，它不会保存提交的任务，而是将直接新建一个线程来执行新来的任务。
     */

    /***
     * 任务拒绝策略
     * 当线程池的任务缓存队列已满并且线程池中的线程数目达到maximumPoolSize，如果还有任务到来就会采取任务拒绝策略，通常有以下四种策略：
     * 1.ThreadPoolExecutor.AbortPolicy:丢弃任务并抛出RejectedExecutionException异常。
     * 2.ThreadPoolExecutor.DiscardPolicy：也是丢弃任务，但是不抛出异常。
     * 3.ThreadPoolExecutor.DiscardOldestPolicy：丢弃队列最前面的任务，然后重新尝试执行任务（重复此过程）
     * 4.ThreadPoolExecutor.CallerRunsPolicy：由调用线程处理该任务
     */

    /***
     * 线程池应用场景
     * 对于线程的创建和销毁的时间远大于线程执行时间的场合适用，如果需要控制线程执行时间，也可以考虑适用线程池。
     */

    /***
     * 常用的4种线程池封装
     * 1.newCachedThreadPool
     * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
     * 不推荐理由：最大线程数无限大。
     *
     * 2.newFixedThreadPool
     * 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。
     * 不推荐理由：虽然线程池中的线程数目是有限且固定的，但是阻塞队列的最大长度为Integer.MAX_VALUE。
     *
     * 3.newScheduledThreadPool
     * 创建一个定长线程池，支持定时及周期性任务执行。
     * 不推荐理由：线程池中的最大线程数用的Integer.MAX_VALUE。阻塞队列是无界队列。
     *
     * 4.newSingleThreadExecutor
     * 创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
     * 不推荐理由：虽然线程池的核心线程数和最大线程数都为1，但是阻塞队列的最大长度为Integer.MAX_VALUE。
     */

    /***
     * 线程池如何合理的设置大小
     * 任务一般分为：CPU密集型、IO密集型、混合型，对于不同类型的任务需要分配不同大小的线程池
     * 1.CPU密集型
     * 即业务大部分都是逻辑判断、运算等操作，因为CPU密集型任务CPU的使用率很高，若开过多的线程，只能增加线程上下文的切换次数，带来额外的开销。
     * 尽量使用较小的线程池，一般Cpu核心数+1。
     * 2.IO密集型
     * 涉及到网络、磁盘IO的任务都是IO密集型任务，这类任务的特点是CPU消耗很少，任务的大部分时间都在等待IO操作完成（因为IO的速度远远低于CPU和内存的速度）。
     * 对于IO密集型任务，任务越多，CPU效率越高，但也有一个限度。
     * 我们现在做的开发大部分都是WEB应用，涉及到大量的网络传输。
     * 不仅如此，与数据库，与缓存间的交互也涉及到IO，一旦发生IO，线程就会处于等待状态，当IO结束，数据准备好后，线程才会继续执行。
     * 可以使用较大的线程池，一般CPU核心数*2。
     */

    public static void main(String[] args) {
        Queue<Integer> queue = new LinkedBlockingQueue<Integer>();
        for (int i=0; i<1000; ++i){
            queue.add(i);
        }

        DataQueue data = new DataQueue();
        data.setQueue(queue);

        ThreadPoolExecutor executor = Pool.getInstance();
        ParseData parseData = new ParseData(data);

        for (int i=0; i<10; ++i){
            executor.execute(parseData);
        }
    }
}
