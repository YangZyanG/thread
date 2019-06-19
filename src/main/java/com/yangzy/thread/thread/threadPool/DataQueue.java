package com.yangzy.thread.thread.threadPool;

import java.util.Queue;

/***
 * 数据源
 */
public class DataQueue {

    private Queue<Integer> queue;

    public Queue<Integer> getQueue() {
        return queue;
    }

    public void setQueue(Queue<Integer> queue) {
        this.queue = queue;
    }

    public synchronized Integer getData(){
        return queue.poll();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
