package demotest;

/**
 有序性：
 　　Java 语言提供了 volatile 和 synchronized 两个关键字来保证线程之间操作的有序性：
     volatile 是因为其本身包含 “禁止指令重排序” 的语义。
     synchronized 是由 “一个变量在同一个时刻只允许一条线程对其进行 lock 操作” 这条规则获得的，此规则决定了持有同一个对象锁的两个同步块只能串行执行。


 Volatile原理（JVM 虚拟机运行操作它时的过程）：
 　　Java语言提供了一种稍弱的同步机制，即volatile变量，用来确保将变量的更新操作通知到其他线程。当把变量声明为volatile类型后，编译器与运行时都会注
 意到这个变量是共享的，因此不会将该变量上的操作与其他内存操作一起重排序。
 volatile变量不会被缓存在寄存器或者对其他处理器不可见的地方，因此在读取volatile类型的变量时总会返回最新写入的值。

 　　在访问volatile变量时不会执行加锁操作，因此也就不会使执行线程阻塞，因此volatile变量是一种比sychronized关键字更轻量级的同步机制。

 　　当对非 volatile 变量进行读写的时候，每个线程先从内存拷贝变量到CPU缓存（私有内存）中。如果计算机有多个CPU，每个线程可能在不同的CPU上被处理，这意
     味着每个线程可以拷贝到不同的 CPU cache 中。而声明变量是 volatile 的，则JVM 保证了每次读变量都从公共内存中读，跳过 CPU cache（私有内存） 这一步。

 volatile 性能：
 　　volatile 的读性能消耗与普通变量几乎相同，但是写操作稍慢，因为它需要在本地代码中插入许多内存屏障指令来保证处理器不发生乱序执行。

 volatile 的可见性，是指线程访问变量是否是最新值。是指线程之间的可见性，一个线程修改的状态对另一个线程是可见的。

 */
/**
 * volatile 与 synchronized 的区别：
 * 1，volatile 是线程同步的轻量级实现，所以其性能肯定比 synchronized 要好。并且它只能修饰变量，而后者可以修饰方法及代码块。
 * 2，多线程访问 volatile 不会发生阻塞，而synchronized 会出现阻塞。
 * 3，volatile 能保证数据的可见性，但不能保证原子性，所以在使用时要考虑到程序是否要求原子性。而 synchronized 可以保证原子性，也可以间接保证可见性，
 *    因为它会将私有内存和公共内存中的数据同步。
 * 4，volatile 解决的是变量在多个线程间的可见性，而synchronized 解决的是多个线程间访问资源的同步性。线程安全包含原子性和可见性两个方面，java 的
 *    同步机制都是围绕这两个方面来确保线程安全的。
 */

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 原子性的特点总结为2点（理想中的状态）：
 * 1. 对一个volatile变量的写操作，只有所有步骤完成，才能被其它线程读取到。
 * 2. 多个线程对volatile变量的写操作本质上是有先后顺序的。也就是说并发写没有问题。
 *
 * 但是 i++本身不是原子操作，是读并写（即当一个线程读到值变进行计算时，另一个线程也要计算时，就会出现脏数据）。并且 volatile 并不会有锁的特性
 *
 * 比如 volatile int a = 0；之后有一个操作 a++；这个变量a具有可见性，但是a++ 依然是一个非原子操作，也就是这个操作同样存在线程安全问题。
 * 比如 a=0；（a非long和double类型） 这个操作是不可分割的，那么我们说这个操作时原子操作。而 a++ 这个操作实际是a = a + 1；是可分割的，所以他不是一个原子操作。
 * 非原子操作都会存在线程安全问题，所以必须使用同步技术（sychronized）来让它变成一个原子操作。一个操作是原子操作，那么我们称它具有原子性。
 *
 *
 * 另外如果在方法上加上 synchronized，也就没必要再使用 volatile 来声明变量了，因为 synchronized 包含了互斥性和可见性两个特征。但前提是多线程访问的都是
 * 同一个方法，但如果是分别访问不同的方法，还是要加 volatile 关键字的，而它的原子性由 synchronized 来解决。
 * volatile 主要使用的场合是在多个线程中可以看到实例变量被更改了，并且可以获得最新的值使用。也就是用多线程读取共享变量时可以获得最新值使用（只从公共内存中读取）。
 * volatile 修饰的变量不允许线程内部缓存和重排序，即直接修改内存。所以对其他线程是可见的。但 volatile只能让被他修饰的内容具有可见性，但不具有原子性。
 * 即它不具备同步性。
 *
 *
 * 在 Java 中 volatile、synchronized 和 final 实现可见性。
 * 在 Java 中 synchronized 和 lock、unlock 来保证线程之间的操作具有原子性，有序性。它可以使多线程访问同一资源时具有同步性，并且还会将线程工作内存中的私有变量与公共内存的进行同步。
 * 因此，synchronized 包含互斥性和可见性两个特征，它不仅能解决一个线程看到实例对象的实时状态，还可以保证进入同步方法或同步块的每个线程，都能看到由同一个锁保护
 * 之前（执行方法之前）所有的修改效果。
 *
 * 对于多线程并发，要外练互斥性，内修可见性。
 */
public class VolatileDemo {

    public static void t1(){

        try {
            Task task =new Task();
            Thread thread = new Thread(task);
            thread.start();

            Thread.sleep(3000);
            System.out.println("现在停止计数 ===");
            task.setFlag(false);
            System.out.println("stop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void t2(){

        Task1[] taskarr =new Task1[100];
        for(int i=0;i<taskarr.length;i++){
            taskarr[i] = new Task1();
        }
        for(int i=0;i<taskarr.length;i++){
            taskarr[i].start();
        }
    }

    public static void t3(){

        Mythreadd dd = new Mythreadd();
        Thread th1 = new Thread(dd);
        th1.start();
        Thread th2 = new Thread(dd);
        th2.start();
        Thread th3 = new Thread(dd);
        th3.start();
        Thread th4 = new Thread(dd);
        th4.start();
        Thread th5 = new Thread(dd);
        th5.start();
    }

    public static void t4(){

        try {
            service serv = new service();
            Mythreadd[] taskarr = new Mythreadd[5];

            for(int i=0;i<taskarr.length;i++){
                taskarr[i] = new Mythreadd(serv);
            }
            for(int i=0;i<taskarr.length;i++){
                taskarr[i].start();
            }

            Thread.sleep(1000);
            System.out.println(service.ato.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 服务器模式会死循环
        //t1();
        // volatile 不具备原子性
        //t2();
        // 使用原子类，保证了线程安全
        //t3();
        // 原子类虽然保证了线程安全，但是执行是随机的
        t4();
    }
}


class Task implements Runnable{
    /**
     * 当在服务器环境中，并发执行时，thread1 线程修改的 flag值并不会立即被刷新到主存中去，从而导致 thread线程中的 flag仍
     * 然为 true，仍会继续运行，形成死循环。这就是因为线程对变量的实际值不可见性。
     *
     * 加上 volatile 修饰符就能够实现线程对变量的可见性。
     * 可见性，是指线程之间的可见性，一个线程修改的状态对另一个线程是可见的。
     *
     * 并且同步块也存在如下语义：
     * 1.进入同步块，访问共享变量会去读取主存
     * 2.退出同步块，本地内存对共享变量的修改会立即刷新到主存
     */
    boolean flag = true;
    //volatile boolean flag = true;
    int i = 0;

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void method(){
        /**
         * 此代码在本地服务器线程中，可以正常运行。但在 JVM 设置为 -server 服务器模式中 64bit 的 JVM 上运行时，就会出现死循环。
         * 这是因为在线程启动时，flag 存在于公共堆栈及线程的私有堆栈中。而在服务器模式中，为了线程运行的效率，线程一直在私有堆栈中
         * 取得 flag。而 setFlag 虽然被执行，但更新的却是公共堆栈中的 flag=false，又由于私有堆栈与公共堆栈中的值是不同步的，所以
         * 一直是死循环状态。
         *
         * 这种现象被称为“重排序”，只要在某个线程中无法检测到重排序情况（即不能实现多线程间的可见性，即使在其他线程中可以明显地看到该线程中的重排序，死循环），
         * 那么就无法确保线程中的操作将按照程序中指定的顺序来执行。
         * 当主线程首先写入 i，然后在没有同步的情况下写入 flag，那么读线程看到的顺序可能与写入的顺序完全相反。
         *
         * 这时就要使用 volatile 关键字，其作用就是强制从公共堆栈中取得变量的值，而不是从线程私有数据栈中取得。
         * synchronized 同步锁也有这个效果，可见性。
         *
         *
         * 在没有同步的情况下，编译器、处理器以及运行时等都可能对操作的执行顺序进行一些意想不到的调整。在缺乏足够同步的多线程程序中，要想对内存操作的
         * 执行持续进行判断，无法得到正确的结论。
         *
         * 这个看上去像是一个失败的设计，但却能使JVM充分地利用现代多核处理器的强大性能。例如在缺少同步的情况下，Java内存模型允许编译器对操作顺序进行重
         * 排序，并将数值缓存在寄存器中。此外它还允许CPU对操作顺序进行重排序，并将数值缓存在处理器特定的缓存中。从而最终得出正确的结果。
         *
         * 并且其重排序的特性，与可见性的特点，两者是可以互相印证的。
         */
        try {
            while (flag){
                i++;
                Thread.sleep(100);
                System.out.println(i);
                //i-=1;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        method();
    }
}

class Task1 extends Thread{

    volatile public static int count;

    private static void add(){
        // 只有数据量大时，才会体现出来不具有原子性的特点（各个线程之间不会按照整百的数量增加）
        for(int i=0;i<100;i++){
            count++;
        }
        System.out.println(Thread.currentThread().getName()+" == "+count);
    }

    synchronized private static void synadd(){
        /**
         * 注意一定要加 static 关键字，这样同步锁锁的对象就是当前类了，也就达到了同步的效果。具有了原子性的特点。
         *
         * 另外如果在方法上加上 synchronized，也就没必要再使用 volatile 来声明变量了。
         */
        for(int i=0;i<100;i++){
            count++;
        }
        System.out.println(Thread.currentThread().getName()+" == "+count);
    }

    @Override
    public void run(){
        //add();
        synadd();
    }
}

class Mythreadd extends Thread{

    // java的concurrent包下提供了一些原子类，我们可以通过阅读API来了解这些原子类的用法。比如：AtomicInteger、AtomicLong、AtomicReference等。
    private AtomicInteger count = new AtomicInteger(0);
    public Mythreadd(){}
    //@Override
    //public void run() {
    //    for(int i=0;i<100;i++){
    //        System.out.println(Thread.currentThread().getName()+" == "+count.incrementAndGet());
    //    }
    //}


    private service service;
    public Mythreadd(service service){
        this.service = service;
    }
    @Override
    public void run() {
        service.add();
    }
}

class service{
    // 原子类方式虽然保证了线程安全，得到的值是正确的。但是它在有逻辑性的业务下时，结果的输出是有随机性的（即线程不是顺序地执行）。
    // 这是因为 addAndGet 虽然是原子的，但是方法和方法之间（add 与 addAndGet 之间）的调用却不是原子的。这就必须要同步解决了。
    // 就是在 add 方法上加上 synchronized 同步锁。
    public static AtomicLong ato = new AtomicLong();
    public void add(){
        System.out.println(Thread.currentThread().getName()+" = 加100 = "+ato.addAndGet(100));
        ato.addAndGet(1);
        System.out.println(ato.get());
    }
}

