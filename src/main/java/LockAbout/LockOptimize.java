package LockAbout;

import java.util.List;
import java.util.Vector;

/**
 * 锁优化的思路和方法：
 * 1，减少锁持有的时间：例如由方法细化到代码块，将锁的持有持有时间和范围尽可能的缩小。
 * <p>
 * 2，减小锁粒度：将大对象拆成小对象，大大增加并行度，降低锁的竞争。使偏向锁，轻量级锁成功率提高。例如 Collections.synchronizedMap(map)
 * 其内部就是将 get,set 的代码块进行 synchronized 加锁。ConcurrentHashMap 其内部是将一个 map 拆成16个小的 hashmap（sgment），
 * 这样就能够允许多个线程同时进入处理，然后最后合并即可。
 * <p>
 * 3，锁分离：根据功能锁分离-ReadWriteLock，提高并发性能。读写分离思想还可以延伸，只要操作互不影响锁就可以分离，例如LinkedBlockingQueue
 * 队列，链表，其存取是从不同的端进行操作的。
 * <p>
 * 4，锁粗化：通常情况下，为了保证多线程间的有效并发，会要求每个线程持有锁的时间尽量短，即在使用完公共资源后应立即释放锁。只有这样，
 * 等待在这个锁上的其他线程才能尽早获得资源执行任务。但是凡事都有一个度，如果对同一个锁不停的进行请求，同步和释放，其本身也会消
 * 耗系统宝贵的资源，反而不利于性能的优化。
 * <p>
 * 5，锁消除：在编译器即时编译时，如果发现不可能被共享的对象，则就会消除这些对象的锁操作。使用 -server -XX:+DoEscapeAnalysis
 * -XX:+EliminateLocks 开启 server模式，
 * 第一个加号为变量逃逸分析（如果为局部变量则就没有逃逸，如果变量作用域超出了方法则就是逃逸了），第二个加号为开启锁消除，即如果
 * 没有逃逸，就自动消除锁。
 * <p>
 * 虚拟机内的锁优化：
 * 在 java 中所有的对象都有一个对象头标记 mark（32位），用于描述对象的系统信息，包含 hash，锁信息，垃圾回收标记，年龄等等。
 * 指向锁记录的指针，指向monitor的指针，GC标记，偏向锁线程ID。
 * <p>
 * <p>
 * 偏向锁：
 * 所谓的偏向就是偏心，即锁会偏向于当前已经占有锁的线程。当前占有锁的线程可以继续向下执行（而不需要不停的请求判断锁资源）。
 * 锁是一种悲观的策略，即排他性。
 * 而在某些情况时，是根本不存在线程安全问题的（即是不存在竞争的，或是竞争不激烈的），那么此时就可以通过偏向锁来提高性能。因为一旦某
 * 个线程持有偏向锁之后，在它下一次再进入执行时，就会判断自己是否处于偏向模式。如果处于偏向模式，并且自己持有偏向锁，那么就不需要进
 * 行锁的请求操作了，而是直接可以进入执行。
 * 即只要没有竞争，获得偏向锁的线程，在将来再次进入同步块时就不需要做同步，而是直接进入并执行。以此来提高系统的性能。
 * 将对象头 mark 的标记设置为偏向，并将线程 ID 写入对象头 mark。此线程就持有了偏向锁。
 * 当其他线程请求相同的锁时，偏向模式结束。因为已经开始有了竞争。
 * -XX:+UseBiasedLocking（默认启用的）。
 * 在竞争激烈的场合，偏向锁会增加系统负担。因为每次偏向模式都会被打破，偏向都会失败，这无疑会对系统产生性能上的消耗。
 */
public class LockOptimize {

    public static List<Integer> numberList = new Vector<Integer>();

    public static void pianxianLock() {
        /**
         * 手动设置开启偏向锁：
         * -XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0
         *
         * 禁用偏向锁：
         * -XX:-UseBiasedLocking
         */
        long begin = System.currentTimeMillis();
        int count = 0;
        int startnum = 0;
        while (count < 10000000) {
            numberList.add(startnum);
            startnum += 2;
            count++;
        }
        long end = System.currentTimeMillis();
        System.out.println(end - begin);
    }


    /**
     * 在此程序中并不能得到真正的 i 的值，即错误的使用了锁。
     * 这是因为 i 是一个 integer 对象，而 i++ 的执行底层是转换为 int 之后加 1，而后再把结果生成一个新 integer 对象。
     * 所以 synchronized 监视一个 integer 对象时，在多线程下，该对象就不一定是同一个 integer 对象了。
     */
    static Integer i = 0;

    public static class AddThread extends Thread {
        @Override
        public void run() {
            for (int k = 0; k < 100000; k++) {
                synchronized (i) {
                    i++;
                }
            }
        }
    }

    public static void useWrong() throws InterruptedException {
        AddThread t1 = new AddThread();
        AddThread t2 = new AddThread();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(i);
    }

    public static void main(String[] args) throws InterruptedException {

    }

}
