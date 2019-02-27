package communication;

import java.util.Random;
import java.util.concurrent.*;

/**
 * concurrent /kernkarent/：并发的。
 * executor /igzekiuter/：执行者。
 *
 *
     int corePoolSize：该线程池中核心线程数最大值。
     核心线程：线程池新建线程的时候，如果当前线程总数小于corePoolSize，则新建的是核心线程，如果超过corePoolSize，则新建
     的是非核心线程。核心线程默认情况下会一直存活在线程池中，即使这个核心线程啥也不干(闲置状态)。
     如果指定 ThreadPoolExecutor 的 allowCoreThreadTimeOut 这个属性为 true，那么核心线程如果不干活(闲置状态)的话，超过
     一定时间(时长下面参数决定)，就会被销毁掉。

     int maximumPoolSize： 该线程池中线程总数最大值。线程总数 = 核心线程数 + 非核心线程数。

     long keepAliveTime：该线程池中非核心线程闲置超时时长。一个非核心线程，如果不干活(闲置状态)的时长超过这个参数所设定的
     时长，就会被销毁掉，如果设置 allowCoreThreadTimeOut = true，则会作用于核心线程。

     TimeUnit unit：keepAliveTime的单位，它是一个枚举类型，其包括：
     NANOSECONDS ： 1微毫秒 = 1微秒 / 1000
     MICROSECONDS ： 1微秒 = 1毫秒 / 1000
     MILLISECONDS ： 1毫秒 = 1秒 /1000
     SECONDS ： 秒
     MINUTES ： 分
     HOURS ： 小时
     DAYS ： 天

     BlockingQueue workQueue：该线程池中的任务队列：维护着等待执行的 Runnable 对象。当所有的核心线程都在干活时，新添加
            的任务会被添加到这个队列中等待处理，如果队列满了，则新建非核心线程执行任务。常用的workQueue类型：

     SynchronousQueue：这个队列接收到任务的时候，会直接提交给线程处理，而不保留它，如果所有线程都在工作怎么办？那就新建
             一个线程来处理这个任务！所以为了保证不出现 ‘线程数达到了 maximumPoolSize 而不能新建线程’ 的错误，使用这个类
             型队列的时候，maximumPoolSize一般指定成Integer.MAX_VALUE，即无限大。

     LinkedBlockingQueue：即链表类型的阻塞队列。这个队列接收到任务的时候，如果当前线程数小于核心线程数，则新建线程(核心
             线程)处理任务；如果当前线程数等于核心线程数，则进入队列等待。由于这个队列没有最大值限制，默认是 Integer.MAX_
             VALUE，即无限大。即所有超过核心线程数的任务都将被添加到队列中，这也就导致了maximumPoolSize的设定失效，从而
             可能就耗费内存非常大导致OOM。
             也可以自定义队列任务量（使用构造方法传值），当队列满时，就创建新的线程，但如果超过了最大线程数就会抛异常。

     ArrayBlockingQueue：可以限定队列的长度，接收到任务的时候，如果没有达到corePoolSize的值，则新建线程(核心线程)执行任
             务，如果达到了则入队等候，如果队列已满，则新建线程(非核心线程)执行任务，又如果总线程数到了maximumPoolSize，并
             且队列也满了，则发生错误。

     DelayQueue：队列内元素必须实现Delayed接口，这就意味着你传进去的任务必须先实现Delayed接口。这个队列接收到任务时，首先
            先入队，只有达到了指定的延时时间，才会执行任务。

     ThreadFactory threadFactory：创建线程的方式，这是一个接口，new 的时候需要实现它的 Thread newThread(Runnable r)方
            法，一般用不上。

     RejectedExecutionHandler handler：它是抛出异常专用的，比如上面提到的两个错误发生了（线程数达到了 maxiSize），就会
            由这个 handler 抛出异常，根本用不上。
 *
 *
 * 在手动创建线程池时，要特别注意一点，一定要设置非核心线程闲置超时时长。否则非核心线程创建即销毁，没有线程接收任务就会抛异常。
 *
 *
 * 说明：Executors各个方法的弊端：
 * 1）newFixedThreadPool（固定线程） 和 newSingleThreadExecutor（单线程）：核心线程数 = 最大线程数，但因为其默认队列大小是
 *    整数的最大值，所以如果过多堆积的请求处理，队列可能会耗费非常大的内存，甚至OOM。
 * 2）newCachedThreadPool 和 newScheduledThreadPool：因为线程数最大数是Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至OOM。
 *
 */
public class ThreadPoolDemo {

    public static void mainNote(){
        /**
         * 创建固定线程数量的线程池：
         * 不能使用下面的这种方式，而是要手动去创建。因为 Executor 其方法的源码就是使用的 ThreadPoolExecutor。
         * 线程池不允许使用Executors去创建，而是通过ThreadPoolExecutor的方式，这样的处理方式可以更加明确线程池的运行规则，规
         * 避资源耗尽的风险。
         *
         * 而阿里提示的创建线程或线程池时需要指定线程名称，是过时的方法，已经被删除了。线程池会自己定义名称。
         * 也可以重写 newThread 方法定义名称。
         */
        ExecutorService pool = Executors.newFixedThreadPool(3);

        ThreadFactory namefactory = Executors.defaultThreadFactory();

        namefactory = new ThreadFactory() {
            private int index = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "线程-"+(index++));
            }
        };

        ThreadPoolExecutor pooln = new ThreadPoolExecutor(3, 10,
                5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10),namefactory,new ThreadPoolExecutor.AbortPolicy());

        /**
         * 创建线程数量动态变化的缓存线程池：
         * 1，这种线程池内部没有核心线程，线程的数量是有没限制的。
         * 2，在创建任务时，若有空闲的线程时则复用空闲的线程，若没有则新建线程。
         * 3，没有工作的线程（闲置状态）在超过了60S还不做事，就会销毁。
         */
        ExecutorService pool2 = Executors.newCachedThreadPool();

        for(int i=0; i<20; i++) {
            pooln.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(Thread.currentThread().getName() + " == ");
                        //Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("核心线程数" + pooln.getCorePoolSize());
            System.out.println("线程池数" + pooln.getPoolSize());
            System.out.println("队列任务数" + pooln.getQueue().size());
            System.out.println("================");
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("======== 最后 ========");
        System.out.println("核心线程数" + pooln.getCorePoolSize());
        System.out.println("线程池数" + pooln.getPoolSize());
        System.out.println("队列任务数" + pooln.getQueue().size());

        pooln.shutdown();
    }

    public static void t1(){
        // 固定线程数量的线程池。
        //ExecutorService pool = new ThreadPoolExecutor(3, 3,
        //        0L, TimeUnit.MILLISECONDS,
        //        new LinkedBlockingQueue<Runnable>());

        // 线程数量动态变化的缓存线程池
        //ExecutorService pool = new ThreadPoolExecutor(0, 100,
        //        60L, TimeUnit.SECONDS,
        //        new SynchronousQueue<Runnable>());

        // 单线程的线程池，其优点是始终会保持有一个活跃的线程在工作。即如果当前线程死掉，它就会重新创建一个新的线程来工作
        ExecutorService pool = new ThreadPoolExecutor(1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>());

        for(int i=0; i<10; i++) {
            // 在子循环中调用私有变量，则此外部的私有变量需要声明为 final 的。
            final int task = i;
            System.out.println(task+"==============");
            // 每执行一次，则代表线程池执行一次任务
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < 10; k++) {
                        try {
                            System.out.println(Thread.currentThread().getName() + " == " + k+" for "+task);
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        System.out.println("任务完成 ===");

        /**
         * 因为使用的是线程池，线程会循环利用，只会回收不会销毁。所以一旦运行程序不会自动停止，而是一直等待。需要手动停止。
         * shutdown()，任务完成则停止。shutdownNow()，立即停止。
         */
        pool.shutdown();
    }

    public static void t2(){
        /**
         * 使用线程池启动定时器：
         * schedule（调用一个 runnable 对象执行任务，间隔时间，时间单位），该方法只会在指定的时间之后执行一次。
         * scheduleAtFixedRate（调用 runnable 对象执行任务，间隔时间，之后间隔的时间，时间单位），该方法会在指定的时间之后
         * 执行一次，然后再按照指定的时间间隔有频率的执行。
         */
        //ScheduledExecutorService service = Executors.newScheduledThreadPool(3);
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(3);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("定时器执行 ===");
            }
        },2,2, TimeUnit.SECONDS);
    }

    public static void t3(){
        /**
         * callable /kellebou/：随时可偿还的
         */
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            // 执行一个任务，并等待结果
            Future future = pool.submit(new Callable<Object>() {
                @Override
                public Object call() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "hello ==";
                }
            });
            System.out.println("等待结果 ==");
            System.out.println(future.get());

            // 也可以指定 future 方法等待结果的时间，如果超时还拿不到结果，则会抛异常
            //System.out.println(future.get(1,TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool.shutdown();

        ExecutorService pool2 = Executors.newFixedThreadPool(3);
        // 使用多线程执行一组任务，并等待结果（其底层就是封装了一组 future 对象）
        CompletionService completion = new ExecutorCompletionService(pool2);
        for(int i=0; i<10; i++){
            final int index = i;
            completion.submit(new Callable() {
                @Override
                public Object call() throws Exception {
                    int millis = new Random().nextInt(3000);
                    Thread.sleep(millis);
                    return index;
                }
            });
        }
        try {
            for(int i=0; i<10; i++){
                System.out.println(completion.take().get()+" ==");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pool2.shutdown();
    }

    public static void main(String[] args) {
        // 三种线程池的使用
        //t1();
        // 线程池的定时器
        //t2();
        // 线程池的等待/通知模式功能
        //t3();
        // 优化线程池的创建方式
        mainNote();
    }
}
