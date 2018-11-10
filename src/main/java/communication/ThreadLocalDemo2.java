package communication;

import java.util.*;

/**
 *
 */
public class ThreadLocalDemo2 {

    private static int data = 0;
    // ThreadLocal 底层实现
    //private static DefaultDataThread defaultdata = new DefaultDataThread();
    private static ThreadLocal<Integer> local = new ThreadLocal<Integer>();
    private static ThreadLocal<User> localuser = new ThreadLocal<User>();

    public static void t1(){
        try {
            // 在 ThreadLocal 线程中，默认的初始值即第一次 get 时是为 null 的，所以可以自定义一个初始值。
            DefaultDataThread defaultdata = new DefaultDataThread();
            if(defaultdata.get() == null){
                System.out.println("当前线程中没有值！");
                defaultdata.set("设置值！");
            }
            System.out.println("A == "+defaultdata.get());
            Thread.sleep(3000);

            // 不能使用同一个线程，因为同一线程内的数据会共享，而不会重新获得值
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("B == "+defaultdata.get());
                }
            }).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t2(){
        /**
         * 使用 InheritableThreadLocal 类时，即使是在多线程操作中，子线程也会从父线程中继承值。而不会重新获得值。
         * 但需要注意一点，如果子线程在取得值的同时，主线程将 InheritableThreadLocal 中的值进行修改后，那么子线程取到的还是
         * 旧的值。注意，是同时执行时。
         */
        try {
            DefaultSon defaultdata = new DefaultSon();
            System.out.println("A == "+defaultdata.get());
            Thread.sleep(3000);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("B == "+defaultdata.get());
                }
            }).start();

            defaultdata.set("changed === ");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("C == "+defaultdata.get());
                }
            }).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){

    }

    public static void t4(){

    }

    public static void main(String[] args) {
        // 设置初始值，注意要使用多线程，创建新的 calendar 对象
        //t1();
        // 使用 InheritableThreadLocal 实现子线程对数据的继承，并实现修改
        t2();
        // 使用 TreadLocal 存储一个对象数据
        //t3();
        // 优化对象实例结构，把 TreadLocal 对象隐藏在后面以保护对象数据
        //t4();

        try {
            Calendar calendar = Calendar.getInstance();
            System.out.println(calendar.getTimeInMillis());
            Thread.sleep(2000);
            System.out.println(calendar.getTimeInMillis());
            System.out.println(Calendar.getInstance().getTimeInMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class DefaultDataThread extends ThreadLocal{
    // 不能把 Calendar 抽成成员变量，因为重复调用其 millis 方法时，返回的都是第一次的值
    //private Calendar calendar = Calendar.getInstance();
    // 设置初始值
    @Override
    protected Object initialValue() {
        long time = Calendar.getInstance().getTimeInMillis();
        return "我是默认值！"+time;
        //return System.currentTimeMillis();
    }
}

// public class InheritableThreadLocal<T> extends ThreadLocal<T>
class DefaultSon extends InheritableThreadLocal {
    @Override
    protected Object initialValue() {
        long time = Calendar.getInstance().getTimeInMillis();
        return "我是子类的默认值 == "+time;
    }

    // 使用本类的 child 方法实现子线程对父线程数据的继承，并实现修改
    @Override
    protected Object childValue(Object parentValue) {
        return parentValue+"== 子类追加！";
    }
}

class Tools {
    public static DefaultDataThread defaultdata = new DefaultDataThread();
}