package com.zhang.jiu.learn.complatable;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

public class CompletableTest {
    @Test
    public void test1 () throws Exception{
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        //1 CompletableFuture 无返回结果
        CompletableFuture.runAsync(new Task1(),executorService);
        //2 CompletableFuture 有返回结果
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            int a = 5;
            int b = 5;
            return a + b;
        });
        //future1 的执行结果
        Integer result = future1.get(10, TimeUnit.SECONDS);
        System.out.println(result);

        //3 future2 依赖future1的执行结果
        CompletableFuture<Integer> future2 = future1.thenApply(future1Result -> {
            return future1Result + 4;
        });
        System.out.println(future2.get());

        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            int a = 5;
            int b = 5;
            return a * b;
        });
        //4 future4 依赖 future1 future3 的结果
        CompletableFuture<Integer> future4 = future3.thenCombine(future1, (future1Result, future3Result) -> {
            return future1Result+future3Result;
        });
        System.out.println(future4.get());

        CompletableFuture<Integer> future5 = CompletableFuture.supplyAsync(() -> {
            int a = 5;
            int b = 5;
            return a / b;
        });
        //5 future6 等待 future1 future3 future5的结果
        CompletableFuture<Void> future6 = CompletableFuture.allOf(future1, future3, future5);
        CompletableFuture<Integer> futureFinal = future6.thenApply(item -> {
            Integer result1 = future1.join();
            Integer result3 = future3.join();
            Integer result5 = future5.join();

            return result1 + result3 + result5;
        });
        //5 future1 future3 future5任何一个执行结束了,执行 future7
        CompletableFuture<Object> future7 = CompletableFuture.anyOf(future1, future3, future5);
        CompletableFuture<String> futureFinal2 = future7.thenApply(item -> {
            System.out.println("item"+item);
            return "future1 future3 future5 有一个执行结束了,我触发了";
        });

        System.out.println(futureFinal2.get());
    }
    @Test
    public void test2 () throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        String hostAddress = address.getHostAddress();
        System.out.println(hostAddress);
    }

    public static class Task1 implements Runnable {

        @Override
        public void run() {
            System.out.println("异步任务Task1执行了");
        }
    }
}
