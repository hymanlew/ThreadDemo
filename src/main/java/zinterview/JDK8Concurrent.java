package zinterview;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * JDK 8 对并发的新支持：
 * java的 concurrent包下提供了一些原子类，我们可以通过阅读API来了解这些原子类的用法。比如：AtomicInteger、AtomicLong、AtomicReference等。
 * 原子类方式虽然保证了线程安全，得到的值是正确的。但是它在有逻辑性的业务下时，结果的输出是有随机性的（即线程不是顺序地执行）。这是因为 addAndGet 虽然是原
 * 子的，但是方法和方法之间（add 与 addAndGet 之间）的调用却不是原子的。这就必须要同步解决了。就是在 add 方法上加上 synchronized 同步锁。
 * <p>
 * LongAdder 是和 AtomicInteger 类似的使用方式，前者是在后者的基础上进行了热点分离（对数据进行了细小拆分，即减小了锁的范围）。其原理就是使用了 CAS 更新。
 * <p>
 * CAS（compareAndSwap，比较交换），一种无锁原子算法。它包含 3 个参数 CAS（V，E，N），V表示要更新变量的值，E表示预期值，N表示新值。仅当 V值等于E值时，
 * 才会将V的值设为N，如果V值和E值不同，则说明已经有其他线程做了更新，则当前线程则什么都不做。最后 CAS 返回当前V的真实值。CAS 操作时抱着乐观的态度进行的，
 * 它总是认为自己可以成功完成操作。
 * 当多个线程同时使用CAS 操作一个变量时，只有一个会胜出，并成功更新，其余均会失败。失败的线程不会挂起，仅是被告知失败，并且允许再次尝试，当然也允许实现的线
 * 程放弃操作。基于这样的原理，CAS 操作即使没有锁，也可以发现其他线程对当前线程的干扰。
 * 与锁相比，使用CAS会使程序看起来更加复杂一些，但由于其非阻塞的，它对死锁问题天生免疫，并且线程间的相互影响也非常小。更为重要的是，使用无锁的方式完全没有
 * 锁竞争带来的系统开销，也没有线程间频繁调度带来的开销，因此他要比基于锁的方式拥有更优越的性能。
 * 简单的说，CAS 需要你额外给出一个期望值，也就是你认为这个变量现在应该是什么样子的。如果变量不是你想象的那样，哪说明它已经被别人修改过了。你就需要重新读取，
 * 再次尝试修改就好了。
 * <p>
 * CAS 的缺点：
 * CAS 看起来很吊，但他仍然有缺点，最著名的就是 ABA 问题。假设一个变量 A ，修改为 B之后又修改为 A，CAS 的机制是无法察觉的，但实际上已经被修改过了。如果在基
 * 本类型上是没有问题的，但如果是引用类型呢？这个对象中有多个变量，我怎么知道有没有被改过？解决办法就是加个版本号。每次修改就检查版本号，如果版本号变了，说
 * 明改过，就算你还是 A，也不行。
 * 在 java.util.concurrent.atomic 包中，就有 AtomicReference 来保证引用的原子性，但楼主觉得有点鸡肋，不如使用同步加互斥，可能会更加高效。
 * JDK的源码中，到处都 unSafe 的 CAS 算法，可以说如果没有CAS ，就没有 java 的并发容器。
 */
public class JDK8Concurrent {

    /**
     * CompletableFuture（完成后得到通知） 实现了 CompletionStage 接口，是 JDK8 对 Future 的增强版，支持流式调用。
     * 该类实际上跟性能上是没有什么关系的，它更多的只是功能上使用方便而已。
     */
    public static class AskThread implements Runnable {

        CompletableFuture<Integer> re = null;

        public AskThread(CompletableFuture<Integer> re) {
            this.re = re;
        }

        @Override
        public void run() {
            int myRe = 0;
            try {
                // get 方法会一直阻塞等待 future 中的值
                myRe = re.get() * re.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(myRe);
        }
    }

    public static void test1() throws Exception {

        final CompletableFuture<Integer> future = new CompletableFuture<>();
        new Thread(new AskThread(future)).start();

        // 模拟长时间的计算过程
        Thread.sleep(1000);
        // 告知业务操作完成，并开始执行 future 的业务（即线程中的运算）
        future.complete(60);
    }

    public static Integer calc(Integer a) {

        try {
            // 模拟长时间的计算过程
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a * a;
    }

    public static void test2() throws Exception {

        // 调用工厂方法，去异步执行业务。同时还可以传入一个线程池对象。
        final CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> calc(2));
        System.out.println(future.get());
    }

    public static void test3() throws Exception {

        /**
         * Void 类是一个不可实例化的占位符类，用于保存对表示 Java 关键字 void 的对象的引用。
         */
        // 流式调用：调用工厂方法，去异步执行业务。同时还可以传入一个线程池对象。
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> calc(2))
                        .thenApply((i) -> Integer.toString(i))
                        .thenApply((str) -> "\"" + str + "\"")
                        .thenAccept(System.out::println);
        System.out.println(future.get());
    }

    public static void test4() throws Exception {

        // 组合多个 CompletableFuture 对象使用：调用工厂方法，去异步执行业务。同时还可以传入一个线程池对象。
        CompletableFuture<Void> future =
                CompletableFuture.supplyAsync(() -> calc(2))
                        .thenCompose((i) -> CompletableFuture.supplyAsync(() -> calc(i)))
                        .thenApply((str) -> "\"" + str + "\"")
                        .thenAccept(System.out::println);
        System.out.println(future.get());
    }

    public static void main(String[] args) throws Exception {

        test1();
        test2();
        test3();
        test4();
    }
}
