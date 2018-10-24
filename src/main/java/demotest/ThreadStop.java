package demotest;

public class ThreadStop {
    /**
     * 在 java 中有 3 种方法可以终止正在运行的线程：
     * 1，使用退出标志，使线程正常退出，即 run 方法完成后即终止。
     * 2，用 Thread.stop 方法，但最好不用。因为它是不安全的，可能会产生不可预期的结果。而且它与 suspend，resume 都是已被弃
     *    用作废的 deprecated。
     * 3，所以大多数是用 Thread.interrupt 方法中断线程，但它不会终止一个正在运行的线程，还需要加入一个判断才可以停止。
     *
     * 并且 interrupt 方法也不像 for-break 那样马上就停止循环。该方法只是让当前线程中断。
     */

    public static void t1(){
        try {
            Mthread mthread = new Mthread();
            mthread.start();
            Thread.sleep(10);

            // 当前自定义的线程
            mthread.interrupt();
            System.out.println("==== "+mthread.getName()+" 是否中断 == "+mthread.isInterrupted());

            // main 主线程
            Thread.currentThread().interrupt();
            System.out.println("==== "+Thread.currentThread().getName()+" 是否中断 == "+Thread.interrupted());
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t2(){
        try {
            Mthread mthread = new Mthread();
            mthread.start();
            Thread.sleep(5000);
            mthread.stop();

            // 当前自定义的线程
            mthread.interrupt();
            System.out.println("==== "+mthread.getName()+" 是否中断 == "+mthread.isInterrupted());

            // main 主线程
            Thread.currentThread().interrupt();
            System.out.println("==== "+Thread.currentThread().getName()+" 是否中断 == "+Thread.interrupted());
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){
        try {
            Mthread mthread = new Mthread();
            mthread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // interrupt 方法测试终止线程
        //t1();
        // 线程外部使用 stop 方法
        //t2();
        // 线程内部使用 stop 方法
        t3();
    }
}

class Mthread extends Thread{

    //@Override
    //public void run() {
    //    super.run();
    //    for (int i=0;i<100000;i++){
    //        System.out.println(i);
    //    }
    //}

    //@Override
    //public void run() {
    //    super.run();
    //    for (int i=0;i<100000;i++){
    //        if(this.isInterrupted()){
    //            System.out.println("手动终止线程！");
    //            break;
    //        }
    //        System.out.println(i);
    //    }
    //    System.out.println("并没有完全终止线程！");
    //}

    //@Override
    //public void run() {
    //    super.run();
    //    try {
    //        for (int i=0;i<100000;i++){
    //            if(this.isInterrupted()){
    //                System.out.println("手动终止线程！");
    //                throw new InterruptedException();
    //            }
    //            System.out.println(i);
    //        }
    //        System.out.println("并没有完全终止线程！");
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //        System.out.println("异常彻底终止线程！");
    //    }
    //}

    //@Override
    //public void run() {
    //    super.run();
    //    try {
    //        System.out.println("线程启动！");
    //        Thread.sleep(5000);
    //        System.out.println("线程结束！");
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //        // 此时的线程状态会输出 false，因为线程在沉睡中中断会抛出异常而进入 catch，同时清除停止状态。
    //        System.out.println("在沉睡中被终止线程！是否中断："+this.isInterrupted());
    //    }
    //}

    //@Override
    //public void run() {
    //    super.run();
    //    try {
    //        for (int i=0;i<1000;i++){
    //            System.out.println(i);
    //            Thread.sleep(1000);
    //        }
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //        // 外部使用 stop 方法暴力终止线程时，不会抛出异常。
    //        System.out.println("暴力终止线程！");
    //    }
    //}

    @Override
    public void run() {
        super.run();
        try {
            for (int i=0;i<1000;i++){
                System.out.println(i);
                Thread.sleep(1000);
                if(i==5){
                    // 此方法已经作废，因为如果强制停止线程有可能使一些清理性的工作得不到完成。另一个情况是对锁定的对象进行
                    // 了解锁，导致数据得不到同步的处理，出现线程安全问题导致数据不一致。
                    this.stop();
                }
            }
        } catch (Exception e) {
            // 内部使用 stop 方法暴力终止线程时，不会抛出异常。
            System.out.println("内部暴力终止线程！");
            e.printStackTrace();

        }
    }
}