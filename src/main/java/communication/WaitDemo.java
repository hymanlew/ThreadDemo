package communication;

import java.util.ArrayList;
import java.util.List;

/**
 * wait/notify 等待/通知机制：
 * 类似于就餐的流程，厨师做一道菜的时间不确定，那么服务员上菜的时间也就不确定。但是服务员又不能一直空等着菜做完。服务员可以先
 * 去忙别的，而当厨师做好之后再通知服务员（即通知机制），那么服务员的这段时间又叫等待（即等待机制）。这时 ‘等待/通知’ 机制就出现了。
 *
 * 需要说明的是，在 demotest 包中的多线程之间也可以实现通信，因为它们都共同访问了同一个变量。但那种通信机制不是 ‘等待/通知’，
 * 多个线程完全是主动式地读取一个共享变量，在花费读取时间的基础上，读到的值是不是想要的，并不能完全确定。所以就需要 ‘等待/通知’ 机制。
 *
 * 方法 wait() 的作用是使当前执行代码的线程进行等待，用来将当前线程置入 ‘预执行队列’ 中，并且在 wait 所在的代码行处停止执行，
 * 直到接到通知或被中断为止。它与 notify 都是 object 类的方法。
 * 在调用 wait 之前，线程必须获得该对象的对象级别锁，即只能在同步方法或同步块中调用此方法。在执行完 wait 方法后，当前线程释放
 * 锁。在从 wait 方法返回前，线程与其他线程竞争重新获得锁。
 * 如果调用 wait 方法时没有持有适当的锁，则会抛出 Illegalmonitor 异常，属于非检查类异常，因此不需要 try-catch。
 *
 * 方法 notify() 也要在同步方法或同步块中调用，即也要获得对象级别的锁。如果调用时没有持有适当的锁，也会抛出 Illegalmonitor 异
 * 常。该方法用来通知那些可能等待该对象对象锁的其他线程，若有多个线程等待，则由线程规划器随机挑选出其中一个呈 wait 状态的线程，
 * 对其发出通知 notify，并使它等待获取该对象的对象锁。
 * 需说明的是，在执行 nofity 方法后，当前线程不会马上释放该对象锁，呈 wait 状态的线程也并不能马上获取该对象锁，要等到执行 notify
 * 方法的线程将程序执行完，也就是退出 synchronized 代码块后，当前线程才会释放锁，而呈 wait 状态所在的线程才可以获取该对象锁。
 * 当第一个获得了该对象锁的 wait 线程运行完毕后，它会释放掉该对象锁。此时如果该对象没有再次使用 notify 语句，则即便该对象已经空闲，
 * 其他 wait 状态等待的线程由于没有得到该对象的通知，还会继续阻塞在 wait 状态，直到这个对象发出一个 notify 或 notifyAll。
 *
 * 如果发出 notify 操作时没有处于阻塞状态中的线程，则该命令会被忽略。
 *
 * 即：wait 使线程停止运行，释放共享资源的锁，进入等待队列，直到被唤醒。而 notify 会随机唤醒等待队列中，停止的等待同一共享资源的
 * ‘一个’线程，并使其退出等待队列，继续运行（只通知一个线程）。
 *
 * 带一个参数的 wait(long 毫秒) 方法的功能是等待某一时间内是否有线程对锁唤醒，如果没有，当超过指定的时间后它会自动唤醒。
 *
 *
 * nofityAll 方法可以使所有正在等待队列中等待同一共享资源的 ‘全部’ 线程进入运行状态。此时优先级最高的那个线程最先执行，但也有可能
 * 是随机执行，这要取决于 JVM 虚拟机的实现。（该方法等同于多次调用 notify 方法）。
 *
 * 在同步块中的 wait，notify 方法执行中，遇到异常时都会导致线程终止（例如 interrup 线程中止异常），同时锁也会被释放。
 */
public class WaitDemo {

    public static void t1(){
        testList list = new testList();
        My my = new My(list);
        my.start();
        Mya a = new Mya(list);
        a.start();
    }

    public static void t2(){
        // 这段代码会抛出异常，是因为没有 ‘对象监视器’，也就是没有同步加锁
        try {
            String s = new String("11");
            s.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){
        testList list = new testList();
        try {
            My my = new My(list);
            my.start();
            Thread.sleep(3000);
            System.out.println("等待 3 秒 ======");
            Myb b = new Myb(list);
            b.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t4(){
        /**
         * 由于有对象锁，所以在执行时两个线程不能同时运行，在编程时也就需要分出先后顺序了，即不能过早地通知。
         * 并且发出通知后，也不会马上就结束 wait 状态。
         * 即，wait 被执行后，锁自动释放。但 notify 执行后，锁却不自动释放。必须 notify 手动 sleep 等待之后才可以。
         */
        testList list = new testList();
        Mya a = new Mya(list);
        a.start();
        My my = new My(list);
        my.start();
    }


    public static void main(String[] args) {
        // 不断地进行通信，会浪费资源
        t1();
        // wait，notify
        //t3();
        // 由于有对象锁，所以在编程时需要分出先后顺序了
        //t4();
    }
}

class testList{

    private List list = new ArrayList();
    public void add(){
        list.add("test");
    }
    public int size(){
        return list.size();
    }
}

class My extends Thread{

    private testList test;
    public My(testList test){
        this.test = test;
    }

    //@Override
    //public void run() {
    //    super.run();
    //    try {
    //        for(int i=0; i<10; i++){
    //            test.add();
    //            System.out.println("加入了 "+(i+1)+" 个元素！");
    //            Thread.sleep(1000);
    //        }
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}

    //@Override
    //public void run() {
    //    super.run();
    //    try {
    //        synchronized (test){
    //            System.out.println("开始 wait ："+System.currentTimeMillis());
    //            test.wait();
    //            System.out.println("结束 wait ："+System.currentTimeMillis());
    //        }
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}

    @Override
    public void run() {
        super.run();
        try {
            synchronized (test){
                for(int i=0; i<10; i++){
                    test.add();
                    if(test.size() == 5){
                        test.notify();
                        System.out.println("数量已达标，发出通知 ====");
                    }
                    System.out.println("加入了 "+(i+1)+" 个元素！");
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Mya extends Thread{

    private testList test;
    public Mya(testList test){
        this.test = test;
    }

    //@Override
    //public void run() {
    //    /**
    //     * 采用这种方式虽然使多个线程间实现了通信，但缺点就是这个线程会不停地 while 轮询来检查一个条件，这样就很浪费 CPU 资源。
    //     * 如果轮询的时间间隔很小，则就更浪费资源了。如果时间间隔很大，又有可能会取不到想要的数据。
    //     * 而 wait/notify 机制，就可以减少 CPU 资源浪费，且可以实现多个线程间通信。
    //     */
    //    try {
    //        while (true){
    //            if(test.size() == 5){
    //                System.out.println("已达到指定数量，退出！");
    //                throw new InterruptedException();
    //            }
    //            System.out.println("检查数量 ========");
    //        }
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}

    @Override
    public void run() {
        try {
            synchronized (test){
                if(test.size() != 5){
                    System.out.println("数量不够，开始等待 =====");
                    test.wait();
                    System.out.println("等待结束，数量OK！");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 测试对象锁用于多线程之间，会阻塞
    //@Override
    //public void run() {
    //    try {
    //        synchronized (test){
    //            for(int i=0; i<10; i++){
    //                System.out.println("test ====");
    //                Thread.sleep(500);
    //            }
    //        }
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}
}

class Myb extends Thread{

    private testList test;
    public Myb(testList test){
        this.test = test;
    }

    @Override
    public void run() {
        super.run();
       synchronized (test){
           System.out.println("开始 notify ："+System.currentTimeMillis());
           test.notify();
           System.out.println("结束 notify ："+System.currentTimeMillis());
       }
    }
}
