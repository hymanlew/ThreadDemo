package LockAbout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock -- Condition 的功能类似传统线程安全中的 wait，notify 的功能，用于线程间通信。在等待 Condition 时，允许发生
 * ‘虚假唤醒’ 这通常作为基础同步线程的让步。对于大多数应用程序，这带来的实际影响很小，因为 Condition 应该总是在一个循环中被等
 * 待，并测试正被等待的状态声明。某个实现可以随意移除可能的虚假唤醒，但建议总是假定这些虚假唤醒可能发生，因此总是在一个循环中等待。
 *
 * await() == wait()，await(long,timeUnit) == wait(long)，signal() == notify()，signalAll() == notifyAll()，
 * 要注意 Condition是被绑定到Lock上的，要创建一个Lock的Condition必须用newCondition()方法。
 * 在使用 notify 或 notifyall 进行通知时，被通知的线程都是 JVM 随机选择的，但是 Condition 可以实现选择性通知。并且是它默认提供的。
 *
 * Condition的强大之处在于，对于一个锁，我们可以为多个线程间建立不同的Condition（即对象监视器），即可以设置多路等待和通知。线
 * 程对象可以注册在指定的Condition中，从而可以有选择的进行线程通知，在调度线程上更加灵活。
 * 而synchronized就相当于整个Lock对象中只有一个单一的Condition对象，所有的线程都注册在这个对象上。线程开始notifyAll时，需要通
 * 知所有的WAITING线程，没有选择权，会有相当大的效率问题。
 */
public class LockCondition {
    private static ThreadPoolExecutor pool =
            new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>(1024));
    private static ProdConsum1 prod = new ProdConsum1();

    public static void t1(){
        try {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    prod.await();
                }
            });
            Thread.sleep(2000);
            prod.signal();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("===== 分隔线 =====");
        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    prod.set();
                }
            }
        });
        pool.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    prod.get();
                }
            }
        });
    }

    public static void t2(){
        try {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    prod.await();
                }
            });
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    prod.await2();
                }
            });
            Thread.sleep(2000);
            // 此时就是唤醒所有的等待线程，同传统的 notifyAll 相同，但是这样用没什么意义。
            //prod.signalAll();

            // 此时只能唤醒对应监视对象 conditon 的等待线程。
            prod.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){

    }

    public static void t4(){
        for(int i=0; i<20; i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        prod.set();
                    }
                }
            });
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        prod.get();
                    }
                }
            });

        }
    }

    public static void main(String[] args) {
        // condition 一对一的生产消费操作栈模式
        //t1();
        // 测试多路通知
        //t2();
        // 多生产，一消费的操作栈模式（在 t2 源码的基础上修改）
        //t3();
        // 多对多的生产消费操作栈模式，并解决假死的情况
        t4();

        pool.shutdown();
    }
}


class ProdConsum1 {
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private Condition condition2 = lock.newCondition();

    public void await(){
        try {
            // 在调用 condition 方法之前，必须调用 lock 方法获得同步监视对象。否则会抛出无监视器异常。
            lock.lock();

            System.out.println("开始等待 == "+Thread.currentThread().getName());
            condition.await();
            System.out.println("等待结束 == "+Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void signal(){
        try {
            lock.lock();
            System.out.println("释放锁 ===");
            condition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void await2(){
        try {
            lock.lock();
            System.out.println("第二次等待 == "+Thread.currentThread().getName());

            // 如果需要使用多路通知功能时，就必须使用多个不同的 condition 对象。
            //condition.await();
            condition2.await();
            System.out.println("第二次结束 == "+Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void signalAll(){
        try {
            lock.lock();
            System.out.println("释放所有的锁 ===");
            // 如果当前有多个 condition 条件监视器时，就需要指定通知了。
            condition.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void signalAll2(){
        try {
            lock.lock();
            System.out.println("释放所有的锁 ===");
            condition2.signalAll();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }


    private boolean flag = false;

    public void set(){
        try {
            lock.lock();
            while (flag){
                System.out.println("set -----");
                condition.await();
            }
            System.out.println("set == "+Thread.currentThread().getName());
            flag = true;

            // 有可能出现假死的情况，所以要改为 signalAll
            //condition.signal();
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void get(){
        try {
            lock.lock();
            while (!flag){
                System.out.println("get -----");
                condition.await();
            }
            System.out.println("get == "+Thread.currentThread().getName());
            flag = false;

            // 有可能出现假死的情况，所以要改为 signalAll
            //condition.signal();
            condition.signalAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}


