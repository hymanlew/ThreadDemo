package zsupplement;


public class Supp3 {

    public static void t1(){
        /**
         * 在此线程代码中，程序会抛出异常。而使用 UncaughtExceptionHandler 类可以对多线程中的异常进行捕捉并处理。
         */
        SThread5 thread = new SThread5();
        thread.start();

        /**
         * setUncaughtExceptionHandler 方法是给指定线程对象设置异常处理器。在 Thread 类中还可以用 setDefaultUncaughtExceptionHandler
         * 方法对所有线程对象设置异常处理器。
         */
        System.out.println(" =========  =========");
        SThread5 thread2 = new SThread5();
        thread2.setName("self-thread");
        thread2.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(t.getName()+" == 出现了异常！");
                e.printStackTrace();
            }
        });
        thread2.start();
    }

    public static void t2(){
        /**
         * setUncaughtExceptionHandler 方法是给指定线程对象设置异常处理器。在 Thread 类中还可以用 setDefaultUncaughtExceptionHandler
         * 方法对所有线程对象设置异常处理器。
         */
        SThread5.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println(t.getName()+" == 出现了异常！");
                e.printStackTrace();
            }
        });
        SThread5 thread = new SThread5();
        thread.start();
    }

    public static void t3(){
        /**
         * 从本方法的运行结果来看，在默认情况下，线程组中的一个线程出现异常并不会影响其他线程正常的运行。
         * 所以如果要达到一个线程出异常，则当前组中的线程全部停止的效果，就要自定义一个线程组，
         */
        //ThreadGroup group = new ThreadGroup("self-group");
        MyGroup group = new MyGroup("self-group");

        SThread6[] threads = new SThread6[10];
        for(int i=0; i<10; i++){
            threads[i] = new SThread6(group,"线程="+(i+1),"1");
            threads[i].start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SThread6 thread = new SThread6(group,"报错线程=","a");
        thread.start();
    }

    public static void main(String[] args) {
        //t1();
        //t2();
        t3();
    }
}

class SThread5 extends Thread {
    @Override
    public void run() {
        String name = null;
        System.out.println(name.hashCode());
    }
}

class SThread6 extends Thread {

    private String num;
    public SThread6(ThreadGroup group,String name,String num){
        super(group,name);
        this.num = num;
    }

    //@Override
    //public void run() {
    //    int num2 = Integer.parseInt(num);
    //    while (true){
    //        try {
    //            Thread.sleep(300);
    //            System.out.println("死循环中 = "+Thread.currentThread().getName());
    //        } catch (InterruptedException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //}

    @Override
    public void run() {
        int num2 = Integer.parseInt(num);
        while (!(this.isInterrupted())){
            try {
                Thread.sleep(300);
                System.out.println("死循环中 = "+Thread.currentThread().getName());
            } catch (InterruptedException e) {
                throw new RuntimeException("线程被中止 ===");
            }
        }
    }
}

class MyGroup extends ThreadGroup {
    // 自定义的线程组名称
    public MyGroup(String name){
        super(name);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        super.uncaughtException(t, e);
        this.interrupt();
    }
}