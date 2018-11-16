package zneedmerge;

import java.util.Random;
import java.util.concurrent.*;

/**
 * 队列包含固定长度的队列和不固定长度的队列。
 * 可阻塞队列，就是当当前队列已经存满了元素时，再向里加入元素，就会阻塞等待。在 java 中实际上已经提供了这样一种队列的接口，即
 * BlockingQueue。它添加删除元素时，都各有三种方法：
 *
 * add，remove ==  当队列已经满时（或空时），不能再加入（或取出）元素，而且抛出异常。
 * offer，poll ==  如果可以添加（或取出）元素则返回 true（或元素），否则返回 false（或 null），但不抛异常。
 * put，take   ==  当队列已经满时（或空时），再加入（或取出）元素会发生阻塞，直到有空间可以添加进去（或有元素可以取出）。
 *
 * BlockingQueue 阻塞队列与 Semaphore 信号灯有些相似，但也不同，阻塞队列是一方存数据，另一方释放数据。Semaphore 通常是由同一
 * 方设置和释放信号量。
 */
public class QueueDemo {

    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(3,10,0, TimeUnit.MINUTES,new SynchronousQueue<>());

    public static void t1(){
        final BlockingQueue queue = new ArrayBlockingQueue(5);
        for(int i=0; i<3; i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            Thread.sleep(new Random().nextInt(1000));
                            System.out.println(Thread.currentThread().getName()+" = 准备放入元素！");
                            queue.put(1);
                            System.out.println(Thread.currentThread().getName()+" = 已经放入元素。当前队列有 "+queue.size()+" 个元素！");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        for(int i=0; i<2; i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            // 将睡眠时间分别设为 100 和 1000，并观察运行结果
                            //Thread.sleep(100);
                            Thread.sleep(1000);
                            System.out.println(Thread.currentThread().getName()+" = 准备取出元素！");
                            queue.take();
                            System.out.println(Thread.currentThread().getName()+" = 已经取出元素。当前队列有 "+queue.size()+" 个元素！");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public static void t2(){
        final DataDemo data = new DataDemo();
        pool.execute(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<3 ;i++){
                    data.sub();
                }
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<3 ;i++){
                    data.maind();
                }
            }
        });
    }

    public static void main(String[] args) {
        // 阻塞队列
        //t1();
        // 阻塞队列实现数据的交叉执行
        t2();
        pool.shutdown();
    }
}

class DataDemo {
    private BlockingQueue queue1 = new ArrayBlockingQueue(1);
    private BlockingQueue queue2 = new ArrayBlockingQueue(1);

    /**
     * 在这里不能使用 static 静态块，因为静态块是加载类时就一同执行了，而且只加载一次。但是我们的需求是在创建对象时才执行代码。
     * 并且我们自定义的成员变量不是静态变量，是不能放入静态块中的。
     *
     * 所以使用这种，匿名构造方法的方式来实现创建对象时，执行相关的代码。它是在任何构造方法之前运行的，即在 new 对象之时首先执
     * 行它，无论调用的是哪一种当前对象的构造方法。
     *
     * 匿名构造方法是在创建对象之时就执行的，并且创建了几个对象，就执行几次。
     */
    {
        try {
            System.out.println("== 匿名构造方法 ==");
            queue2.put(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 当使用阻塞队列之后，就不再需要同步锁 synchronized 了，因为它会造成假死，即 queue 阻塞之后，同步锁又锁住。则程序就会一直等待。
    public void sub(){
        try {
            queue1.put(1);
            for(int i=1; i<11 ;i++){
                System.out.println("sub == "+i);
            }

            queue2.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void maind(){
        try {
            queue2.put(1);
            for (int i = 1; i < 51; i++) {
                System.out.println("main == " + i);
            }

            queue1.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}