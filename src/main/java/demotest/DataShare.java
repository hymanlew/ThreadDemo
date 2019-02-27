package demotest;

public class DataShare {

    public static void noshare(){
        Mythread1 a = new Mythread1("a");
        Mythread1 b = new Mythread1("b");
        Mythread1 c = new Mythread1("c");
        a.start();
        b.start();
        c.start();
    }

    public static void share(){
        Mythread2 mythread = new Mythread2();
        Thread a = new Thread(mythread,"a");
        Thread b = new Thread(mythread,"b");
        Thread c = new Thread(mythread,"c");
        a.start();
        b.start();
        c.start();
    }

    public static void checkThread(){
        ThreadName threadName = new ThreadName();
        Thread a = new Thread(threadName);
        a.setName("A");
        a.start();
        System.out.println("外部线程 == "+a.getName());
        System.out.println("外部存活 == "+a.isAlive());
        System.out.println("外部 current_name == "+Thread.currentThread().getName());
    }

    public static void checkThread1(){
        TName tName = new TName();
        Thread a = new Thread(tName);
        a.setName("A");
        a.start();
        System.out.println("外部线程 == "+a.getName());
    }

    /**
     * 自定义线程类中的实例变量针对其他线程可以有共享，和不共享之分。
     *
     *
     * 类的构造器及类中的自定义方法（包含 main 方法，重写的方法）都是被 main 线程（即外部主线程）调用的。而线程中的 run 方
     * 法是被具体线程（即自定义的主线程）调用的。
     *
     * 使用Thread.currentThread().getName() 和使用 this.getName()和 对象实例.getName(),都可以得到线程的名称，但是使用 this
     * 调用 getName()方法只能在本类中，而不能在其他类中，更不能在Runnable接口中。所以在实现接口的方式中，只能使用 Thread.currentThread().getName()
     * 获取线程的名称，否则会出现编译时异常。
     * 因为实现 runnable 的方式创建的线程，则只有一个线程组名，不会有其他子线程。不能调用 this。
     *
     * Thread.currentThread().getName()，对象实例.getName() 和 this.getName(）区别：
     * 在继承 Thread的 run方法中使用 this.getName 和在外部调用对象.getName 或者 Thread.currentThread().getName，所得出的
     * 结果不是一样，这是因为：
     *
     * 1.new一个自定义的线程，然后把这个线程对象丢给 Thread对象构造方法，执行start 才会出现这样的区别。
     * 2.如果是直接new 一个自定义对象不交给 Thread线程执行调用，在其内部外部使用 Thread.currentThread().getName()，对象实
     * 例.getName() 和 this.getName(），这3个区别都不存在，都是相同的。因为没有交给 Thread执行，而是直接调用的当前实例本身。
     *
     * 首先要清楚 a（implements Runnable）对象和 t（new thread）对象是两个完全不同的对象，他俩之间唯一的关系就是把 t 传递给 a
     * 对象仅仅是为了让 a 调用 t 对象的 run方法。在构造方法中，Thread.currentThread().getName() 为主线程（main），在run方法中，
     * Thread.currentThread().getName() 获取的是当前具体执行的线程 name（即外部的 t），即它与外部的 a.getName 名称相同。
     *
     * 正常按道理来说，对象继承父类 this也应该实例的，t.setname 也是可以设置到父类中的，重点还是要清楚 a 和 t 是两个完全不同
     * 的对象，他俩之间唯一的关系就是把 t 传递给 a 对象仅仅是为了让 a 调用 t 对象的run方法。
     *
     * 但如果调用线程是 Thread继承的方式 a（extend thread），在构造方法中 Thread.currentThread().getName() 为主线程（main），
     * this.getName() 为Thread-0。在run方法中 Thread.currentThread().getName() 获取的是当前具体执行的线程 name（即外部的 t），
     * this.getName() 仍然为Thread-0（因为是主线程调用执行的当前线程）。外部对象实例.getName() 为 t，外部的 Thread.currentThread()
     * .getName 为 main（因为是主线程调用执行的当前线程）。
     *
     * sleep 方法的作用是在指定的毫秒内让当前 ”正在执行的线程“ 休眠暂停执行，这个正在执行的是指 Thread.currentThread().getName()，
     * 即它是与外部主线程异步执行的。休眠的是 Thread.currentThread().getName() 指定的线程。
     */
    public static void main(String[] args) {
        // 变量不共享，各自计算各自的。
        //noshare();
        // 变量共享，并保证线程安全。
        //share();
        // 查看线程的调用，继承方式
        //checkThread();
        // 查看线程的调用，实现方式
        checkThread1();
    }
}


class Mythread1 extends Thread{
    private int count = 5;

    public Mythread1(String name){
        super();
        // 设置线程名称
        this.setName(name);
    }

    @Override
    public void run() {
        super.run();
        while (count > 0){
            count--;
            System.out.println(Mythread1.currentThread().getName()+"=="+count);
        }
    }
}

class Mythread2 extends Thread{
    private int count = 5;

    // 加锁保证线程安全，加锁的代码也称为互斥区，或者临界区。
    /**
     * 多线程并发安全问题
     * 当多个线程并发访问同一资源时，由于线程切换时机不确定导致执行代码顺序的混乱，
     * 从而出现执行未按程序设计顺序运行导致出现各种错误，严重时可能导致系统瘫痪。
     * 这时就要把线程的异步执行调整为同步执行。
     */
    @Override
    synchronized public void run() {
        super.run();
        count--;
        System.out.println(Mythread1.currentThread().getName()+"=="+count);
    }
}

class ThreadName extends Thread{
    public ThreadName(){
        System.out.println("构造器执行 ==");
        System.out.println("current_name == "+Thread.currentThread().getName());
        System.out.println("this_name == "+this.getName());
        System.out.println("构造存活 == "+this.isAlive());
        System.out.println("构造器结束 ==");
    }

    @Override
    public void run() {
        System.out.println("重写方法执行 ==");
        System.out.println("current_name == "+Thread.currentThread().getName());
        try {
            System.out.println("==================");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("this_name == "+this.getName());
        System.out.println("重写存活 == "+this.isAlive());
        System.out.println("重写结束 ==");
    }
}

class TName implements Runnable{
    public TName(){
        System.out.println("构造器执行==");
        System.out.println("构造线程=="+Thread.currentThread().getName());
        //System.out.println("构造getname=="+this.getName());
        System.out.println("构造器结束==");
    }

    @Override
    public void run() {
        System.out.println("重写方法执行==");
        System.out.println("重写线程=="+Thread.currentThread().getName());
        try {
            System.out.println("==================");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println("重写getname=="+this.getName());
        System.out.println("重写结束==");
    }
}