package com.yangzy.thread.thread.threadPool;

public class ParseArrayData implements Runnable {

    private DataArray array;

    public ParseArrayData(DataArray array){
        this.array = array;
    }

    @Override
    public void run() {
        while (!array.isEmpty()){
            try {
                System.out.println("线程" + Thread.currentThread().getId() + "：解析数据 ------->" + array.getData());
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("解析完成！");
    }
}
