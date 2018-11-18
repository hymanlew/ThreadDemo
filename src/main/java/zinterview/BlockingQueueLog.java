package zinterview;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 多线程并发输出日志
 *
 * 使用 List 与 BlockingQueue 的区别：
 * 因为 BlockingQueue 实现了 ReentrantLock 安全锁的机制，保证了线程安全，且可以设置为 FIFO 规则（所以可以直接取出），添加删
 * 除是可以阻塞等待的。但是在取出后，集合中对应的元素就没有了。
 * 而 List 是传统数据集合，是非线程安全的，在多线程中也不能保证 FIFO 规则（但手动加入 ReentrantLock 锁机制可以实现），是不能
 * 阻塞等待的。但是其得到元素 get 时，不会影响集合中原有的元素。
 */
public class BlockingQueueLog {

    public static void printLog(String log){
        System.out.println(log+" = "+System.currentTimeMillis());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t1(){
        System.out.println("log = begin");
        for(int i=0; i<16; i++){
            final String log = (i+1)+"";
            printLog(log);
        }
    }


    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(4,4,0, TimeUnit.MINUTES,new LinkedBlockingDeque<>());

    private static List<String> obs = new ArrayList();
    public static void t2(){
        System.out.println("log = begin");
        for(int i=0; i<16; i++){
            final String log = (i+1)+"";
            obs.add(log);
        }
        int index = obs.size()/pool.getCorePoolSize();

        for(int i=0; i<pool.getCorePoolSize(); i++){
            final int num = i;
            pool.execute(new Runnable() {
                private int d = index*num;
                @Override
                public void run() {
                    while (d<index*(num+1)){
                        printLog(obs.get(d));
                        d++;
                    }
                    System.out.println("list 是否清空 = "+obs.size());
                }
            });
        }
    }


    // 如果是生产者消费者场景，或是要保证 FIFO 规则（设置为公平锁），则最好使用 BlockingQueue 可阻塞队列。即使 ArrayBlockingQueue
    // 设值为 1，程序也可以正常执行，因为它操作时是可以阻塞等待的。
    //private static BlockingQueue queue = new ArrayBlockingQueue(3);
    private static BlockingQueue queue = new LinkedBlockingQueue();
    public static void t3(){
        System.out.println("log = begin");

        for(int i=0; i<16; i++){
            final String log = (i+1)+"";
            try {
                queue.put(log);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 要注意在使用 ArrayBlockingQueue 时，如果操作时有阻塞等待的情况时，就必须要使用 while true 循环不
                        // 断的去获取元素（并在循环内作是否取出完成的判断）。并且必须编写在 take 方法之后，否则就使得 put 时
                        // 一直发生阻塞状态。
                        //while (true){
                        //    printLog(queue.take().toString());
                        //    if(queue.isEmpty()){
                        //        break;
                        //    }
                        //}

                        while (!queue.isEmpty()){
                            // 由于 BlockingQueue 实现了 ReentrantLock 安全锁的机制，所以可以直接取出。
                            printLog(queue.take().toString());
                        }
                        System.out.println("BlockingQueue 是否清空 = "+queue.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        // 注意在使用 ArrayBlockingQueue 时，put 方法必须编写在 take 方法之后，否则就使得 put 时一直发生阻塞状态。
        //for(int i=0; i<16; i++){
        //    final String log = (i+1)+"";
        //    try {
        //        queue.put(log);
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //}
    }

    public static void main(String[] args) {
        // 原始代码
        //t1();
        // 4 线程代码，使用传统集合
        //t2();
        // 4 线程代码，使用阻塞队列集合
        t3();

        pool.shutdown();
    }
}
