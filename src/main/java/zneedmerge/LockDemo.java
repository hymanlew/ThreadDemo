package zneedmerge;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock 比传统线程中的 synchronized 方式更加面向对象，与生活中的锁类似，锁本身也是一个对象。两个线程执行的代码片段要实现同步
 * 互斥的效果，它们必须用同一个 lock 对象。锁是上在被操作资源的类的内部方法中，而不是线程代码中。
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

    public static void main(String[] args) {
        t1();
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