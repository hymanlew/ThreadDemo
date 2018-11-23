package instruct;

import java.util.Arrays;

public class Main {
    /**
     * Thread 类实现了 Runnable 接口，它们之间具有多态关系。并且为了支持多继承，可以一边实现 thread类一边实现 runable 接口。
     * 这两种方式创建的线程在工作时的性质是一样的，没有本质的区别。
     *
     * Thread[main,5,main] 是主线程的表示方式，中括号里的第一个值为当前主线程的名字，第二个为线程级别，第三个为线程组。
     * Thread[Thread-0,5,main]，Thread-0为另一个启动的线程，级别为5，属于线程组main。
     *
     *
     * yield 方法作用是放弃当前的 CPU 资源，让给其他线程去使用。但放弃延续的时间不确定，有可能刚刚放弃，马上又获得 CPU 时间片。
     *
     * 线程可以划分优先级，优先级高的获得的 CPU 资源就多，也就是 CPU 会优先执行优先级高的线程中的任务。有 1，5，10 三个等级的常量。
     * 设置线程优先级有助于帮 “线程规划器” 确定在下一次选择哪一个线程来优先执行。使用 setPriority 方法。
     *
     * 线程的优先级具有继承性，比如 A 线程启动了 B 线程，则 B 跟 A 的优先级相同。
     *
     * 优先级具有规则性：
     * 虽然可以设置线程的优先级，但也只是优先级高的线程优先执行，而不是全部执行完才执行优先级低的线程。并且当线程的优先级差距很大时，
     * 谁先执行完和代码的调用（编码）顺序无关。
     *
     * 优先级具有随机性：
     * 优先级高的线程一般先执行完任务（与代码的调用（编码）顺序无关），但这个也不是绝对的，因为也不是每一次都先执行完。
     *
     *
     * sleep执行后线程进入阻塞状态
     * yield执行后线程进入就绪状态
     * join执行后线程进入阻塞状态
     *
     * 要用到共同数据（包括同步锁）或共同算法的若干方法应该归在同一类身上，这种设计正好体现了高类聚和程序的健壮性。
     * 并且锁是要加在线程访问的执行资源类的内部方法中，而不是加在线程代码中，以实现高类聚性。
     */


    /**
     * {@link java.lang.ThreadGroup}示例
     *
     * 1，线程组表示一个线程的结合。此外线程组也可以包含其他线程组。线程组构成一棵树。在树中，除了初始线程组外，每个线程组都有一
     *    个父线程组。
     *
     * 2，每个线程产生时，都会被归入某个线程组(Java中每个线程都是属于某个线程组)，视线程是在那个线程组中产生而定。如果没有指定，
     *    则归入产生该子线程的线程的线程组中。(如在main中初始化一个线程，未指定线程组，则线程所属线程组为main)。
     *
     * 3，线程一旦归入某个组就无法更换组。
     *
     * 4，main线程组的parent是system线程组，而system线程组的parent为null。(参考ThreadGroup的私有构造方法)。也就是说初始线程组为
     *    system。以 system/main 衍生出一颗树。
     *
     * 5，其 ThreadGroup.activeCount / ThreadGroup.enumerate 方法均为不精确的统计，建议仅用于信息目的。
     *
     *    前者返回此线程组中活动线程的估计数，结果并不能反映并发活动(因为多线程并发运行，所以不是很精确。多线程的不确定性，如 add
     *    (某一新增线程启动) / remove (某一现有线程销毁))。
     *    固有的不精确性，建议只用于信息。从源码的实现看，其计算数目只是取了一个groupsSnapshot(syncrhonized)，即当前的快照。
     *
     *    后者将此线程组即其子组中的所有活动线程复制到指定数组中。可事先使用 activeCount 方法获取数组大小的估计数。如果数组太小而
     *    无法保持所有线程，则忽略额外的线程。（可额外校验该方法的返回值是否严格小于参数list的长度）。因为此方法固有的竞争条件(源
     *    码实现也是取了一个 Snapshot(syncrhonized))，建议仅用于信息目的。
     *
     * 6，可通过 enumerate 获得当前活动线程的引用并对其进行操作。
     *
     * 7，允许线程访问有关自己的线程组的信息，使用 getThreadGroup() 方法。但不允许它访问有关其线程组的父线程组或其他任何线程组的信息。
     *
     * 8，线程组的某些方法，将对线程组机器所有子组的所有线程执行，如 ThreadGroup 的 interrupt()。
     *
     * 9，public class ThreadGroup implements Thread.UncaughtExceptionHandler，线程组类实现了 UncaughtExceptionHandler方法。即
     *
     *    当Thread因未捕获的异常而突然中止时,调用处理程序的接口.当某一线程因捕获的异常而即将中止时,JVM将使用UncaughtExceptionHandler
     *    查询该线程以获得其 UncaughtExceptionHandler 的线程并调用处理程序的 uncaughtException 方法，将线程和异常作为参数传递。如
     *    果某一线程为明确设置其 UncaughtExceptionHandler，则将它的 ThreadGroup 对象作为 UncaughtExceptionHandler。如果ThreadGroup
     *    对象对处理异常没有特殊要求,可以将调用转发给 Thread 的 getDefaultUncaughtExceptionHandler() 方法。
     *
     * 10，线程是独立执行的代码片断，线程的问题应该由线程自己来解决，而不要委托到外部。基于这样的设计理念，在Java中，线程方法的异常
     *   （无论是 checked 还是 unchecked exception），都应该在线程代码边界之内（run方法内）进行try catch并处理掉。换句话说，我们不能
     *    捕获从线程中逃逸的异常。
     *
     * 11，参考 Thread 的 dispatchUncaughtException，该方法是一个私有方法，在异常逃逸时调用.判断线程自身是否设置了uncaughtExceptionHandler。
     *    如果没有则直接返回group，即自己的所在的线程组，而线程组实现了UncaughtExceptionHandler接口。 Thread 的 getUncaughtExceptionHandler()。
     *
     */


    /**
     * 线程是操作系统中独立的个体，但这些个体如果不经过特殊处理就不能成为一个整体。线程间的通信就是成为整体的改用方案之一。这样系统
     * 之间的交互性会更强大，大大的提高 CPU 的利用率。
     *
     * 读写锁：分读锁，写锁。读锁之间不互斥，写锁之间要互斥，读写锁之间要互斥。这是 JVM 控制的，只需加上对应的锁即可。
     *
     * Java里面内置锁 (synchronized) 和 Lock(ReentrantLock) 都是可重入的。
     */

    public static void main(String[] args) {
        /**
         * 冒泡排序是从左到右，依次两两比对，直到最后把需要的值放到最后。
         * 然后再循环经过两两比对，直到最后把需要的值放到上一次值的前一位（即 length-i-1）。
         * 最后完成 length-1 次的比对，即可拿到想要的值。
         */
        int[] nums = {1,5,9,6,4,2,3};

        for(int i=0; i<nums.length; i++){
            for(int k=0; k<nums.length-i-1; k++){
                if(nums[k]>nums[k+1]){
                    int a = nums[k+1];
                    nums[k+1] = nums[k];
                    nums[k] = a;
                }
            }
        }
        System.out.println(Arrays.toString(nums));

        /**
         * 冒泡排序是从左到右，把拿到的数与其后面所有的数依次比对，直到最后把需要的值放到最前面。
         * 然后再循环比对所有的数，及依次比对其后面的数，最后把需要的值放到上一次值的后一位（即 length-i-1）。
         * 最后完成 length-1 次的比对，即可拿到想要的值。
         */
        for(int i=0; i<nums.length; i++){
            for(int k=i+1; k<nums.length-i-1; k++){
                if(nums[i]>nums[k]){
                    int a = nums[k];
                    nums[k] = nums[i];
                    nums[i] = a;
                }
            }
        }
        System.out.println(Arrays.toString(nums));
    }
}
