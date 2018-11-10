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
    private List list = ListObject.list;

    synchronized public void push(){
        try {
            //if(list.size() == 1){
            //    this.wait();
            //}

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
            //if(list.size() == 0){
            //    System.out.println("pop 操作中，线程 wait == "+Thread.currentThread().getName());
            //    this.wait();
            //}

            // 当处于一生产，多消费的操作栈模式时：就需要解决 wait 条件改变判断，与线程假死的问题。使用 while。
            if(list.size() == 0){
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


