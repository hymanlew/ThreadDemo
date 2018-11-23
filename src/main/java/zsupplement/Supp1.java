package zsupplement;

/**
 * 线程对象在不同的运行时期有不同的状态，状态信息就存在于 state 枚举类（java.lang.Thread.State）中。
 * 调用与线程有关的方法是造成线程状态改变的主要原因。
 */
public class Supp1 {

    private static SThread thread = new SThread();

    /**
     * new 状态：          是指线程实例化后还未执行 start 方法时的状态。
     * runnable 状态：     是指线程进入运行时的状态。
     * terminaterd 状态：  是指线程被销毁时的状态。
     *
     * 构造方法中的状态 = RUNNABLE = main（代表是 main 主线程去创建了新线程）
     * main 方法中新线程的状态1 = NEW
     * run 方法中的状态1 = RUNNABLE = Thread-0（代表是新建的线程的状态）
     * run 方法中的状态2 = TIMED_WAITING（代表是线程的 sleep 状态）
     * main 方法中新线程的状态2 = TERMINATED
     */
    public static void t1(){
        try {
            System.out.println("main 方法中新线程的状态1 = "+thread.getState());
            Thread.sleep(1000);
            thread.start();

            Thread.sleep(1000);
            System.out.println("run 方法中的状态2 = "+thread.getState());

            Thread.sleep(3000);
            System.out.println("main 方法中新线程的状态2 = "+thread.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * main 方法中 thread2 线程的状态 = BLOCKED（代表是 thread2 线程等待锁的状态）
     * main 方法中 thread2 线程的状态 = WAITING（代表是线程执行 wait 方法后的状态）
     */
    public static void t2(){
        try {
            thread.setName("a");
            thread.start();

            SThread thread2 = new SThread();
            thread2.setName("b");
            thread2.start();

            Thread.sleep(1000);
            System.out.println("main 方法中 thread2 线程的状态 = "+thread2.getState());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //t1();
        t2();
    }
}


class SThread extends Thread {

    public SThread(){
        System.out.println("构造方法中的状态 = "+Thread.currentThread().getState()+" = "+Thread.currentThread().getName());
    }

    //@Override
    //public void run() {
    //    try {
    //        System.out.println("run 方法中的状态1 = "+Thread.currentThread().getState()+" = "+Thread.currentThread().getName());
    //        Thread.sleep(3000);
    //
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}

    //@Override
    //public void run() {
    //   Sservice.method();
    //}

    @Override
    public void run() {
        try {
            // 使用 wait，notify 方法时，必须声明一个监视器对象
            synchronized (Sservice.BYTE){
                Sservice.BYTE.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Sservice {
    synchronized static public void method(){
        try {
            System.out.println(Thread.currentThread().getName()+" == 执行业务");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final Byte BYTE = new Byte("0");
}