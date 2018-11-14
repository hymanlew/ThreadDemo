package LockAbout;

/**
 * ReentrantLock -- Condition 的功能类似传统线程安全中的 wait，notify 的功能，用于线程间通信。在等待 Condition 时，允许发生
 * ‘虚假唤醒’ 这通常作为基础同步线程的让步。对于大多数应用程序，这带来的实际影响很小，因为 Condition 应该总是在一个循环中被等
 * 待，并测试正被等待的状态声明。某个实现可以随意移除可能的虚假唤醒，但建议总是假定这些虚假唤醒可能发生，因此总是在一个循环中等待。
 *
 * 它使用await()替换wait()，用signal()替换notify()，用signalAll()替换notifyAll()，传统线程的通信方式，Condition都可以实现，
 * 要注意 Condition是被绑定到Lock上的，要创建一个Lock的Condition必须用newCondition()方法。
 *
 * Condition的强大之处在于，对于一个锁，我们可以为多个线程间建立不同的Condition（即对象监视器），即可以设置多路等待和通知。线
 * 程对象可以注册在指定的Condition中，从而可以有选择的进行线程通知，在调度线程上更加灵活。
 * 而synchronized就相当于整个Lock对象中只有一个单一的Condition对象，所有的线程都注册在这个对象上。线程开始notifyAll时，需要通
 * 知所有的WAITING线程，没有选择权，会有相当大的效率问题。
 */
public class LockCondition {



    public static void main(String[] args) {

    }
}


class ProdConsum1 {




}


