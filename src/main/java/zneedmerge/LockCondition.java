package zneedmerge;

import java.util.ArrayList;
import java.util.List;

/**
 * ReentrantLock -- Condition 的功能类似传统线程安全中的 wait，notify 的功能，用于线程间通信。在等待 Condition 时，允许发生
 * ‘虚假唤醒’ 这通常作为基础同步线程的让步。对于大多数应用程序，这带来的实际影响很小，因为 Condition 应该总是在一个循环中被等
 * 待，并测试正被等待的状态声明。某个实现可以随意移除可能的虚假唤醒，但建议总是假定这些虚假唤醒可能发生，因此总是在一个循环中等待。
 *
 * 它使用await()替换wait()，用signal()替换notify()，用signalAll()替换notifyAll()，传统线程的通信方式，Condition都可以实现，
 * 要注意 Condition是被绑定到Lock上的，要创建一个Lock的Condition必须用newCondition()方法。
 *
 * Condition的强大之处在于，对于一个锁，我们可以为多个线程间建立不同的Condition（即对象监视器），即可以设置多路等待和通知。线
 * 程对象可以注册在指定的Condition中，从而可以有选择的进行线程通知，在调度线程上更加灵活。
 * 而synchronized就相当于整个Lock对象中只有一个单一的Condition对象，所有的线程都注册在这个对象上。线程开始notifyAll时，需要通
 * 知所有的WAITING线程，没有选择权，会有相当大的效率问题。
 */
public class LockCondition {

    public static void t1(){
        ProdConsum1 prod = new ProdConsum1();
        Service service = new Service(prod);

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    service.pushService();
                }
            }
        });
        th1.setName("push 线程");
        th1.start();

        Thread th2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    service.popervice();
                }
            }
        });
        th2.setName("pop 线程");
        th2.start();
    }

    public static void t2(){
        ProdConsum1 prod = new ProdConsum1();
        Service service = new Service(prod);

        Thread th1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    service.pushService();
                }
            }
        });
        th1.setName("push 线程");
        th1.start();

        Thread[] threads = new Thread[3];
        for(int i=0; i<threads.length; i++){
            threads[i] =  new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        service.popervice();
                    }
                }
            });
            threads[i].setName("pop "+i+"线程");
            threads[i].start();
        }
    }

    public static void t3(){
        ProdConsum1 prod = new ProdConsum1();
        Service service = new Service(prod);

        Thread[] threads = new Thread[3];
        for(int i=0; i<threads.length; i++){
            threads[i] =  new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        service.pushService();
                    }
                }
            });
            threads[i].setName("push "+i+"线程");
            threads[i].start();
        }


        Thread th2  =  new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    service.popervice();
                }
            }
        });
        th2.setName("pop 线程");
        th2.start();

    }

    public static void t4(){
        ProdConsum1 prod = new ProdConsum1();
        Service service = new Service(prod);

        Thread[] threads = new Thread[5];
        for(int i=0; i<threads.length; i++){
            threads[i] =  new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        service.pushService();
                    }
                }
            });
            threads[i].setName("push "+i+"线程");
            threads[i].start();
        }


        Thread[] threadsp = new Thread[5];
        for(int i=0; i<threadsp.length; i++){
            threadsp[i] =  new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        service.popervice();
                    }
                }
            });
            threadsp[i].setName("pop "+i+"线程");
            threadsp[i].start();
        }
    }

    public static void main(String[] args) {
        // 一对一的生产消费操作栈模式
        //t1();
        // 一生产，多消费的操作栈模式：需要解决 wait 条件改变判断，与线程假死的问题（在 t1 源码的基础上修改）
        //t2();
        // 多生产，一消费的操作栈模式（在 t2 源码的基础上修改）
        //t3();
        // 多对多的生产消费操作栈模式（在 t3 源码的基础上修改）
        t4();
    }
}


class ProdConsum1 {
    private List list = new ArrayList();

    synchronized public void push(){
        try {
            // 当处于一生产，多消费的操作栈模式时：就需要解决 wait 条件改变判断，与线程假死的问题。使用 while。
            while (list.size() == 1){
                this.wait();
            }
            list.add("== "+Math.random()*10);

            //this.notify();
            this.notifyAll();
            System.out.println("push == "+Thread.currentThread().getName()+" == "+list.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized public void pop(){
        String value = "";
        try {
            // 当处于一生产，多消费的操作栈模式时：就需要解决 wait 条件改变判断，与线程假死的问题。使用 while。
            while (list.size() == 0){
                System.out.println("pop 操作中，线程 wait == "+Thread.currentThread().getName());
                this.wait();
            }
            value = list.get(0)+"";
            System.out.println("pop == "+Thread.currentThread().getName()+" == "+list.size());
            list.remove(0);

            //this.notify();
            this.notifyAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class Service {
    private ProdConsum1 prodCon;

    public Service(ProdConsum1 prodConsum1){
        this.prodCon = prodConsum1;
    }

    public void pushService(){
        prodCon.push();
    }

    public void popervice(){
        prodCon.pop();
    }
}


