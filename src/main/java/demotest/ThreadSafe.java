package demotest;

import java.io.PrintWriter;
import java.util.Random;

/**
 * 非线程安全问题是对于同一个对象中的全局实例变量来说的，而方法内的私有局部变量是不存在线程安全问题的（即其结果永远是线程安全
 * 的，这是因为方法内部的变量是私有的特性造成的）。
 *
 * 关键字 synchronized 取得的锁都是对象锁，而不是把一段代码或方法当作锁。所以当多线程调用同一个实例对象时，就会进行同步操作。
 * 但如果是多个线程访问多个实例对象，则 JVM 就会创建多个锁，执行时就会是异步执行。
 *
 * 只有共享资源的读写访问才需要同步化，如果不是共享资源，那么根本就没有同步的必要。
 * 虽然在赋值时进行了同步，但在取值时有可能出现意外的情况，就是脏读。其实就是在读值时，该值已经被改过了。这就需要同步处理了。
 * 因为 synchronized 锁的是当前对象，只有在释放了当前对象时，其他线程才能正常调用该对象的其他同步的方法。
 *
 */
public class ThreadSafe {

    public static void t1(){
        /*
         * 关键字 synchronized 拥有锁重入的功能，即当一个线程得到一个对象锁后，再次请求此对象锁是可以再次得到该对象锁的。这
         * 也证明在一个 synchronized 方法、块的内部调用本类的其他 synchronized 方法，块时，是永远可以得到锁的，即是可以执行的。
         *
         * 可重入锁的概念是：自己可以再次获取自己的内部锁。否则就会造成死锁。
         * 可重入锁也支持在父子类继承的环境中使用，即子类是完全可以通过 “可重入锁” 调用父类的同步方法的。
         */
        demo demo = new demo();
        Mythreadb mythreadb = new Mythreadb(demo);
        mythreadb.start();

        // 因为 synchronized 加在方法上，锁的是当前对象，只有在释放了当前对象时，其他线程才能正常调用该对象的其他同步的方法。
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                System.out.println("new =======");
                demo.t3();
            }
        };
        thread.start();
    }

    public static void t2(){
        demo demo = new demo();

        /**
         * 当一个线程执行的代码出现异常时，其所持有的锁会自动释放。
         */
        Thread th1 = new Thread(){
            @Override
            public void run() {
                super.run();
                demo.t4();
            }
        };
        th1.setName("a");
        th1.start();

        Thread th2 = new Thread(){
            @Override
            public void run() {
                super.run();
                demo.t4();
            }
        };
        th2.start();
    }

    public static void t3(){
        /**
         * 同步的方法不具有继承性：
         * 当子类对父类中的同步方法进行重写后，如果不加 synchronized 修饰，则子类中的方法是不会自动被同步执行的。因为已经重写了。
         *
         * 使用 synchronized 同步块可以提高运行效率，它同样是同步执行，并且持有当前的对象进行加锁。
         * 即当一个线程访问同步块进行加锁后（必须是 this），则其他线程对同一个对象中的所有其他的同步块（必须是 this）的访问都将
         * 被阻塞，即锁的是当前对象。它与是否是一个方法无关。
         *
         * 多线程访问同一对象，同一方法内的不同同步块时，只会同步 this 锁的同步块，因为它锁的是当前对象。前提是 this 同步块不是
         * 该方法中的第一个同步块。
         * 并且 this 同步块对当前对象的其他同步方法或 this 同步块调用都呈阻塞状态。
         * 而其他的同步块，只要锁定的不是同一个对象（非this对象，全局变量或方法参数），就还是会异步执行的。但是对于同一个对象还
         * 是多线程还是会同步执行的。
         */
        demo demo = new demo();
        Mythreadb mythreadb = new Mythreadb(demo);
        mythreadb.setName("a");
        mythreadb.start();

        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                System.out.println("new =======");
                demo.t5();
            }
        };
        thread.setName("b");
        thread.start();
    }



    public static void main(String[] args) {
        // 可重入锁
        //t1();
        // 异常自动解锁
        //t2();
        t3();
    }
}

class Mythreadb extends Thread{
    private demo demo;
    public Mythreadb(demo d){
        this.demo = d;
    }
    @Override
    public void run() {
        super.run();
        //demo.t1();
        demo.t5();
    }
}

class demo{
    synchronized public void t1(){
        try {
            System.out.println("== 1 ==");
            Thread.sleep(3000);
            t2();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    synchronized public void t2(){
        System.out.println("== 2 ==");
        t3();
    }
    synchronized public void t3(){
        System.out.println("== 3 ==");
    }
    synchronized public void t4(){
        if(Thread.currentThread().getName().equals("a")){
            System.out.println("开始抛异常了 ===");

            // 获取 0-1 之间的随机小数
            double d = Math.random();
            int i = (int)(d*10);
            Random random = new Random();
            int i2 = random.nextInt(10);
            System.out.println(i +" = 1 = "+ i2);

            Integer.parseInt("a");

            System.out.println(i +" = 2 = "+ i2);
        }else {
            System.out.println("已经释放了对象锁！");
        }
    }

    public void t5(){
        synchronized (new Object()){
            System.out.println(Thread.currentThread().getName()+"测试不同的同步块 ===== 1");
            try {
                System.out.println(Thread.currentThread().getName()+" == 4 ==");
                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName()+" == 5 ==");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 如果把 this 放在最前面，则线程就会先锁定当前的对象，而其他线程就访问不到下面的同步块了。
        synchronized (this){
            System.out.println(Thread.currentThread().getName()+"测试不同的同步块 ===== 2");
            try {
                System.out.println(Thread.currentThread().getName()+" == 6 ==");
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName()+" == 7 ==");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}