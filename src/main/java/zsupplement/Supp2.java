package zsupplement;

import java.util.zip.GZIPOutputStream;

/**
 * 可以把线程归属到某一个线程组中，线程组中可以有线程对象，也可以有线程组，组中还可以有线程。这种结构类似于树的形式。
 * 线程组的作用是：可以批量的管理线程或线程组对象，有效地对线程或线程组对象进行组织。
 *
 * 线程组：线程组存在的意义，首要原因是安全。java默认创建的线程都是属于系统线程组，而同一个线程组的线程是可以相互修改对方的数
 * 据的。但如果在不同的线程组中，那么就不能“跨线程组”修改数据，可以从一定程度上保证数据安全。
 *
 * 线程池：它存在的意义首要作用是效率。线程的创建和结束都需要耗费一定的系统时间（特别是创建），不停创建和删除线程会浪费大量的
 * 时间。所以，在创建出一条线程并使其在执行完任务后不结束，而是使其进入休眠状态，在需要用时再唤醒，那么 就可以节省一定的时间。
 * 如果这样的线程比较多，那么就可以使用线程池来进行管理。保证效率。
 *
 * 线程组和线程池共有的特点：
 * 1，都是管理一定数量的线程
 * 2，都可以对线程进行控制---包括休眠，唤醒，结束，创建，中断（暂停）--但并不一定包含全部这些操作。
 */
public class Supp2 {

    /**
     * 线程对象关联线程组：1 级关联：
     * 所谓 1 级关联就是父对象中有子对象，但并不创建子孙对象。此情况经常出现在开发中，例如创建一个线程组，并把部分线程归属到该组中。
     * 这样就可以对零散的线程对象进行有效的组织和规划。
     */
    public static void t1(){
        SThread2 a = new SThread2();
        SThread2 b = new SThread2();
        ThreadGroup group = new ThreadGroup("hyman-");
        Thread thread = new Thread(group,a);
        Thread thread2 = new Thread(group,b);

        // 特别要注意，调用启动线程时，要调用 Thread。
        thread.start();
        thread2.start();
        System.out.println("活动的线程数为："+group.activeCount());
        System.out.println("线程组的名称为："+group.getName());

        // 也可以使用构造方法的方式，将线程加入到线程组中
        SThread3 c = new SThread3(group,"Thread-c");
        SThread3 d = new SThread3(group,"Thread-d");
        c.start();
        d.start();
        System.out.println("活动的线程数为："+group.activeCount());
        System.out.println("线程组的名称为："+group.getName());
    }

    /**
     * 线程对象关联线程组：多级关联：
     * 所谓多级关联就是父对象中有子对象，子对象中再创建子对象，也就是出现了孙对象。此情况在开发中不常出现，因为如果线程树结构
     * 设计得非常复杂反而不利于线程对象的管理了。
     */
    public static void t2(){
        ThreadGroup maingroup = Thread.currentThread().getThreadGroup();
        // 将自定义的线程组放入到 main 主线程组中，即显式的添加。
        ThreadGroup group = new ThreadGroup(maingroup,"SELF-GROUP");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println(Thread.currentThread().getName()+" == run method");
                    // 线程必须在运行状态下时，才可以受到组管理
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 将自定义线程放入到指定的线程组中
        Thread thread = new Thread(group,runnable);
        thread.setName("aaa");
        // 线程必须启动后才能归属到指定的组中
        thread.start();

        /**
         * enumerate 方法用于将每个活动线程的线程组及其子组复制到指定的数组中。
         * 此方法使用 activeCount 方法来估计数组应该有多大。如果数组的长度太短而无法容纳所有线程，则会以静默方式忽略额外的线程。
         * 此方法返回放入数组的线程数。
         */
        ThreadGroup[] listgroup = new ThreadGroup[Thread.currentThread().getThreadGroup().activeGroupCount()];
        Thread.currentThread().getThreadGroup().enumerate(listgroup);
        System.out.println("main 线程中的子线程组数："+listgroup.length+"；名称为："+listgroup[0].getName());

        Thread[] listthread = new Thread[listgroup[0].activeCount()];
        listgroup[0].enumerate(listthread);
        System.out.println(listthread[0].getName());
    }

    /**
     * 线程组的自动归属特性：是指线程自动归属到当前线程组中
     */
    public static void t3(){
        System.out.println("A 线程:"+Thread.currentThread().getName()+"，所属的线程组为："+Thread.currentThread()
                .getThreadGroup().getName()+"，子线程组数量："+Thread.currentThread().getThreadGroup().activeGroupCount());

        // 新自定义的线程或是线程组，如果没有指定，则会自动归属到当前线程对象所属的线程组中，即它是隐式的添加的。
        ThreadGroup group = new ThreadGroup("新线程组");

        System.out.println("B 线程："+Thread.currentThread().getName()+"，所属的线程组为："+Thread.currentThread()
                .getThreadGroup().getName()+"，子线程组数量："+Thread.currentThread().getThreadGroup().activeGroupCount());

        ThreadGroup[] groups = new ThreadGroup[Thread.currentThread().getThreadGroup().activeGroupCount()];
        Thread.currentThread().getThreadGroup().enumerate(groups);
        for(int i=0; i<groups.length; i++){
            System.out.println("第一个线程组名为："+groups[i].getName());
        }

        /**
         * 输出之后可以看到：
         * JVM 的根线程组（即当前 main 主线程组的父线程组）就是 system，而再向上取父线程组就会出现空异常（也就是已经没有了）。
         */
        System.out.println("main 线程所在的线程组的父线程组名称为："+Thread.currentThread().getThreadGroup().getParent()
                .getName());

        System.out.println("main 线程所在的线程组的父线程组的父线程组名称为："+Thread.currentThread().getThreadGroup()
                .getParent().getParent().getName());
    }

    /**
     * 线程组内的线程批量停止
     */
    public static void t4(){
        try {
            ThreadGroup group = new ThreadGroup("my-group");
            for(int i=0; i<5; i++){
                SThread3 thread3 = new SThread3(group,"线程-"+(i+1));
                thread3.start();
            }
            Thread.sleep(3000);
            group.interrupt();
            System.out.println("调用 interrupt 方法中止线程组 ===");
        } catch (InterruptedException e) {
            System.out.println("线程组被强制中止 ===");
            e.printStackTrace();
        }
    }

    /**
     * 递归与非递归取得组内对象
     */
    public static void t5(){
        ThreadGroup maingroup = Thread.currentThread().getThreadGroup();
        // 将自定义的线程组放入到 main 主线程组中，即显式的添加。
        ThreadGroup group = new ThreadGroup(maingroup,"SELF-GROUP");
        ThreadGroup groupson = new ThreadGroup(group,"SELF-SON");

        // 分配空间，但不一定全部用完
        ThreadGroup[] groups = new ThreadGroup[Thread.currentThread().getThreadGroup().activeGroupCount()];
        // 传入 true 是递归取得组内子组及子孙组对象
        Thread.currentThread().getThreadGroup().enumerate(groups,true);

        for(int i=0; i<groups.length; i++){
            if(groups[i] != null){
                System.out.println(groups[i].getName());
            }
        }
    }

    /**
     * 使线程具有有序性：
     * 正常情况下，线程在运行时多个线程之间执行任务的时机是无序的。可以通过改造使它们具有有序性。
     */
    public static void t6(){
        Object object = new Object();
        SThread4 thread = new SThread4(object,"A",1);
        SThread4 thread2 = new SThread4(object,"B",2);
        SThread4 thread3 = new SThread4(object,"C",0);
        thread.start();
        thread2.start();
        thread3.start();
    }

    public static void main(String[] args) {
        // 线程对象关联线程组：1 级关联
        //t1();
        // 线程对象关联线程组：多级关联
        //t2();
        // 线程及线程组的自动归属
        //t3();
        // 线程组内的线程批量停止
        //t4();
        // 递归与非递归取得组内对象
        //t5();
        // 使线程具有有序性
        t6();
    }
}


class SThread2 extends Thread {

    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName()+" == new");
            // 线程必须在运行状态下时，才可以受到组管理
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class SThread3 extends Thread {

    public SThread3(ThreadGroup group,String name){
        super(group,name);
    }

    //@Override
    //public void run() {
    //    try {
    //        System.out.println(Thread.currentThread().getName()+" == new 2");
    //        Thread.sleep(2000);
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //}

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" == 开始死循环");
        while (!this.isInterrupted()){

        }
        System.out.println(Thread.currentThread().getName()+" == 死循环结束");
    }
}

class SThread4 extends Thread {
    private Object lock;
    private String showChar;
    private int numPosition;
    // 统计打印了几个字母
    private int count;
    volatile private static int addNum = 1;

    public SThread4(Object lock,String showChar,int numPosition){
        this.lock = lock;
        this.showChar = showChar;
        this.numPosition = numPosition;
    }

    @Override
    public void run() {
        try {
            synchronized (lock){
                while (true){
                 if(addNum % 3 == numPosition){
                     System.out.println(Thread.currentThread().getName()+"，打印次数 = "+addNum+"，打印批数 = "+count+"，输出 = "+showChar);
                     lock.notifyAll();
                     addNum++;
                     count++;

                     /**
                      * 在这里的 addNum，count 的值不相同：
                      * 是因为 addNum 是静态变量，只要加载该类时就一同加载了，并且只加载一次。之后再操作也是在原对象基础上操作的。
                      * 而 count 是基于对象的变量，也就是创建几个对象，就有几个 count，并且它们之间不共享。
                      *
                      * 所以 addNum 会一直累加，而 count 只对它归属的对象操作它时，它才会变动。
                      */
                     System.out.println(addNum+" ==");
                     System.out.println(count+" ==");
                     if(count == 3){
                         break;
                     }
                 }else {
                     lock.wait();
                 }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}