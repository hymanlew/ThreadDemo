package LockAbout;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Java里面内置锁 (synchronized) 和 Lock(ReentrantLock) 都是可重入的。Reentrant /rintrent/：可重入的。
 *
 * synchronized 和 ReentrantLock 的比较：
 * 1）Lock是一个接口，而synchronized是Java中的关键字，synchronized是内置的语言实现；
 *
 * 2）synchronized在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而Lock在发生异常时，如果没有主动通过 unLock()
 * 去释放锁，则很可能造成死锁现象，因此使用Lock时需要在finally块中释放锁；
 *
 * 3）Lock可以让等待锁的线程响应中断，而synchronized却不行，使用synchronized时，等待的线程会一直等待下去，不能够响应中断；
 *
 * 4）通过Lock可以知道有没有成功获取锁，而synchronized却无法办到。
 *
 * 5）Lock可以提高多个线程进行读操作的效率，具有嗅探锁定，多路分支通知等功能，而且使用上也更加灵活。
 *
 * 总结：ReentrantLock相比synchronized，增加了一些高级的功能。但也有一定缺陷。在ReentrantLock类中定义了很多方法（在 tn 方法中），比如：
 * isFair()        //判断锁是否是公平锁
 * isLocked()    //判断锁是否被任何线程获取了
 * isHeldByCurrentThread()   //判断锁是否被当前线程获取了
 * hasQueuedThreads()   //判断是否有线程在等待该锁
 *
 *
 * 两者在锁的相关概念上区别：
 * 1) 可中断锁，顾名思义，就是可以相应中断的锁。在Java中，synchronized就不是可中断锁，而Lock是可中断锁。如果某一线程A正在执行
 * 锁中的代码，另一线程B正在等待获取该锁，可能由于等待时间过长，线程B不想等待了，想先处理其他事情，我们可以让它中断自己或者在别
 * 的线程中中断它，这种就是可中断锁。
 * lockInterruptibly()的用法体现了Lock的可中断性。
 *
 * 2) 公平锁，即尽量以请求锁的顺序来获取锁。线程获取锁的顺序是按照线程加锁的顺序来分配的，即 FIFO（first in first out）规则。
 * 这种就是公平锁。
 * 非公平锁即无法保证锁的获取是按照请求锁的顺序进行的。这样就可能导致某个或者一些线程永远获取不到锁。
 * 在Java中，synchronized就是非公平锁，它无法保证等待的线程获取锁的顺序。ReentrantLock可以设置成公平锁。
 *
 * CPU在调度线程的时候是在等待队列里随机挑选一个线程，由于这种随机性所以是无法保证线程先到先得的（synchronized控制的锁就是这种
 * 非公平锁）。但这样就会产生饥饿现象，即有些线程（优先级较低的线程）可能永远也无法获取CPU的执行权，优先级高的线程会不断的强制
 * 它的资源。那么如何解决饥饿问题呢，这就需要公平锁了。
 *
 * 公平锁可以保证线程按照时间的先后顺序执行，避免饥饿现象的产生。但公平锁的效率比较低，因为要实现顺序执行，需要维护一个有序队列。
 * ReentrantLock 便是一种公平锁机制，通过在构造方法中传入true就是公平锁，传入false，就是非公平锁。默认是非公平锁。
 *
 * 3) 读写锁，读写锁将对一个资源（比如文件）的访问分成了2个锁，一个读锁和一个写锁。正因为有了读写锁，才使得多个线程之间的读操作可
 * 以并发进行，不需要同步，而写操作需要同步进行，提高了效率。
 * ReadWriteLock就是读写锁，它是一个接口，ReentrantReadWriteLock实现了这个接口。
 *
 * 4) 绑定多个条件，一个ReentrantLock对象可以同时绑定多个Condition对象，而在synchronized中，锁对象的wait()和notify()或notifyAll()
 * 方法可以实现一个隐含的条件，如果要和多余一个条件关联的时候，就不得不额外地添加一个锁，而ReentrantLock则无须这么做，只需要多
 * 次调用new Condition()方法即可。
 *
 *
 * 3.性能比较：
 * 在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，而当竞争资源非常激烈时（即有大量线程同时竞争），此时ReentrantLock的
 * 性能要远远优于synchronized。所以说，在具体使用时要根据适当情况选择。
 *
 * 在JDK1.5中，synchronized是性能低效的。因为这是一个重量级操作，它对性能最大的影响是阻塞的是实现，挂起线程和恢复线程的操作都需
 * 要转入内核态中完成，这些操作给系统的并发性带来了很大的压力。相比之下使用Java提供的ReentrankLock对象，性能更高一些。到了JDK1.6，
 * 发生了变化，对synchronize加入了很多优化措施，有自适应自旋，锁消除，锁粗化，轻量级锁，偏向锁等等。导致在JDK1.6上synchronize的
 * 性能并不比Lock差。官方也表示，他们也更支持synchronize，在未来的版本中还有优化余地，所以还是提倡在synchronized能实现需求的情况
 * 下，优先考虑使用synchronized来进行同步。
 */
public class LockDemo2 {

    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(5,5,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>());

    public static void t1(){
        // getHolddCount() == 查询当前线程保持锁定的个数，即调用 lock 方法的次数
        Tservice tservice = new Tservice();
        tservice.method();
        tservice.method2();
    }

    public static void t2(){
        // getQueueLength() == 查询正等待获取此锁定的线程估计数。如果有 5个线程，1个线程先执行 await 方法，那么调用此方法则
        // 返回值是 4，说明有 4个线程同时在等待 lock 的释放。
        Tservice tservice = new Tservice();

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    tservice.method3();
                }
            });
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("现有 = "+tservice.lock.getQueueLength()+"个线程在等待获取锁！");
    }

    public static void t3(){
        // getWaitQueueLength(condition) == 返回等待与此锁定相关的给定条件 conditon 的线程估计数。比如有 5个线程，每个线程都
        // 执行了一个 conditon 对象的 await 方法，那么调用此方法则返回值是 5，说明有 5个线程同时在等待 lock 的释放。
        Tservice tservice = new Tservice();

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    tservice.waitm();
                }
            });
        }
        try {
            Thread.sleep(2000);
            tservice.notifytm();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t4(){
        // hasQueuedThread(thread) == 判断指定的线程是否有在等待该锁。
        // hasQueuedThreads() == 判断是否有线程在等待该锁。
        Tservice tservice = new Tservice();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 在此案例中，不能使用 condition 的 await 方法。因为它是使用的 wait 方法即自动释放锁
                //tservice.waitm();
                tservice.method3();
            }
        };

        try {
            Thread a = new Thread(runnable);
            a.start();
            Thread.sleep(500);
            Thread b = new Thread(runnable);
            b.start();
            Thread.sleep(500);

            System.out.println("a 线程是否在等待获取锁 = "+tservice.lock.hasQueuedThread(a));
            System.out.println("b 线程是否在等待获取锁 = "+tservice.lock.hasQueuedThread(b));
            System.out.println("是否有线程在等待获取锁 = "+tservice.lock.hasQueuedThreads());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t5(){
        // hasWaiters(condition) == 判断是否有线程正在等待与该锁有关的 condition 条件。
        Tservice tservice = new Tservice();

        for(int i=0; i<pool.getCorePoolSize(); i++){
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    tservice.waitm();
                }
            });
        }
        try {
            Thread.sleep(2000);
            tservice.checkWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t6(){
        // isHeldByCurrentThread() == 判断锁是否被当前线程获取了
        // isLocked() == 判断该锁是否由任意线程保持
        Tservice tservice = new Tservice(true);
        pool.execute(new Runnable() {
            @Override
            public void run() {
                tservice.method4();
            }
        });
    }

    public static void t7(){
        Tservice tservice = new Tservice();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tservice.method5();
            }
        };
        try {
            // 由于两个线程调用的都是同一个对象的方法，因为 lock 锁而产生同步关系。所以会先执行完 a，再执行 b。
            Thread a = new Thread(runnable);
            a.setName("a");
            a.start();
            Thread.sleep(500);
            Thread b = new Thread(runnable);
            b.setName("b");
            b.start();
            b.interrupt();

            System.out.println("main end = ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void t8(){
        Tservice tservice = new Tservice(true);
        ThreadFactory names = new ThreadFactory() {
            private int i = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"线程-"+(i++));
            }
        };
        pool = new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),names);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tservice.method6();
            }
        };
        pool.execute(runnable);
        pool.execute(runnable);
    }

    public static void t9(){
        Tservice tservice = new Tservice();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tservice.method7();
            }
        };
        try {
            Thread thread = new Thread(runnable);
            thread.start();
            Thread.sleep(3000);
            thread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t10(){
        Tservice tservice = new Tservice();
        Runnable wait = new Runnable() {
            @Override
            public void run() {
                tservice.wait2();
            }
        };
        Runnable notify = new Runnable() {
            @Override
            public void run() {
                tservice.notify2();
            }
        };
        try {
            pool.execute(wait);
            Thread.sleep(2000);
            pool.execute(notify);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        // getHolddCount() == 查询当前线程保持锁定的个数，即调用 lock 方法的次数
        //t1();
        // getQueueLength() == 查询正等待获取此锁定的线程估计数。
        //t2();
        // getWaitQueueLength(condition) == 返回等待与此锁定相关的给定条件 conditon 的线程估计数。
        //t3();
        // hasQueuedThread(thread) == 判断指定的线程是否有在等待该锁。
        //t4();
        // hasWaiters(condition) == 判断是否有线程正在等待与该锁有关的 condition 条件。
        //t5();
        // isHeldByCurrentThread() == 判断锁是否被当前线程获取了
        //t6();
        // lockInterruptibly() == 如果当前线程未被中断，则获取锁定。如果已被中断则出现异常。
        //t7();
        // trylock() == 仅在调用时检查该锁对象未被另一个线程锁定的情况下，才获取锁定对象加锁。
        //t8();
        // awaitUninterruptibly() == 该方法强制当前线程忽略中断命令，而是继续正常执行。
        //t9();
        // awaitUninterruptibly() == 该方法强制当前线程忽略中断命令，而是继续正常执行。
        t10();

        pool.shutdown();
    }
}

class Tservice {
    public ReentrantLock lock = new ReentrantLock();
    public Tservice(){

    }

    public void method(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" hold count = "+lock.getHoldCount());
            method2();
        }finally {
            lock.unlock();
        }
    }

    public void method2(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" hold count = "+lock.getHoldCount());
        }finally {
            lock.unlock();
        }
    }

    public void method3(){
        try {
            lock.lock();
            System.out.println(Thread.currentThread().getName()+" hold ==");
            Thread.sleep(8000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }


    private Condition condition = lock.newCondition();
    public void waitm(){
        try {
            lock.lock();
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void notifytm(){
        try {
            lock.lock();
            System.out.println("现有 = "+lock.getWaitQueueLength(condition)+"个线程在等待 condition 获取锁！");
            condition.signal();
        }finally {
            lock.unlock();
        }
    }

    public void checkWait(){
        try {
            lock.lock();
            System.out.println("是否有线程在等待 condition 获取锁 = "+lock.hasWaiters(condition)+"，线程数量为 = "+lock.getWaitQueueLength(condition));
            condition.signal();
        }finally {
            lock.unlock();
        }
    }


    // ReentrantLock 便是一种公平锁机制，通过在构造方法中传入true就是公平锁，传入false，就是非公平锁。默认是非公平锁。
    public Tservice(boolean isFair){
        lock = new ReentrantLock(isFair);
    }
    public void method4(){
        System.out.println("是否是公平锁 == "+lock.isFair());
        try {
            System.out.println("是否由任意线程保持1 = "+lock.isLocked());
            System.out.println("是否获取锁1 = "+lock.isHeldByCurrentThread());
            lock.lock();
            System.out.println("是否获取锁2 = "+lock.isHeldByCurrentThread());
            System.out.println("是否由任意线程保持2 = "+lock.isLocked());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }

    public void method5(){
        /**
         * Lock() 方法可以让等待锁的线程响应中断，即当当前线程被 interrupt 后，它也不会抛出异常而是正常结束，最后释放锁。
         * 而synchronized却不行，使用synchronized时，等待的线程会一直等待下去，不能够响应中断；
         *
         * lockInterruptibly() == 如果当前线程未被中断，则获取锁定。如果已被中断则出现异常。
         */
        try {
            //lock.lock();
            lock.lockInterruptibly();
            System.out.println("lock begin = "+Thread.currentThread().getName());
            // 不断的创建对象
            for(int i=0; i<10000000; i++){
                String s = new String();
                Math.random();
                //System.out.println("-----");
            }
            System.out.println("lock end = "+Thread.currentThread().getName());
        }catch (Exception e){
            e.printStackTrace();
            // 只有自定义异常并输出信息时，才能 get 到 message。并且以下方式都不能得到想要的信息
            System.out.println("exception1 == "+e.getMessage());
            System.out.println("exception2 == "+e.getCause());
            System.out.println("exception3 == "+e.getStackTrace());
            System.out.println("exception4 == "+System.err);
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    public void method6(){
        // trylock() == 仅在调用时检查该锁对象未被另一个线程锁定的情况下，才获取锁定对象加锁。
        if(lock.tryLock()){
            System.out.println(Thread.currentThread().getName()+" = 获得锁！");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println(Thread.currentThread().getName()+" = 没有获得锁！");
        }
        System.out.println(Thread.currentThread().getName()+" 是否锁定 = "+lock.isHeldByCurrentThread());
        if(lock.isHeldByCurrentThread()){
            lock.unlock();
        }

        // trylock(time，timeUnit) == 仅在调用时检查该锁对象在指定的时间内未被另一个线程锁定，且当前线程未被中断的情况下，
        // 才获取锁定对象加锁。
        try {
            System.out.println("=== 等待锁释放 "+Thread.currentThread().getName()+" ===");
            Thread.sleep(8000);

            if(lock.tryLock(2,TimeUnit.SECONDS)){
                System.out.println(Thread.currentThread().getName()+" = 超时获得锁 ==2");
                Thread.sleep(5000);
                System.out.println(Thread.currentThread().getName()+" = 运行结束！");
            }else{
                System.out.println(Thread.currentThread().getName()+" = 没有获得锁 ==2");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    public void method7(){
        try {
            lock.lock();
            System.out.println("wait begin == ");

            // 使用 await 方法时，如果当前线程被中断则会抛出异常。这时就要用 awaitUninterruptibly 方法
            //condition.await();
            condition.awaitUninterruptibly();
            System.out.println("wait end == ");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("catch exception ==");
        }finally {
            lock.unlock();
        }
    }

    public void wait2(){
        try {
            Calendar calendar = Calendar.getInstance();
            // 该方法是在当前日期上再加上指定的时间。
            calendar.add(Calendar.SECOND,10);

            lock.lock();
            System.out.println("wait begin == "+System.currentTimeMillis());
            System.out.println(new Date()+" == "+calendar.getTime());

            // 该方法是使得当前线程阻塞指定的时间之后（从当前时间开始算），再继续执行。并且它允许被其他线程提前唤醒。
            condition.awaitUntil(calendar.getTime());
            System.out.println("wait end == "+System.currentTimeMillis());
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("catch exception ==");
        }finally {
            lock.unlock();
        }
    }
    public void notify2(){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND,10);

            lock.lock();
            System.out.println("notify begin == "+System.currentTimeMillis());
            condition.signalAll();
            System.out.println("notify end == "+System.currentTimeMillis());
        }finally {
            lock.unlock();
        }
    }
}