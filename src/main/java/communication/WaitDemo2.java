package communication;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果通知过早，则会打乱程序正常的运行逻辑（即在编程时需要分出先后顺序，不可以先通知再等待，这样容易造成线程一直处于 wait 状态）。
 * 另外在使用 wait/notify 时，还要注意当 wait 等待的条件发生了变化，也容易造成程序逻辑的混乱。
 *
 * 等待/通知模式最经典的就是 ‘生产者/消费者’ 模式，即 set，get 模式（原理同本例中的 add，sub）。
 * 如果在此例中，设计出多个生产者和消费者，那么在运行中极有可能出现 ‘假死’ 的情况。
 *
 * 假死，其实就是所有的线程都进入 wait 等待状态，程序不再执行任何业务，整个项目呈停止状态。这在使用生产者，消费者模式时经常遇到。
 * 这是因为虽然使用了 wait/notify 进行了通信，但由于多个同类之间是异步执行，并不能保证 notify 唤醒的一定是异类。也许是同类，比
 * 如 ‘生产者’ 唤醒 ‘生产者’，‘消费者’ 唤醒 ‘消费者’。而按照这种情况运行的比率积少成多，就会导致所有的线程都不能继续运行下去，大
 * 家都在等待，都呈 wait 状态，程序最后也就会是假死状态。
 *
 * 解决方式很简单，就是在通知时不光通知同类，也通知异类即可，即使用 notifyAll，这样就不会假死了。
 */
public class WaitDemo2 {

    public static void t1(){
        String lock = "";
        ProdConsum prod = new ProdConsum(lock);

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                prod.subtract();
            }
        });
        th1.setName("su1");
        th1.start();

        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                prod.subtract();
            }
        });
        th2.setName("su2");
        th2.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread th3 = new Thread(new Runnable() {
            @Override
            public void run() {
                prod.add();
            }
        });
        th3.setName("add");
        th3.start();
    }

    public static void t2(){
        String lock = "";
        ProdConsum prod = new ProdConsum(lock);

        Thread[] t1s = new Thread[2];
        Thread[] t2s = new Thread[2];

        for(int i=0; i<2; i++){
            t1s[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        prod.setValue();
                    }
                }
            });

            t2s[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        prod.getValue();
                    }
                }
            });

            t1s[i].setName("生产者"+(i+1));
            t2s[i].setName("消费者"+(i+1));

            t1s[i].start();
            t2s[i].start();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /**
         * 表示当前线程组中活跃的线程数，如果该线程组已停止运行，则 getThreadGroup 就无线程组就返回 null，线程数为 0。
         * 其 activeCount/enumerate 方法均为不精确的统计，建议仅用于信息目的。
         *
         * enumerate 方法，用于获得当前活动线程的引用并对其进行操作。将指定的线程组及其子组中的所有活动线程复制到指定数组中。
         */
        Thread[] tarray = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
        Thread.currentThread().getThreadGroup().enumerate(tarray);

        for(int i=0; i<tarray.length; i++){
            System.out.println(tarray[i].getName()+" "+tarray[i].getState());
        }
    }

    public static void main(String[] args) {
        // 测试使用 if 判断，当 wait 条件改变时，程序出现异常的情况
        //t1();
        // 在此例中，设计出多生产者和多消费者模式，那么在运行中就会出现 ‘假死’ 的情况。
        t2();
    }
}

class ListObject {
    public static List list = new ArrayList();
    public static String value = "";
}

class ProdConsum {
    private String lock;

    public ProdConsum(String lock){
        this.lock = lock;
    }

    public void add(){
        synchronized (lock){
            ListObject.list.add("test");
            System.out.println("已经添加 == "+ListObject.list.size());
            lock.notifyAll();
        }
    }

    public void subtract(){
        synchronized (lock){

            // 在 wait 之前的代码只会执行一次，除非是在循环体中。
            //System.out.println("begin :"+ListObject.list.size());

            try {
                /**
                 * 这里不能使用 if，因为它只会判断一次，在 wait 状态接到 notify 时，该线程就会被唤醒，就会往下继续执行代码，
                 * 而不会再进行条件判断。所以当 th1 线程正常删除 list 元素后，th2 线程再删除元素就会 ‘下标越界异常’。
                 *
                 * 而使用 while 时，即使当前线程已经被唤醒，只要它处在一个循环体中，它继续往下执行完循环内的代码后，还是会循
                 * 环判断当前的条件。这样就可以避免这种问题。
                 *
                 * 同时使用 while，也可以防止线程被伪唤醒，即没有接到 notify 而自动唤醒。
                 */
                //if(ListObject.list.size() == 0){
                //    System.out.println("check begin == "+ListObject.list.size()+" == "+Thread.currentThread().getName());
                //    lock.wait();
                //}

                while (ListObject.list.size() == 0){
                    System.out.println("check begin == "+ListObject.list.size()+" == "+Thread.currentThread().getName());
                    lock.wait();
                    System.out.println("end wait ====");
                }
                ListObject.list.remove(0);
                System.out.println("check end == "+ListObject.list.size()+" == "+Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("final :"+ListObject.list.size());
        }
    }

    public void setValue(){
        synchronized (lock){
            try {
                while (!ListObject.value.equals("")){
                    System.out.println("生产者 wait == "+Thread.currentThread().getName());
                    lock.wait();
                }
                System.out.println("生产者已产出 == "+Thread.currentThread().getName());

                // 该函数是返回纳秒的，只能用于计算时间差，不能用于计算距离现在的时间。因为是纳秒太小了。1毫秒=1纳秒*1000*1000
                ListObject.value = System.nanoTime()+"";

                //lock.notify();
                lock.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getValue(){
        synchronized (lock){
            try {
                while (ListObject.value.equals("")){
                    System.out.println("消费者 wait == "+Thread.currentThread().getName());
                    lock.wait();
                }
                System.out.println("消费者可消费 == "+Thread.currentThread().getName());
                ListObject.value = "";

                //lock.notify();
                lock.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}




