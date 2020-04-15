package com.yangzy.thread.thread.threadLocal;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/***
 * ThreadLocal解析
 */
public class day01<T> {

    /***
     * 对ThreadLocal的理解
     * ThreadLocal，很多地方叫做线程本地变量，也有些地方叫做线程本地存储，其实意思差不多。
     * 可能很多朋友都知道ThreadLocal为变量在每个线程中都创建了一个副本，那么每个线程可以访问自己内部的副本变量。
     * 这句话从字面上看起来很容易理解，但是真正理解并不是那么容易。我们先来看个例子：
     */

    /***
     * 假设有这样一个数据库链接管理类，这段代码在单线程中使用是没有任何问题的，但是如果在多线程中使用呢？很显然，在多线程中使用会存在线程安全问题。
     * 第一，这里面的2个方法都没有进行同步，很可能在openConnection方法中会多次创建connect。
     * 第二，由于connect是共享变量，那么必然在调用connect的地方需要使用到同步来保障线程安全，因为很可能一个线程在使用connect进行数据库操作，而另外一个线程调用closeConnection关闭链接。
     *
     * 所以出于线程安全的考虑，必须将这段代码的两个方法进行同步处理，并且在调用connect的地方需要进行同步处理。
     * 但这样将会大大影响程序执行效率，因为一个线程在使用connect进行数据库操作的时候，其他线程只有等待。
     *
     * 那么大家来仔细分析一下这个问题，这地方到底需不需要将connect变量进行共享？
     * 事实上，是不需要的。假如每个线程中都有一个connect变量，各个线程之间对connect变量的访问实际上是没有依赖关系的，即一个线程不需要关心其他线程是否对这个connect进行了修改的。
     */
    static class ConnectionManager{

        private static Connection connection = null;

        public static Connection openConnection() throws SQLException {
            if(connection == null){
                connection = DriverManager.getConnection("192.168.1.1");
            }
            return connection;
        }

        public static void closeConnection() throws SQLException {
            if(connection!=null)
                connection.close();
        }
    }

    /***
     * 到这里，可能会有朋友想到，既然不需要在线程之间共享这个变量，可以直接这样处理，在每个需要使用数据库连接的方法中具体使用时才创建数据库链接，然后在方法调用完毕再释放这个连接。
     * 这样处理确实也没有任何问题，由于每次都是在方法内部创建的连接，那么线程之间自然不存在线程安全问题。
     * 但是这样会有一个致命的影响：导致服务器压力非常大，并且严重影响程序执行性能。由于在方法中需要频繁地开启和关闭数据库连接，这样不尽严重影响程序执行效率，还可能导致服务器压力巨大。
     */
    class Dao{
        public void insert() throws SQLException {
            ConnectionManager connectionManager = new ConnectionManager();
            Connection connection = connectionManager.openConnection();

            //使用connection进行操作

            connectionManager.closeConnection();
        }
    }

    /***
     * 那么这种情况下使用ThreadLocal是再适合不过的了。
     * 因为ThreadLocal在每个线程中对该变量会创建一个副本，即每个线程内部都会有一个该变量，且在线程内部任何地方都可以使用，线程之间互不影响，这样一来就不存在线程安全问题，也不会严重影响程序执行性能。
     * 但是要注意，虽然ThreadLocal能够解决上面说的问题，但是由于在每个线程中都创建了副本，所以要考虑它对资源的消耗，比如内存的占用会比不使用ThreadLocal要大。
     */

    /***
     * ThreadLocal怎么实现的可以自己去看看，下面我们来看看ThreadLocal在每个线程中创建副本变量的效果。
     */
    private ThreadLocal<Long> longThreadLocal = new ThreadLocal<Long>();
    private ThreadLocal<String> stringThreadLocal = new ThreadLocal<String>();

    public void set(){
        longThreadLocal.set(Thread.currentThread().getId());
        stringThreadLocal.set(Thread.currentThread().getName());
    }

    public long getLong(){
        return longThreadLocal.get();
    }

    public String getString(){
        return stringThreadLocal.get();
    }

    @Test
    public void method1() throws InterruptedException {
        final day01 day01 = new day01();

        day01.set();
        System.out.println(day01.getLong());
        System.out.println(day01.getString());

        Thread thread = new Thread(){
            @Override
            public void run() {
                day01.set();
                System.out.println(day01.getLong());
                System.out.println(day01.getString());
            }
        };
        thread.start();
        thread.join();

        System.out.println(day01.getLong());
        System.out.println(day01.getString());
    }
    /***
     * 这段代码输出结果可以看出，在main线程中和thread线程中，longLocal保存的副本值和stringLocal保存的副本值都不一样。
     * 最后一次在main线程再次打印副本值是为了证明在main线程中和thread1线程中的副本值确实是不同的且相互不会影响。
     */

    /***
     * 总结
     * 1.实际的通过ThreadLocal创建的副本是存储在每个线程自己的threadLocals中的。
     * 2.为何threadLocals的类型ThreadLocalMap的键值为ThreadLocal对象，因为每个线程中可有多个threadLocal变量。
     * 3.在进行get之前，必须先set，否则会报空指针异常。
     */

    /***
     * ThreadLocal的应用场景
     * 最常见的ThreadLocal使用场景为 用来解决 数据库连接、Session管理等。
     */

}
