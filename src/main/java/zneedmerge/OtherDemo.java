package zneedmerge;

import java.util.concurrent.*;

/**
 * Semaphore 信号灯，可以维护当前访问自身的线程个数，并提供了同步机制。使用它可以控制同时访问资源的线程个数，例如实现一个文件
 * 允许的并发访问数。
 * 如果当前信号灯已经全部被并发线程占用，则其他等待的并发线程是随机获得优先机会的，也可能是按照先来后到的顺序获得机会，这取决
 * 于构造 Semaphore 对象时传入的参数选项。即 Semaphore(灯数量，boolean 公平锁);
 *
 * 单个信号量的 Semaphore 对象可以实现互斥锁的功能，并且可以是由一个线程获得锁，再由另一个线程释放锁，这个可以应用于死锁恢复的
 * 一些场合。
 *
 * CyclicBarrier 循环条件路障（可循环使用），表示大家彼此等待，等大家全部集合好后才出发。分散活动后又在指定地点集合。
 *
 * CountDownLatch 倒计时计数器，调用其对象的 countDown 方法进行倒数计算。设定某一个业务只有当所有条件都满足后（计数为 0 后）才
 * 开始执行，当到达 0 后，则所有等待者或单个等待者开始执行。
 */
public class OtherDemo {

    // 缓存线程池
    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(0,10,0,
            TimeUnit.MINUTES,new SynchronousQueue<>());

    public static void t1(){
        // 设定为三盏灯
        final Semaphore semaphore = new Semaphore(3);
        for(int i=0; i<10; i++){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // 获取当前灯并亮起
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 查看还有几盏灯可用
                    System.out.println(Thread.currentThread().getName()+" 进入，当前仍然有 "+semaphore.availablePermits()+
                            " 灯可用");

                    try {
                        Thread.sleep((long) Math.random()*100);
                        System.out.println(Thread.currentThread().getName()+" 工作中 ===");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 释放当前灯
                    semaphore.release();
                    System.out.println(Thread.currentThread().getName()+" 离开，当前有 "+semaphore.availablePermits()+
                            " 灯可用");
                }
            };
            pool.execute(runnable);
        }
    }

    public static void t2(){
        // 设定有 5 个人需要全部到达集合点
        final CyclicBarrier barrier = new CyclicBarrier(5);
        for(int i=0; i<5; i++){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // 注意单位是毫秒数
                        Thread.sleep((long) (Math.random()*10000));
                        System.out.println(Thread.currentThread().getName()+" 已经到达集合点1，当前已经有 "+barrier.getNumberWaiting()+" 个人在等！");

                        // 等待其他线程到达集合点
                        barrier.await();
                        if(barrier.getNumberWaiting()==0){
                            System.out.println("已经全部集合完毕1，然后再分散要去下一个集合点2 =====");
                        }

                        Thread.sleep((long) (Math.random()*10000));
                        System.out.println(Thread.currentThread().getName()+" 已经到达集合点2，当前已经有 "+barrier.getNumberWaiting()+" 个人在等！");

                        barrier.await();
                        System.out.println("已经全部集合完毕2，然后再分散要去下一个集合点3 =====");

                        Thread.sleep((long) (Math.random()*10000));
                        System.out.println(Thread.currentThread().getName()+" 已经到达集合点3，当前已经有 "+barrier.getNumberWaiting()+" 个人在等！");

                        barrier.await();
                        System.out.println("已经全部集合完毕3，任务全部完成 =====");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            pool.execute(runnable);
        }
    }

    public static void t3(){
        // 设定有 1 个指令，同时有 3 个人执行
        final CountDownLatch order = new CountDownLatch(1);
        final CountDownLatch answer = new CountDownLatch(3);

        for(int i=0; i<5; i++){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(Thread.currentThread().getName()+" 正准备接收指令 === ");
                        // 当计数器到达 0 时，会自动释放锁并继续往下执行
                        order.await();

                        // 之所以要等待 1 秒，是为了同步输出，防止异步执行。
                        Thread.sleep(1000);
                        System.out.println(Thread.currentThread().getName()+" 已接收指令 === ");
                        // 注意单位是毫秒数
                        Thread.sleep((long) (Math.random()*10000));
                        System.out.println(Thread.currentThread().getName()+" 执行指令结束 === ");

                        // 计数减一
                        answer.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            pool.execute(runnable);
        }

        try {
            Thread.sleep((long) (Math.random()*10000));
            System.out.println(Thread.currentThread().getName()+" 准备发布指令 === ");

            order.countDown();
            System.out.println(Thread.currentThread().getName()+" 指令已经发布，等待执行结果 === ");

            // 当计数器到达 0 时，会自动释放锁并继续往下执行
            answer.await();
            // 之所以要等待 1 秒，是为了同步输出，防止异步执行。
            Thread.sleep(2000);
            System.out.println(answer.getCount()+" == answer");
            System.out.println(Thread.currentThread().getName()+" 指令执行完毕！ === ");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Semaphore 信号灯
        //t1();
        // CyclicBarrier 循环条件路障
        //t2();
        // CountDownLatch 倒计时计数器
        t3();

        pool.shutdown();
    }
}
