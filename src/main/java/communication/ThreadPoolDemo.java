package communication;

import java.util.Random;
import java.util.concurrent.*;

/**
 * concurrent /kernkarent/：并发的。
 * executor /igzekiuter/：执行者。
 */
public class ThreadPoolDemo {

    public static void t1(){
        // 固定线程数量的线程池
        ExecutorService pool = Executors.newFixedThreadPool(3);

        // 线程数量动态变化的缓存线程池
        //ExecutorService pool = Executors.newCachedThreadPool();

        // 单线程的线程池，其优点是始终会保持有一个活跃的线程在工作。即如果当前线程死掉，它就会重新创建一个新的线程来工作
        //ExecutorService pool = Executors.newSingleThreadExecutor();

        for(int i=0; i<10; i++) {
            // 在子循环中调用私有变量，则此外部的私有变量需要声明为 final 的。
            final int task = i;
            System.out.println(task+"==============");
            // 每执行一次，则代表线程池执行一次任务
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < 10; k++) {
                        try {
                            System.out.println(Thread.currentThread().getName() + " == " + k+" for "+task);
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        System.out.println("任务完成 ===");

        /**
         * 因为使用的是线程池，线程会循环利用，只会回收不会销毁。所以一旦运行程序不会自动停止，而是一直等待。需要手动停止。
         * shutdown()，任务完成则停止。shutdownNow()，立即停止。
         */
        pool.shutdown();
    }

    public static void t2(){
        /**
         * 使用线程池启动定时器：
         * schedule（调用一个 runnable 对象执行任务，间隔时间，时间单位），该方法只会在指定的时间之后执行一次。
         * scheduleAtFixedRate（调用 runnable 对象执行任务，间隔时间，之后间隔的时间，时间单位），该方法会在指定的时间之后
         * 执行一次，然后再按照指定的时间间隔有频率的执行。
         */
        Executors.newScheduledThreadPool(3).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("定时器执行 ===");
            }
        },2,2, TimeUnit.SECONDS);
    }

    public static void t3(){
        /**
         * callable /kellebou/：随时可偿还的
         */
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            // 执行一个任务，并等待结果
            Future future = pool.submit(new Callable<Object>() {
                @Override
                public Object call() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "hello ==";
                }
            });
            System.out.println("等待结果 ==");
            System.out.println(future.get());

            // 也可以指定 future 方法等待结果的时间，如果超时还拿不到结果，则会抛异常
            //System.out.println(future.get(1,TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool.shutdown();

        ExecutorService pool2 = Executors.newFixedThreadPool(3);
        // 使用多线程执行一组任务，并等待结果（其底层就是封装了一组 future 对象）
        CompletionService completion = new ExecutorCompletionService(pool2);
        for(int i=0; i<10; i++){
            final int index = i;
            completion.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    int millis = new Random().nextInt(3000);
                    Thread.sleep(millis);
                    return index;
                }
            });
        }
        try {
            for(int i=0; i<10; i++){
                System.out.println(completion.take().get()+" ==");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool2.shutdown();
    }

    public static void main(String[] args) {
        // 三种线程池的使用
        t1();
        // 线程池的定时器
        //t2();
        // 线程池的定时器
        //t3();
    }
}
