package com.yangzy.thread.thread.synchronized_;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author yangziyang
 * @since 2020-06-15
 * 偏向锁
 */
public class BiasedLock {

    static BiasedLockDemo demo;

    public static void main(String[] args) {
        demo = new BiasedLockDemo();
        System.out.println("befor lock");
        //无锁：偏向锁
        System.out.println(ClassLayout.parseInstance(demo).toPrintable());
        //计算hashcode后，偏向锁将失效
        demo.hashCode();

        synchronized (demo){
            System.out.println("lock ing");
            System.out.println(ClassLayout.parseInstance(demo).toPrintable());
        }

        System.out.println("after lock");
        System.out.println(ClassLayout.parseInstance(demo).toPrintable());
    }
}

class BiasedLockDemo{

}

/***
 * 偏向锁分析
 * 首先开启偏向锁：-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0 -client -Xmx1024m -Xms1024m
 * 执行main方法，结果如下：
 * befor lock
 * # WARNING: Unable to attach Serviceability Agent. You can try again with escalated privileges. Two options: a) use -Djol.tryWithSudo=true to try with sudo; b) echo 0 | sudo tee /proc/sys/kernel/yama/ptrace_scope
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           05 00 00 00 (00000101 <-(判断后三位) 00000000 00000000 00000000) (5)
 *       4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 *
 * lock ing
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           05 30 00 ea (00000101 00110000 00000000 11101010) (-369086459)
 *       4     4        (object header)                           7f 7f 00 00 (01111111 01111111 00000000 00000000) (32639)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 *
 * after lock
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           05 30 00 ea (00000101 00110000 00000000 11101010) (-369086459)
 *       4     4        (object header)                           7f 7f 00 00 (01111111 01111111 00000000 00000000) (32639)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 *
 * jvm在初始化一个对象的时候，如果没有启用偏向锁延迟，就会去判断这个对象是否可以被偏向，如果可以就是偏向锁，退出同步代码块，还是偏向锁。
 * 具体来说，在线程进行加锁时，如果该锁对象支持偏向锁，那么JVM会通过CAS操作，将当前线程的地址记录在锁对象的标记字段之中，并且将标记字段的最后三位设置为：1 01。
 * 在接下来的运行过程中，每当有线程请求这把锁，JVM只需判断锁对象标记字段中最后三位是否为：1 01，是否包含当前线程的地址，以及epoch值是否和锁对象的类的epoch值相同。如果都满足，那么当前线程持有该偏向锁，可以直接返回。
 *
 * hashcode另偏向锁失效
 * 当线程获取对象锁之前，如果对象计算了hashcode，那么hashcode的会覆盖对象头中的线程地址和epoch值，这时候JVM认为当前对象就不支持偏向锁，会变成轻量锁。
 * 上述代码计算hashcode的注释放开后的结果：
 *
 * befor lock
 * # WARNING: Unable to attach Serviceability Agent. You can try again with escalated privileges. Two options: a) use -Djol.tryWithSudo=true to try with sudo; b) echo 0 | sudo tee /proc/sys/kernel/yama/ptrace_scope
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
 *       4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 *
 * lock ing
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           f8 18 f2 04 (11111000 00011000 11110010 00000100) (82974968)
 *       4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 *
 * after lock
 * com.yangzy.thread.thread.synchronized_.BiasedLockDemo object internals:
 *  OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
 *       0     4        (object header)                           01 f4 f8 b8 (00000001 11110100 11111000 10111000) (-1191644159)
 *       4     4        (object header)                           64 00 00 00 (01100100 00000000 00000000 00000000) (100)
 *       8     4        (object header)                           43 c1 00 f8 (01000011 11000001 00000000 11111000) (-134168253)
 *      12     4        (loss due to the next object alignment)
 * Instance size: 16 bytes
 * Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
 */

/***
 * epoch值概念
 * 首先我们要了解偏量锁升级为轻量级锁的两种情况：
 * 1.很明显，当出现线程竞争该对象锁的时候，自然会升级为轻量级锁。
 * 2.epoch达到40（默认）的时候，JVM认为这个类已经不在适合偏量锁，以后该类的实例会直接设置为轻量级锁。
 *
 * 我们先从偏向锁的撤销讲起。
 * 当请求加锁的线程和锁对象标记字段保持的线程地址不匹配时（而且epoch值相等，如若不等，那么当前线程可以将该锁重偏向至自己），JVM需要撤销该偏向锁。
 * 这个撤销过程非常麻烦，需要等到全局安全点，检测当前持有偏向锁的线程是否存活。
 * 如果存活，检测是否正在执行同步块，如果正在执行，则升级为轻量级锁，CAS竞争锁。如果未执行同步块或者未存活，则进行重偏向。
 *
 * 如果某一类锁对象的总撤销数超过了一个阈值（对应JVM参数-XX:BiasedLockingBulkRebiasThreshold，默认为 20），那么JVM会宣布这个类的偏向锁失效（批量重偏向）。
 * 1.每个class对象会有一个对应的epoch字段，每个处于偏向锁状态对象的Mark Word中也有该字段，其初始值为创建该对象时class中的epoch的值。
 * 2.在宣布某个类的偏向锁失效时，JVM则将该类的epoch值加1，表示之前那一代的偏向锁已经失效，而新设置的偏向锁则需要复制新的epoch值。
 * 3.为了保证当前持有偏向锁并且已加锁的线程不至于因此丢锁，JVM需要遍历所有持有该类对象的线程的Java栈，找出已加锁的实例，并且将它们标记字段中的epoch值加1，该操作需要所有线程处于安全点状态。
 * 4.如果总撤销数超过另一个阈值（对应JVM参数-XX:BiasedLockingBulkRevokeThreshold，默认值为 40），那么JVM会认为这个类已经不再适合偏向锁。
 * 此时，JVM会撤销该类实例的偏向锁，并且在之后的加锁过程中直接为该类实例设置轻量级锁(批量撤销)。
 *
 * 批量重偏向机制是为了解决：一个线程创建了大量对象并执行了初始的同步操作，后来另一个线程也来将这些对象作为锁对象进行操作，这样会导致大量的偏向锁撤销操作。
 * 批量撤销机制是为了解决：在明显多线程竞争剧烈的场景下使用偏向锁是不合适的。
 */
