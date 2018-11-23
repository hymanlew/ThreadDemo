package zinterview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多线程并发输出日志：
 * 多线程调用 doSome(key, value) 方法，默认时是全部异步输出的，即是同时输出。
 * 现在要求，当线程中的 key 的值相同时（即 equal 相同时），则 key 相同的线程就要同步输出，不能异步执行。
 * 并且在要求 ‘不能改动’ 的代码，不可以修改。
 */
public class BlockingQueueLog3 {

    public static void t1() {
        Test a = new Test("1", "", "1");
        Test b = new Test("2", "", "2");
        Test c = new Test("1", "", "3");
        Test d = new Test("4", "", "4");
        System.out.println("begin ===");
        a.start();
        b.start();
        c.start();
        d.start();
    }


    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 10, 0, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
    private static BlockingQueue q1 = new SynchronousQueue(true);
    private static BlockingQueue queue = new LinkedBlockingQueue();
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition put = lock.newCondition();
    private static Condition take = lock.newCondition();
    private static volatile boolean flag = true;

    public static void t2() {
        Test a = new Test("1", "", "1");
        Test b = new Test("1", "", "2");
        Test c = new Test("3", "", "3");
        Test d = new Test("4", "", "4");
        a.start();
        b.start();
        c.start();
        d.start();
    }

    public static void test() {
        String a = "a"+"";
        String b = "a"+"";
        String c = "a";
        String d = "a";

        /**
         * 此时的 a==b 为 true，这是因为 a，b 在赋值时使用的全部都常量，而常量相拼接时，java 编译器在处理其字节码时，会自动
         * 优化为 c，d 的样式（就因为是常量赋值）。
         *
         * 而 Test 类在初始化时，key 的值全部都是变量相拼接，编译器是无法优化的。所以生成的 key 对象都是不同的（就因为是变量
         * 相拼接赋值的）。
         */
        System.out.println(a==b);
        System.out.println(c==d);
    }

    public static void main(String[] args) {
        // 原始代码
        //t1();
        // 10 线程代码，使用 LinkedBlockingQueue
        t2();
        // 10 线程代码，使用 SynchronousQueue 阻塞队列集合



        //test();
        pool.shutdown();
    }
}

// 该类不能改动
class Test extends Thread {
    private TestDo2 testDo;
    private String key;
    private String value;

    public Test(String key, String key2, String value) {
        this.testDo = TestDo2.getInstance();

        /**
         * 常量 ‘1’ 和 string‘1’ 是同一对象，下面这行代码就是要用 ‘1’+‘’ 的方式产生新的对象。以实现内容没有改变（即值仍然
         * 为 1），但却不再是同一对象的效果。
         */
        this.key = key + key2;
        this.value = value;
    }

    @Override
    public void run() {
        //testDo.doSome(key, value);
        //testDo.doSome2(key, value);
        //testDo.doSome3(key, value);
        testDo.doSome4(key, value);
    }
}

class TestDo2 {
    private static TestDo2 testDo2 = new TestDo2();
    private static String lock;

    public static TestDo2 getInstance() {
        return testDo2;
    }

    public void doSome(String key, String value) {
        // 该方法块中的代码不能改动
        try {
            Thread.sleep(1000);
            System.out.println(key+" ："+value+" == "+System.currentTimeMillis()/1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void doSome2(String key, String value) {
        // 此时的 key 已经不是常量了，而是新建的对象，也就是说是不同锁，所以不能互斥。
        synchronized (key){
            // 该方法块中的代码不能改动
            try {
                Thread.sleep(1000);
                System.out.println(key+" ："+value+" == "+System.currentTimeMillis()/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    private List list = new ArrayList();
    public void doSome3(String key, String value) {
        /**
         * 所以想到一个办法，就是把线程中的 key 存到一个集合中，然后将其他线程中的 key 与集合中的 key equal 比对，只要比对成
         * 功，那就用原来的 key 作为锁，也就实现了对同一对象的同步锁功能。也就可以满足需求了。
         *
         * 有两种方法可以实现此功能（boolean 开关，contains 方法）：
         * 需要注意的是，第一种方法不能先进行集合是否为空的判断，只能直接进行遍历判断。因为在多线程并发时，访问到的 list 集合
         * 的长度就一直为空。
         *
         * 并且遍历的方式，也有两种：增强 for 循环，iterator 遍历。
         */
        Object o = key;
        boolean flag = false;
        //if(list.size()>0){
            for(Object ob : list){
                if(ob.equals(o)){
                    o = ob;
                    flag = true;
                }
            }
            if(!flag){
                list.add(o);
            }
        //}

        //if(!list.contains(o)){
        //    list.add(o);
        //}else {
        //    for(Object ob : list){
        //        if(ob.equals(o)){
        //            o = ob;
        //        }
        //    }
        //}

        //if(!list.contains(o)){
        //    list.add(o);
        //}else {
        //    for(Iterator ite = list.iterator(); ite.hasNext();){
        //        try {
        //            Thread.sleep(20);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //        Object ob = ite.next();
        //        if(ob.equals(o)){
        //            o = ob;
        //        }
        //    }
        //}
        synchronized (o){
            // 该方法块中的代码不能改动
            try {
                Thread.sleep(1000);
                System.out.println(key+" ："+value+" == "+System.currentTimeMillis()/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private CopyOnWriteArrayList clist = new CopyOnWriteArrayList();
    public void doSome4(String key, String value) {
        /**
         * 但使用 iterator 遍历的方式有一个隐患，就是当一个线程对迭代器进行迭代时，而另一个线程需要对集合进行增删操作时，程序
         * 就会抛出异常。因为 iterator 遍历时是不允许有增删操作的。
         * 即传统集合是非线程安全的，这就需要用线程安全的集合，CopyOnWriteArrayList。
         */
        Object o = key;
        if(!clist.contains(o)){
            clist.add(o);
        }else {
            for(Iterator ite = clist.iterator(); ite.hasNext();){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Object ob = ite.next();
                if(ob.equals(o)){
                    o = ob;
                }
            }
        }
        synchronized (o){
            // 该方法块中的代码不能改动
            try {
                Thread.sleep(1000);
                System.out.println(key+" ："+value+" == "+System.currentTimeMillis()/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}