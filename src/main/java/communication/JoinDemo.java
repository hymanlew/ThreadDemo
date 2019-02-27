package communication;


/**
 * 在很多情况下，主线程创建并启动子线程，如果子线程中的业务需要大量的耗时运算，则主线程往往将早于子线程结束。这时，如果主线程
 * 想等待子线程执行完之后再结束，就要用到 join 方法了。
 * 该方法的作用就是等待线程对象销毁才执行之后的代码，在此之前就进行无限期的阻塞。即它具有使线程排队运行的作用，类似同步。
 *
 * join 与 synchronized 的区别是：前者是在内部使用 wait 方法进行等待。而后者使用的是 ‘对象监视器’ 进行同步。
 *
 * join(long) 方法使当前线程等待指定的线程固定的毫秒数，超过这个时间，就会自动继续执行。但如果不设值，则当前线程会一直阻塞直到
 * 指定的线程（调用 join 的线程）执行完毕。但同样的 sleep 方法也可以使线程等待指定的时间。两者在运行效果上并没区别，其区别主要
 * 是来自这两个方法对同步的处理上：
 *
 * join(long) 是在内部使用 wait 方法实现的，所以该方法具有释放锁的特点（因为 wait 方法会快速的释放锁）。此时其他线程就可以调用该线程
 * 内的方法了。
 * 而 sleep(long) 方法却不释放锁，它会使得当前线程一直进行等待。如果是锁定了对象，则其他线程也不能访问该对象的其他同步方法。
 */
public class JoinDemo {

    public static void t1(){
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int num = (int) (Math.random()*10);
                        System.out.println(num);
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("等线程运行完毕之后，再执行！");
    }

    public static void t2(){
        try {
            Thread th1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for(int i=0; i<20; i++){
                            System.out.println(i);
                            Thread.sleep(500);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread th2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        th1.start();
                        th1.join();
                        System.out.println("等 th1 线程运行完，再执行 th2 线程！");
                    } catch (InterruptedException e) {
                        System.out.println("th2 线程被中止！");
                        e.printStackTrace();
                    }
                }
            });
            th2.start();

            Thread.sleep(3000);
            Thread th3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    th2.interrupt();
                }
            });
            th3.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void t3(){
        try {
            /**
             * 测试本案例必须使用继续 Thread 的方式创建对象，不能使用 new Thread 的方式。
             *
             * 因为 Thread 类与 Runnable 接口是父子的关系，而 bthread 在执行时会先 new 一个 Runnable 对象，然后再执行其内部
             * 的 run 方法。所以 bthread 线程锁定的是一个 Runnable 对象。
             * 而 athread 虽然也是先 new 一个 Runnable 对象，但其 run 方法内部使用的是同步块，并且指定了锁对象是 bthread 对
             * 象。
             * 所以这两个线程在执行时锁定的是两个对象（即 bthread 线程锁定 Runnable，而 athread 线程锁定 bthread对象,不是同
             * 一个）。所以在同一个主线程中执行时是异步的关系。达不到本例的要求。
             *
             * 但是如果其他线程都是在一个大的主线程中运行时，并从多次的运行结果可以得出，join 方法会先抢到锁（即 main 结束打印
             * 总是后来输出，如果没有 join 方法则总是会第一行输出）。join 抢到锁后，由于 wait 方法就又立即释放锁。然后 a线程
             * 抢到锁然后正常执行，执行完后此时 join 已经超时了，所以主线程会与 b线程争抢锁，当某一方抢到锁后然后正常执行。
             *
             * 之所以 a，b，主线程会同步输出，是因为它们都是在一个大的主线程中，并且争抢的是一把锁。
             *
             */
            Bthread bthread = new Bthread();
            Athread athread = new Athread(bthread);

            athread.start();
            bthread.start();
            bthread.join(2000);
            System.out.println("main 线程结束！");

            //try {
            //    Thread bthread = new Thread(new Runnable() {
            //        @Override
            //        synchronized public void run() {
            //            // 因为程序首先是要执行 main 方法，即 joindemo 类先执行，把代码编译成对象。所以这里的 this 代表的是本类对象
            //            //System.out.println("B this == "+this);
            //            try {
            //                System.out.println("线程 B 开始 == "+Thread.currentThread().getName());
            //                Thread.sleep(5000);
            //                System.out.println("线程 B 结束 == "+Thread.currentThread().getName());
            //            } catch (InterruptedException e) {
            //                e.printStackTrace();
            //            }
            //        }
            //    });
            //
            //
            //    Thread athread = new Thread(new Runnable() {
            //        @Override
            //        public void run() {
            //            // 因为程序首先是要执行 main 方法，即 joindemo 类先执行，把代码编译成对象。所以这里的 this 代表的是本类对象
            //            //System.out.println("A this == "+this);
            //            //System.out.println("A == "+bthread);
            //            try {
            //                synchronized (bthread){
            //                    System.out.println("线程 A 开始 == "+Thread.currentThread().getName());
            //                    Thread.sleep(5000);
            //                    System.out.println("线程 A 结束 == "+Thread.currentThread().getName());
            //                }
            //            } catch (InterruptedException e) {
            //                e.printStackTrace();
            //            }
            //        }
            //    });
            //
            //    Bthread b = new Bthread();
            //    System.out.println("1 == "+bthread);
            //    System.out.println("2 == "+athread);
            //    System.out.println("3 == "+b);
            //    bthread.start();
            //    athread.start();
            //    bthread.join(2000);
            //    System.out.println("main 线程结束！");
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // join 原理
        //t1();
        // join 与 interrupt 方法同时运行时，会抛出异常，但不影响其他线程正常执行
        //t2();
        // join 与 interrupt 方法同时运行时，会抛出异常，但不影响其他线程正常执行
        t3();
    }
}

class Bthread extends Thread {
    @Override
    synchronized public void run() {
        System.out.println("B == "+this);
        try {
            System.out.println("线程 B 开始 == "+Thread.currentThread().getName());
            Thread.sleep(5000);
            System.out.println("线程 B 结束 == "+Thread.currentThread().getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Athread extends Thread {
    private Bthread b;
    public Athread(Bthread b) {
        this.b = b;
    }

    @Override
    public void run() {
        System.out.println("A == "+b);
        try {
            synchronized (b){
                System.out.println("线程 A 开始 == "+Thread.currentThread().getName());
                Thread.sleep(5000);
                System.out.println("线程 A 结束 == "+Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}