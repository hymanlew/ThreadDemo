package demotest;

public class Mythread extends Thread{
    @Override
    public void run() {
        super.run();
        System.out.println("自定义线程！");
    }
}

class MyRunable implements Runnable{
    public void run() {
        System.out.println("运行中！");
    }
}


class test{

    public static void thread(){
        Mythread mythread = new Mythread();
        mythread.start();
        System.out.println("运行结束！");
        System.out.println("=== 1 ====");
        System.out.println("=== 2 ====");
    }

    public static void runable(){
        MyRunable runable = new MyRunable();
        Thread thread = new Thread(runable);
        thread.start();
        System.out.println("运行结束！");
        System.out.println("=== 1 ====");
        System.out.println("=== 2 ====");
    }

    public static void main(String[] args) {
        thread();
        runable();
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
