package com.yangzy.thread.thread.threadPool;

public class ParseData implements Runnable {

    private DataQueue queue;

    public ParseData(DataQueue queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!queue.isEmpty()){
            try {
                System.out.println("线程" + Thread.currentThread().getId() + "：解析数据 ------->" + queue.getData());
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("解析完成！");
    }
}
