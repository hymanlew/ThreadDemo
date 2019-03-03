package ThreadForSingle;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 立即加载：就是在使用类的时候已经将对象创建完毕，常见的实现办法就是直接 new 实例化。而立即加载从语境上来看，有 ‘着急，急迫’
 * 的含义，所以也称为 ‘饿汉模式’。
 *
 * 延迟加载：就是在调用 get 方法时实例才被创建，常见的实现办法就是在该方法中 new 实例化。从语境上来看，有 ‘缓慢，不急迫’
 * 的含义，所以也称为 ‘懒汉模式’。
 */
public class SingleModelDemo implements Serializable{

    /**
     * 立即加载 == 饿汉模式：
     *
     * 此代码版本为立即加载，由于声明为静态变量，所以不能创建其他实例变量。
     * 而且又由于 getInstance 方法没有同步，所以可能会出现非线程安全问题。
     */
    private static SingleModelDemo demo = new SingleModelDemo();
    public static SingleModelDemo getInstance(){
        return demo;
    }


    /**
     * 延迟加载 == 懒汉模式，同时保证了实例对象中变量值的实时性。
     *
     * 但需要注意的是，DCL 双重锁模式在创建对象时，虽然对象是单例的，但是其变量值却有可能因为多线程并发访问，而发生脏数据。
     * 所以在静态实例对象上需要加上 volatile 关键字。
     */
    private static volatile SingleModelDemo demo2;
    public static SingleModelDemo getInstance2(){
        try {
            if(demo2 != null){

            }else {
                // 模拟在创建对象之前做一些准备的工作
                Thread.sleep(2000);

                // 虽然在部分代码上使用同步块，提升了运行效率。但还是会有非线程安全的问题
                synchronized (SingleModelDemo.class){
                    //demo2 = new SingleModelDemo();

                    // 所以在这里加上双重检查锁的功能，就可以解决非线程安全的问题。即 Double-checked Locking (DCL)
                    if(demo2 == null){
                        demo2 = new SingleModelDemo();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return demo2;
    }


    // 另外使用静态内部类的方式，也可以创建懒汉模式的单例对象
    private static class ModelHandler {
        private static SingleModelDemo demo3 = new SingleModelDemo();
    }
    public static SingleModelDemo getInstance3(){
        return ModelHandler.demo3;
    }


    /**
     * 使用静态内部类的方式，可以创建线程安全的单例对象。但遇到序列化对象时（输入输出对象时），使用默认的方式运行（注释掉 read
     * Resolve 方法）还是会创建多例的。
     * 下列代码中（不使用 readResolve 方法），demo3 对象的 hashcode 值不同，则代表了是不同的对象。
     *
     * 解决的办法就是在反序列化中使用 readResolve 方法（只要实现了序列化接口，并定义了序列化号，它就会自动调用。如果没有实现接
     * 口并且又声明了 readResolve 方法，则会抛出异常）。
     * 这是因为 JDK 序列化操作提供了一个很特别的钩子（hook），类中具有一个私有的被实例化的方法 readresolve()，这个方法可以确保
     * 单例的实现对象在序列化时仍然是单例的。
     */
    private static final long serialUID = 888L;
    protected Object readResolve(){
        return ModelHandler.demo3;
    }
    public static void t1(){
        try {
            SingleModelDemo demo = SingleModelDemo.getInstance3();
            System.out.println(demo.hashCode()+" == 1");

            /**
             * file 的路径，可以使用绝对与相对两种路径。
             * 其中相对路径的根路径，就是当前项目的根。
             */
            FileOutputStream fileout = new FileOutputStream(new File("src/main/file/obj.txt"));
            ObjectOutputStream objout = new ObjectOutputStream(fileout);
            objout.writeObject(demo);
            objout.close();
            fileout.close();

            FileInputStream filein = new FileInputStream(new File("src/main/file/obj.txt"));
            ObjectInputStream objin = new ObjectInputStream(filein);
            demo = (SingleModelDemo) objin.readObject();
            System.out.println(demo.hashCode()+" == 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 使用静态块的方式，也可以创建懒汉模式的单例对象，实现单例模式
    private static SingleModelDemo demo4 = null;
    static {
        demo4 = new SingleModelDemo();
    }
    public static SingleModelDemo getInstance4(){
        return demo4;
    }


    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
    public static void test(){
        for(int i=0; i<pool.getCorePoolSize(); i++){
            System.out.println(SingleModelDemo.getInstance().hashCode()+" == 饿汉");
            System.out.println(SingleModelDemo.getInstance2().hashCode()+" == 懒汉 DCL");
            System.out.println(SingleModelDemo.getInstance3().hashCode()+" == 静态内部类的方式");
            System.out.println(SingleModelDemo.getInstance4().hashCode()+" == 静态块的方式");
        }
    }

    public static void main(String[] args) {
        // 使用静态内部类的方式，可以创建线程安全的单例对象
        t1();

        // 立即加载，延迟加载
        //test();
    }
}
