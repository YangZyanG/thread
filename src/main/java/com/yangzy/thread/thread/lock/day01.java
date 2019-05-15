package com.yangzy.thread.thread.lock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class day01 {

    /***
     * synchronized的缺陷
     * synchronized是java语言的一个关键字
     * 我们了解到如果一个代码块被synchronized修饰了，当一个线程获取了对应的锁，并执行该代码块时，其他线程便只能一直等待。
     * 等待获取锁的线程释放锁，而这里获取锁的线程释放锁只会有两种情况：
     * 1.获取锁的线程执行完了该代码块，然后线程释放对锁的占有。
     * 2.线程执行发生异常，此时JVM会让线程自动释放锁。
     *
     * 那么如果这个获取锁的线程由于要等待IO或者其他原因（比如调用sleep方法）被阻塞了，但是又没有释放锁，其他线程便只能干巴巴地等待，试想一下，这多么影响程序执行效率。
     * 因此就需要有一种机制可以不让等待的线程一直无期限地等待下去（比如只等待一定的时间或者能够响应中断），通过Lock就可以办到。
     *
     * 再举个例子：当有多个线程读写文件时，读操作和写操作会发生冲突现象，写操作和写操作会发生冲突现象，但是读操作和读操作不会发生冲突现象。
     * 但是采用synchronized关键字来实现同步的话，就会导致一个问题：
     * 如果多个线程都只是进行读操作，所以当一个线程在进行读操作时，其他线程只能等待无法进行读操作。
     * 因此就需要一种机制来使得多个线程都只是进行读操作时，线程之间不会发生冲突，通过Lock就可以办到。
     *
     * 总结一下，也就是说Lock提供了比synchronized更多的功能。但是要注意以下几点：
     * 1.Lock不是Java语言内置的，synchronized是Java语言的关键字，因此是内置特性。Lock是一个类，通过这个类可以实现同步访问。
     * 2.Lock和synchronized有一点非常大的不同，采用synchronized不需要用户去手动释放锁，当synchronized方法或者synchronized代码块执行完之后，系统会自动让线程释放对锁的占用。
     * 而Lock则必须要用户去手动释放锁，如果没有主动释放锁，就有可能导致出现死锁现象。
     */

    /***
     * Lock
     * 在Lock中声明了四个方法来获取锁，那么这四个方法有何区别呢？
     */

    /***
     * 首先lock()方法是平常使用得最多的一个方法，就是用来获取锁。如果锁已被其他线程获取，则进行等待。
     * 由于在前面讲到如果采用Lock，必须主动去释放锁，并且在发生异常时，不会自动释放锁。因此一般来说，使用Lock必须在try{}catch{}块中进行，并且将释放锁的操作放在finally块中进行，以保证锁一定被被释放，防止死锁的发生。
     * 通常使用Lock来进行同步的话，是以下面这种形式去使用的：
     * Lock lock = new ...
     * lock.lock();
     * try{
     *
     * }catch(Exception e){
     *
     * }finally{
     *     lock.unlock();
     * }
     */

    private List<Integer> list = new ArrayList<Integer>(10);
    private Lock lock = new ReentrantLock();

    @Test
    public void method1(){
        new Thread(){
            @Override
            public void run() {
                insert1(Thread.currentThread());
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                insert1(Thread.currentThread());
            }
        }.start();
    }

    public void insert1(Thread thread){

        lock.lock();
        try{
            System.out.println(thread.getName() +"得到了锁");
            for (int i=0; i<5; ++i){
                list.add(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.out.println(thread.getName() +"释放了锁");
            lock.unlock();
        }
    }

    /***
     * tryLock()方法是有返回值的，它表示用来尝试获取锁。
     * 如果获取成功，则返回true，如果获取失败（即锁已被其他线程获取），则返回false，也就说这个方法无论如何都会立即返回。在拿不到锁时不会一直在那等待。
     * tryLock(long time, TimeUnit unit)方法和tryLock()方法是类似的。
     * 只不过区别在于这个方法在拿不到锁时会等待一定的时间，在时间期限之内如果还拿不到锁，就返回false。如果如果一开始拿到锁或者在等待期间内拿到了锁，则返回true。
     */
    @Test
    public void method2(){
        new Thread(){
            @Override
            public void run() {
                insert2(Thread.currentThread());
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                insert2(Thread.currentThread());
            }
        }.start();
    }

    public void insert2(Thread thread){
        if(lock.tryLock()){
            try{
                System.out.println(thread.getName() +"得到了锁");
                for (int i=0; i<500; ++i){
                    list.add(i);
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                System.out.println(thread.getName() +"释放了锁");
                lock.unlock();
            }
        }else{
            System.out.println(thread.getName() +"获取锁失败");
        }
    }

    /***
     * lockInterruptibly()方法比较特殊，当通过这个方法去获取锁时，如果线程正在等待获取锁，则这个线程能够响应中断，即中断线程的等待状态。
     * 也就使说，当两个线程同时通过lock.lockInterruptibly()想获取某个锁时，假若此时线程A获取到了锁，而线程B只有在等待，那么对线程B调用threadB.interrupt()方法能够中断线程B的等待过程。
     * 由于lockInterruptibly()的声明中抛出了异常，所以lock.lockInterruptibly()必须放在try块中或者在调用lockInterruptibly()的方法外声明抛出InterruptedException。
     * 注意，当一个线程获取了锁之后，是不会被interrupt()方法中断的。因为本身在前面的文章中讲过单独调用interrupt()方法不能中断正在运行过程中的线程，只能中断阻塞过程中的线程。
     */
    public static void main(String[] args) throws InterruptedException {
        day01 day01 = new day01();
        Method3 method3 = day01.new Method3();
        Thread thread1 = new Thread(method3);
        Thread thread2 = new Thread(method3);
        thread1.start();
        thread2.start();

        Thread.sleep(2000);
        thread2.interrupt();
    }

    public void insert3(Thread thread) throws InterruptedException {
        lock.lockInterruptibly();
        try{
            System.out.println(thread.getName() +"得到了锁");
            thread.sleep(10000);
        }finally {
            System.out.println(thread.getName() +"释放了锁");
            lock.unlock();
        }
    }

    class Method3 implements Runnable{

        @Override
        public void run() {
            try {
                insert3(Thread.currentThread());
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() +"被中断");
            }
        }
    }

    /***
     * ReadWriteLock和ReentrantReadWriteLock
     * 一个用来获取读锁，一个用来获取写锁。也就是说将文件的读写操作分开，分成2个锁来分配给线程，从而使得多个线程可以同时进行读操作。
     * readLock()获取读锁，writeLock()获取写锁。
     * 根据结果可知，读锁是可以多个线程并发进行的，而写锁就只能一个线程同步执行。
     * 不过要注意的是，如果有一个线程已经占用了读锁，则此时其他线程如果要申请写锁，则申请写锁的线程会一直等待释放读锁。
     * 如果有一个线程已经占用了写锁，则此时其他线程如果申请写锁或者读锁，则申请的线程会一直等待释放写锁。
     */
    @Test
    public void method4(){
        new Thread(){
            @Override
            public void run() {
                read(Thread.currentThread());
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                read(Thread.currentThread());
            }
        }.start();

        new Thread(){
            @Override
            public void run() {
                write(Thread.currentThread());
            }
        }.start();
    }

    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void read(Thread thread){
        readWriteLock.readLock().lock();

        try{
            for (int i=0; i<50; i++){
                System.out.println(thread.getName() +"正在进行读操作");
            }
        }finally {
            System.out.println(thread.getName() +"读操作完毕");
            readWriteLock.readLock().unlock();
        }
    }

    public void write(Thread thread){
        readWriteLock.writeLock().lock();

        try{
            for (int i=0; i<10; i++){
                System.out.println(thread.getName() +"正在进行写操作");
            }
        }finally {
            System.out.println(thread.getName() +"写操作完毕");
            readWriteLock.writeLock().unlock();
        }
    }

    /***
     * Lock和synchronized的选择
     * 在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，而当竞争资源非常激烈时（即有大量线程同时竞争），此时Lock的性能要远远优于synchronized。
     * 所以说，在具体使用时要根据适当情况选择。
     */

    /***
     * 锁的相关概念介绍
     * 1.可重入锁
     * 如果锁具备可重入性，则称作为可重入锁。像synchronized和ReentrantLock都是可重入锁，可重入性在我看来实际上表明了锁的分配机制：基于线程的分配，而不是基于方法调用的分配。
     * 举个简单的例子，当一个线程执行到某个synchronized方法时，比如说method1，而在method1中会调用另外一个synchronized方法method2，此时线程不必重新去申请锁，而是可以直接执行方法method2。
     *
     * 2.可中断锁
     * 顾名思义，就是可以相应中断的锁。在Java中，synchronized就不是可中断锁，而Lock是可中断锁。
     * 在前面演示lockInterruptibly()的用法时已经体现了Lock的可中断性。
     *
     * 3.公平锁
     * 公平锁即尽量以请求锁的顺序来获取锁。比如同时有多个线程在等待一个锁，当这个锁被释放时，等待时间最久的线程（最先请求的线程）会获得该所，这种就是公平锁。
     * 非公平锁即无法保证锁的获取是按照请求锁的顺序进行的，这样就可能导致某个或者一些线程永远获取不到锁。
     * 在Java中，synchronized就是非公平锁，它无法保证等待的线程获取锁的顺序。
     * 而对于ReentrantLock和ReentrantReadWriteLock，它默认情况下是非公平锁，但是可以设置为公平锁。
     * 非公平锁性能高于公平锁性能。首先，在恢复一个被挂起的线程与该线程真正运行之间存在着严重的延迟。而且，非公平锁能更充分的利用cpu的时间片，尽量的减少cpu空闲的状态时间。
     *
     * 4.读写锁
     * 读写锁将对一个资源（比如文件）的访问分成了2个锁，一个读锁和一个写锁。
     * 正因为有了读写锁，才使得多个线程之间的读操作不会发生冲突。
     * ReadWriteLock就是读写锁，它是一个接口，ReentrantReadWriteLock实现了这个接口。
     * 可以通过readLock()获取读锁，通过writeLock()获取写锁。
     */
}
