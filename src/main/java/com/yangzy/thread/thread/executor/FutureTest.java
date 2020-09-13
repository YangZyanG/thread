package com.yangzy.thread.thread.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yangziyang
 * @since 2020-09-12
 * 本示例模拟一个网页加载场景，文本和图片资源并行加载
 */
public class FutureTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void reader() throws InterruptedException, ExecutionException {
        //模拟获取图片资源路径
        final List<String> images = new ArrayList<>();
        images.add("a");
        images.add("b");
        images.add("c");

        Callable<List<String>> task = new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                final List<String> result = new ArrayList<>();
                for (String image : images){
                    System.out.println("开始下载图片资源:" + image);
                    downloadImage(image);
                    result.add(image);
                }
                return result;
            }
        };
        Future<List<String>> future = executorService.submit(task);

        //模拟加载文本资源
        System.out.println("开始加载文本资源..");
        readerText();

        List<String> result = future.get();
        System.out.println("开始加载图片资源..");
        for (String image : result){
            readerImage(image);
        }

        executorService.shutdown();
    }

    private void readerText() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("文本资源加载完成");
    }

    private void readerImage(String image) throws InterruptedException {
        Thread.currentThread().sleep(1000);
        System.out.println("图片资源" + image + "加载完成");
    }

    private void downloadImage(String image) throws InterruptedException {
        Thread.currentThread().sleep(1000);
        System.out.println("图片资源" + image + "下载完成");
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTest test = new FutureTest();
        test.reader();
    }
}
