package communication;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class WaitDemo3 {

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
         * 其 activeCount/activeCount/enumerate 方法均为不精确的统计，建议仅用于信息目的。
         *
         * enumerate 方法，用于获得当前活动线程的引用并对其进行操作。将此线程组即其子组中的所有活动线程复制到指定数组中。
         */
        Thread[] tarray = new Thread[Thread.currentThread().getThreadGroup().activeCount()];
        Thread.currentThread().getThreadGroup().enumerate(tarray);

        for(int i=0; i<tarray.length; i++){
            System.out.println(tarray[i].getName()+" "+tarray[i].getState());
        }
    }

    public static void main(String[] args) {
        // 一对一的生产消费操作栈模式
        t1();
        // 在此例中，设计出多生产者和多消费者模式，那么在运行中就会出现 ‘假死’ 的情况。
        //t2();
    }
}


class ProdConsum1 {
    private List list = ListObject.list;

    synchronized public void push(){
        try {
            if(list.size() == 1){
                this.wait();
            }
            list.add("== "+Math.random()*10);
            this.notify();
            System.out.println("push == "+Thread.currentThread().getName()+" == "+list.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized public void pop(){
        String value = "";
        try {
            if(list.size() == 0){
                System.out.println("pop 操作中，线程 wait == "+Thread.currentThread().getName());
                this.wait();
            }
            value = list.get(0)+"";
            System.out.println("pop == "+Thread.currentThread().getName()+" == "+list.size());
            list.remove(0);
            this.notify();
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


