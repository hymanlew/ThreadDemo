package demotest;

import com.sun.javafx.logging.PulseLogger;

/**
 * 内部类和静态内部类：
 *
 */
public class InnerClass {

    public static void t1(){
        final OutClass.Inner inner = new OutClass.Inner();
        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                inner.method1();
            }
        },"a");

        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                inner.method2();
            }
        },"b");

        /**
         * 在内部类中的同步方法与同步块之间，如果锁定的不是同一个对象，则执行的时候是异步的。
         */
        th1.start();
        th2.start();
    }

    public static void t2(){
        final OutClass.Inner inner = new OutClass.Inner();
        final OutClass.Inner2 inner2 = new OutClass.Inner2();

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                inner.method2();
            }
        },"a");

        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                inner.method3(inner2);
            }
        },"b");

        Thread th3 = new Thread(new Runnable() {
            @Override
            public void run() {
                inner2.method();
            }
        },"c");


        /**
         * 不同的内部类之间的同步方法与同步块之间，如果锁定的不是同一个对象，则执行的时候是异步的。
         *
         * 所以在将任何数据类型作为同步锁时，就要注意，是否有多个线程同时持有相同的锁对象。如果是同一对象则这些线程之间就是同步
         * 的。如果是不同的对象，则它们之间就是异步的。
         *
         * 另外要注意一下，只要锁的对象不变，即使对象中的属性、变量值被改变，则它是不影响同步效果的，多线程仍是同步执行。
         */
        th1.start();
        th2.start();
        th3.start();
    }

    public static void main(String[] args) {
        //t1();
        t2();
    }
}


class OutClass {
    static class Inner{
        public void method1(){
            synchronized (new Object()){
                for(int i=0;i<10;i++){
                    System.out.println(Thread.currentThread().getName()+" == "+i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public synchronized void method2(){
            for(int i=0;i<10;i++){
                System.out.println(Thread.currentThread().getName()+" = method2 = "+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void method3(Inner2 inner2){
            synchronized (inner2){
                for(int i=0;i<10;i++){
                    System.out.println(Thread.currentThread().getName()+" = method3 = "+i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Inner2{
        public synchronized void method(){
            for(int i=0;i<10;i++){
                System.out.println(Thread.currentThread().getName()+" = inner2 = "+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}