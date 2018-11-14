package demotest;

public class Mythread extends Thread{
    @Override
    public void run() {
        super.run();
        System.out.println("自定义线程！=== 1");
        for(int i=0; i<10; i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(i);
        }
    }
}

class MyRunable implements Runnable{
    @Override
    public void run() {
        System.out.println("运行中！");
    }
}

class Thread2 extends Thread{
    @Override
    public void run() {
        System.out.println("自定义线程！=== 2");
        for(int i=0; i<10; i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(i);
        }
        super.run();

    }
}

class test{

    public static void thread(){
        Mythread mythread = new Mythread();
        mythread.start();
        System.out.println("运行结束！");
        System.out.println("=== 1 ====");
    }

    public static void runable(){
        MyRunable runable = new MyRunable();
        Thread thread = new Thread(runable);
        thread.start();
        System.out.println("运行结束！");
        System.out.println("=== 1 ====");
    }

    public static void test(){
        Mythread mythread = new Mythread();
        mythread.start();
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread2 thread2 = new Thread2();
        thread2.start();
        System.out.println("运行结束！");
    }

    public static void main(String[] args) {
        //thread();
        //runable();

        // 此时的 super.run() 方法在前在后是没分别的，因为其实现就是调用 runnable 对象的 run 方法。但是现在是使用的继承的
        // 方式创建的线程。
        test();
    }
}

/**
 * 线程被调用的随机性（即异步执行的特性）：
 * 运行结果为先 ”结束“，后 ”自定义“。即先执行 start方法之后的代码，再执行线程任务。
 * 这说明了，在使用多线程技术时，代码运行的结果与编程的代码顺序是无关的。输出结果的顺序取决于 CPU 对线程时间片的处理速度，或是
 * start 方法后面代码运行的速度。CPU 时间片的分配是随机的，不确定的。
 * 例如，打断点查看输出，就会是 ”结束 -- 自定义 -- 1 -- 2“。
 *
 * 并且如果是多线程同时运行时，各个线程的 start 方法编码顺序并不代表线程启动的顺序。它也是经过 CPU 随机分配的，即线程的启动顺
 * 序与 start 方法执行的顺序无关。
 */
