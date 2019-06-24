package com.yangzy.thread.thread.threadPool;

public class DataArray {

    private int[] array;
    private volatile int i;

    public DataArray(int[] array){
        this.array = array;
        this.i = 0;
    }

    public int[] getArray() {
        return array;
    }

    public void setArray(int[] array) {
        this.array = array;
    }

    public synchronized int getData(){
        return array[i++];
    }

    public boolean isEmpty(){
        return i >= array.length;
    }
}
