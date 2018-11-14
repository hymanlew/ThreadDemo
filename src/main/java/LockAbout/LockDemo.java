package LockAbout;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lock 接口（实现类为 ReentrantLock）：
 *
 * Lock，是一个锁对象。在Java中锁是用来控制多个线程访问共享资源的方式，一般来说，一个锁能够防止多个线程同时访问共享资源（但有
 * 的锁可以允许多个线程并发访问共享资源，比如读写锁）。在Lock接口出现之前，Java程序是靠synchronized关键字实现锁功能的，而JAVA
 * SE5.0之后并发包中新增了 Lock 接口用来实现锁的功能，它提供了与 synchronized 关键字类似的同步功能，只是在使用时需要显式地获
 * 取和释放锁，缺点就是缺少像synchronized那样隐式获取释放锁的便捷性，但是却拥有了锁获取与释放的可操作性，可中断的获取锁以及超
 * 时获取锁等多种synchronized关键字所不具备的同步特性。

 * Lock 比传统线程中的 synchronized 方式更加面向对象，与生活中的锁类似，锁本身也是一个对象。两个线程执行的代码片段要实现同步
 * 互斥的效果，它们必须用同一个 lock 对象。锁是上在被操作资源的类的内部方法中，而不是线程代码中。
 *
 * lock()：执行此方法时, 如果锁处于空闲状态, 当前线程将获取到锁。相反，如果锁已经被其他线程持有，将禁用当前线程，直到当前线程
 * 获取到锁。
 *
 * boolean tryLock()：如果锁可用, 则获取锁, 并立即返回true, 否则返回false. 该方法和lock()的区别在于, tryLock()只是"试图"获
 * 取锁, 如果锁不可用, 不会导致当前线程被禁用, 当前线程仍然继续往下执行代码. 而lock()方法则是一定要获取到锁, 如果锁不可用, 就
 * 一直等待, 在未获得锁之前,当前线程并不继续向下执行。
 *
 * unlock()：执行此方法时, 当前线程将释放持有的锁. 锁只能由持有者释放, 如果线程并不持有锁, 却执行该方法, 可能导致异常的发生。
 *
 * Condition newCondition()：条件对象，获取等待通知组件。该组件和当前的锁绑定，当前线程只有获取了锁，才能调用该组件的await()方
 * 法，而调用后，当前线程将缩放锁。
 *
 *
 * 读写锁：分读锁，写锁。读锁之间不互斥，写锁之间要互斥，读写锁之间要互斥。这是 JVM 控制的，只需加上对应的锁即可。
 */
public class LockDemo {

    public static void t1(){
        ExecutorService service = Executors.newFixedThreadPool(2);
        OutPutter putter = new OutPutter();

        service.execute(new Runnable() {
            @Override
            public void run() {
                //putter.put3("aaaaaaaa");
                putter.putlock("aaaaaaaa");
            }
        });
        service.execute(new Runnable() {
            @Override
            public void run() {
                //putter.put3("bbbbbbb");
                putter.putlock("bbbbbbb");
            }
        });
        service.execute(new Runnable() {
            @Override
            public void run() {
                //putter.put3("ccccccc");
                putter.putlock("ccccccc");
            }
        });
        service.shutdown();
    }

    public static void t2(){
        final ReadWrite readWrite = new ReadWrite();
        ExecutorService service = Executors.newFixedThreadPool(3);

        service.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        // 在测试时，编写读与存程序的间隔时间不要相同，因为这样就会人为的使线程之间有秩序的互相读写值了。
                        Thread.sleep(500);
                        readWrite.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        service.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        // 在测试时，编写读与存程序的间隔时间不要相同，因为这样就会人为的使线程之间有秩序的互相读写值了。
                        Thread.sleep(4000);
                        readWrite.put(new Random().nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        service.shutdown();
    }

    public static void main(String[] args) {
        // 使用 lock 对象同步数据
        //t1();
        // 读写锁测试
        t2();
    }
}

class OutPutter {
    Lock lock = new ReentrantLock();

    public void put(String name){
        int len = name.length();
        synchronized (OutPutter.class){
            for(int i=0; i<len; i++){
                System.out.println(name.charAt(i));
            }
            System.out.println(" ==== ");
        }
    }

    public synchronized void put2(String name){
        int len = name.length();
        for(int i=0; i<len; i++){
            System.out.println(name.charAt(i));
        }
        System.out.println(" ==== ");
    }

    public void put3(String name){
        int len = name.length();
        for(int i=0; i<len; i++){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(name.charAt(i));
        }
        System.out.println(" ==== ");
    }

    public void putlock(String name){
        lock.lock();
        // 使用 try-catch 包裹，是防止程序出现异常时，锁对象不能释放
        try {
            int len = name.length();
            for(int i=0; i<len; i++){
                Thread.sleep(500);
                System.out.println(name.charAt(i));
            }
            System.out.println(" ==== ");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}

class ReadWrite {
    private Object data = null;

    // 不用使用自定义的锁对象，即自定义的监视器对象，因为这会锁住整个对象。而不能达到连续读的效果。
    // lock 这里是可重入锁。但当有很多线程都从某个数据结构中读取数据而很少有线程对其进行修改时，rwlock 就很有用了。在这种情况下，
    // 允许读取器线程共享访问是合适的。当然，写入器线程依然必须是互斥访问的。
    private Lock lock = new ReentrantLock();
    private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    public void get(){
        //lock.lock();
        rwlock.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+" == 准备读 ==");
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName()+" == 读到 "+data);
            data = null;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //lock.unlock();
            rwlock.readLock().unlock();
        }
    }

    public void put(int i){
        //lock.lock();
        rwlock.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName()+" == 准备存 ==");
            Thread.sleep(2000);
            this.data = i;
            System.out.println(Thread.currentThread().getName()+" == 存入 "+data);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //lock.unlock();
            rwlock.writeLock().unlock();
        }
    }
}