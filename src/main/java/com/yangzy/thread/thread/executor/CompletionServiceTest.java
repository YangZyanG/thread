package com.yangzy.thread.thread.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yangziyang
 * @since 2020-09-13
 */
public class CompletionServiceTest {

    private ExecutorService executor;

    public CompletionServiceTest(ExecutorService executor){
        this.executor = executor;
    }

    public void reader() throws InterruptedException, ExecutionException {
        //模拟获取图片资源路径
        final List<String> images = new ArrayList<>();
        images.add("a");
        images.add("b");
        images.add("c");

        CompletionService<String> completionService = new ExecutorCompletionService<String>(executor);
        for (final String image : images){
            completionService.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    System.out.println("开始下载图片" + image + "资源:" +  Thread.currentThread().getId());
                    return downloadImage(image);
                }
            });
        }

        System.out.println("开始加载文本资源");
        readerText();

        for (int i=0; i<images.size(); ++i){
            Future<String> future = completionService.take();
            String image = future.get();
            System.out.println("开始加载图片" + image + "资源:" + Thread.currentThread().getId());
            readerImage(image);
        }
    }

    private void readerText() throws InterruptedException {
        Thread.sleep(1000);
        System.out.println("文本资源加载完成");
    }

    private void readerImage(String image) throws InterruptedException {
        Thread.currentThread().sleep(1000);
        System.out.println("图片资源" + image + "加载完成:" + Thread.currentThread().getId());
    }

    private String downloadImage(String image) throws InterruptedException {
        Thread.currentThread().sleep(1000);
        return "图片资源" + image + "下载完成:" + Thread.currentThread().getId();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletionServiceTest test = new CompletionServiceTest(Executors.newFixedThreadPool(10));
        test.reader();
    }
}

