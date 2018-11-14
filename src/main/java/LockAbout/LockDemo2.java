package LockAbout;

/**
 * Java里面内置锁 (synchronized) 和 Lock(ReentrantLock) 都是可重入的。Reentrant /rintrent/：可重入的。
 *
 * synchronized 和 ReentrantLock 的比较：
 * 1）Lock是一个接口，而synchronized是Java中的关键字，synchronized是内置的语言实现；
 *
 * 2）synchronized在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而Lock在发生异常时，如果没有主动通过 unLock()
 * 去释放锁，则很可能造成死锁现象，因此使用Lock时需要在finally块中释放锁；
 *
 * 3）Lock可以让等待锁的线程响应中断，而synchronized却不行，使用synchronized时，等待的线程会一直等待下去，不能够响应中断；
 *
 * 4）通过Lock可以知道有没有成功获取锁，而synchronized却无法办到。
 *
 * 5）Lock可以提高多个线程进行读操作的效率。
 *
 * 总结：ReentrantLock相比synchronized，增加了一些高级的功能。但也有一定缺陷。在ReentrantLock类中定义了很多方法，比如：
 * isFair()        //判断锁是否是公平锁
 * isLocked()    //判断锁是否被任何线程获取了
 * isHeldByCurrentThread()   //判断锁是否被当前线程获取了
 * hasQueuedThreads()   //判断是否有线程在等待该锁
 *
 *
 * 两者在锁的相关概念上区别：
 * 1) 可中断锁，顾名思义，就是可以相应中断的锁。在Java中，synchronized就不是可中断锁，而Lock是可中断锁。如果某一线程A正在执行
 * 锁中的代码，另一线程B正在等待获取该锁，可能由于等待时间过长，线程B不想等待了，想先处理其他事情，我们可以让它中断自己或者在别
 * 的线程中中断它，这种就是可中断锁。
 * lockInterruptibly()的用法体现了Lock的可中断性。
 *
 * 2) 公平锁，即尽量以请求锁的顺序来获取锁。比如同是有多个线程在等待一个锁，当这个锁被释放时，等待时间最久的线程（最先请求的线程）
 * 会获得该锁（并不是绝对的，大体上是这种顺序），这种就是公平锁。
 * 非公平锁即无法保证锁的获取是按照请求锁的顺序进行的。这样就可能导致某个或者一些线程永远获取不到锁。
 * 在Java中，synchronized就是非公平锁，它无法保证等待的线程获取锁的顺序。ReentrantLock可以设置成公平锁。
 *
 * CPU在调度线程的时候是在等待队列里随机挑选一个线程，由于这种随机性所以是无法保证线程先到先得的（synchronized控制的锁就是这种
 * 非公平锁）。但这样就会产生饥饿现象，即有些线程（优先级较低的线程）可能永远也无法获取CPU的执行权，优先级高的线程会不断的强制
 * 它的资源。那么如何解决饥饿问题呢，这就需要公平锁了。
 *
 * 公平锁可以保证线程按照时间的先后顺序执行，避免饥饿现象的产生。但公平锁的效率比较低，因为要实现顺序执行，需要维护一个有序队列。
 * ReentrantLock 便是一种公平锁，通过在构造方法中传入true就是公平锁，传入false，就是非公平锁。
 *
 * 3) 读写锁，读写锁将对一个资源（比如文件）的访问分成了2个锁，一个读锁和一个写锁。正因为有了读写锁，才使得多个线程之间的读操作可
 * 以并发进行，不需要同步，而写操作需要同步进行，提高了效率。
 * ReadWriteLock就是读写锁，它是一个接口，ReentrantReadWriteLock实现了这个接口。
 *
 * 4) 绑定多个条件，一个ReentrantLock对象可以同时绑定多个Condition对象，而在synchronized中，锁对象的wait()和notify()或notifyAll()
 * 方法可以实现一个隐含的条件，如果要和多余一个条件关联的时候，就不得不额外地添加一个锁，而ReentrantLock则无须这么做，只需要多
 * 次调用new Condition()方法即可。
 *
 *
 * 3.性能比较：
 * 在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，而当竞争资源非常激烈时（即有大量线程同时竞争），此时ReentrantLock的
 * 性能要远远优于synchronized。所以说，在具体使用时要根据适当情况选择。
 *
 * 在JDK1.5中，synchronized是性能低效的。因为这是一个重量级操作，它对性能最大的影响是阻塞的是实现，挂起线程和恢复线程的操作都需
 * 要转入内核态中完成，这些操作给系统的并发性带来了很大的压力。相比之下使用Java提供的ReentrankLock对象，性能更高一些。到了JDK1.6，
 * 发生了变化，对synchronize加入了很多优化措施，有自适应自旋，锁消除，锁粗化，轻量级锁，偏向锁等等。导致在JDK1.6上synchronize的
 * 性能并不比Lock差。官方也表示，他们也更支持synchronize，在未来的版本中还有优化余地，所以还是提倡在synchronized能实现需求的情况
 * 下，优先考虑使用synchronized来进行同步。
 */
public class LockDemo2 {
}
