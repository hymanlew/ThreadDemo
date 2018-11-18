package zinterview;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程并发输出日志
 *
 * 使用 List 与 BlockingQueue 的区别：
 * 因为 BlockingQueue 实现了 ReentrantLock 安全锁的机制，保证了线程安全，且又可以设置为FIFO 规则（所以可以直接取出），添加删
 * 除是可以阻塞等待的。但是在取出后，集合中对应的元素就没有了。
 * 而 List 是传统数据集合，是非线程安全的，在多线程中也不能保证 FIFO 规则（但手动加入 ReentrantLock 锁机制可以实现），是不能
 * 阻塞等待的。但是其得到元素 get 时，不会影响集合中原有的元素。
 */
public class BlockingQueueLog2 {

    public static void t1(){
        System.out.println("begin ==");
        for(int i=0; i<10; i++){
            String in = (i+1)+"";
            String out = TestDo.dosome(in);
            System.out.println(Thread.currentThread().getName()+" == "+out);
        }
    }


    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(10,10,0, TimeUnit.MINUTES,new LinkedBlockingDeque<>());
    private static BlockingQueue q1 = new SynchronousQueue(true);
    private static BlockingQueue queue = new LinkedBlockingQueue();
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition put = lock.newCondition();
    private static Condition take = lock.newCondition();
    private static volatile boolean flag = true;

    public static void t2(){
        System.out.println("begin ==");

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        while (flag){
                            take.await();
                        }
                        while(!queue.isEmpty()){
                            String out = TestDo.dosome(queue.take().toString());
                            System.out.println(Thread.currentThread().getName()+" == "+out);
                        }
                        flag = true;
                        put.signalAll();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                }
            });
        }
        for(int i=0; i<10; i++){
            final String in = (i+1)+"";
            try {
                lock.lock();
                while (!flag){
                    put.await();
                }
                queue.put(in);

                // 在本方法案例中不能使用 SynchronousQueue，因为它会造成一直阻塞的状态。参考 test 方法中的解释。
                //q1.put(in);
                flag = false;
                take.signalAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
    }

    public static void t3(){
        final Semaphore semaphore = new Semaphore(1);
        System.out.println("begin ==");

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 亮起灯，建立同步机制
                        semaphore.acquire();

                        String out = TestDo.dosome(q1.take().toString());
                        System.out.println(Thread.currentThread().getName()+" == "+out);

                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        for(int i=0; i<10; i++){
            final String in = (i+1)+"";
            try {
                q1.put(in);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tx(){
        for(int i=0; i<10; i++){
            String in = (i+1)+"";
            try {
                queue.put(in);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!queue.isEmpty()){
                            // 由于 BlockingQueue 实现了 ReentrantLock 安全锁的机制，并且保证了 FIFO 规则，所以可以直接取出。
                            String out = TestDo.dosome(queue.take().toString());
                            System.out.println(Thread.currentThread().getName()+" == "+out);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void test(){
        /**
         * 本测试案例说明了，SynchronousQueue 这类阻塞队列中，每个 put 必须等待一个 take，否则就会产生一直阻塞的状态。反之亦
         * 然。因为它没有任何的内部容量，甚至连一个队列的容量都没有，所以必须是实时调用的。
         * 即除非另一个线程试图移除或添加某个元素（即处于等待状态下），否则也不能（无论使用任何方法）添加或删除元素。
         *
         * 该类也不能迭代队列，因为其中没有任何元素可用于迭代。即 iterator() 永远为空。
         *
         * 它非常适合于传递性设计，在此种设计中，在一个线程中运行的对象要将某些信息，事件或任务传递给在另一个线程中运行的对象，
         * 它就必须与该对象同步。因此它也称为同步队列。
         * 它也可以设置为公平排序的策略，以保证 FIFO 规则（默认是非公平的）。
         */
        Thread thread = new Thread(new Runnable() {
            private int i = 0;
            @Override
            public void run() {
                try {
                    while (q1.isEmpty()) {
                        System.out.println(q1.take());
                        i++;
                        if(i == 10){
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        for(int i=0; i<10; i++){
            final String in = (i+1)+"";
            try {
                q1.put(in);

                // 这里的 take 方法会造成一直阻塞的状态。
                System.out.println(q1.take()+"=");
                queue.put(in);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("q1 = "+q1.size());
        System.out.println("que = "+queue.size());
    }

    public static void main(String[] args) {
        // 原始代码
        //t1();
        // 10 线程代码，使用 LinkedBlockingQueue
        //t2();
        // 10 线程代码，使用 SynchronousQueue 阻塞队列集合
        t3();

        //test();
        pool.shutdown();
    }
}

class TestDo {
    public static String dosome(String str){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String out = str+" == ok";
        return out;
    }
}