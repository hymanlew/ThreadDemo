package instruct;

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
     *
     * 静态方法使用synchronized修饰后，该方法一定具有同步效果。
     * 当静态方法与非静态方法同时声明了synchronized时，它们之间是非互斥关系的。因为静态方法锁的
     * 是类对象即当前类，而非静态方法锁的是当前方法所属的实例对象。一个线程执行静态方法时，
     * 另一个线程可以同时执行非静态方法，只有在执行同一个方法时才互斥。
     *
     */
}
