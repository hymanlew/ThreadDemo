package zinterview;

import java.util.concurrent.locks.StampedLock;

public class JDK8Concurrent2 {

    private double x, y;

    /**
     * StampedLock 是改进的读写锁，使得在高并发读时，读不会阻塞写的操作。并且在写操作完成之后，会使得再重读一次，即读取最新的数据。
     *
     * 其实现原理是 CLH 自旋锁：
     * 该锁使得在线程读失败时，不会立即挂起当前线程，而是维护一个等待线程队列，所有申请锁，但没成功的线程都记录在这个队列中。每一个节点（一个节点代表一个线程）保
     * 存一个标记位（locked），用于判断当前线程是否已经释放锁。
     * 当一个线程试图获得锁时，会取得当前等待队列的尾部节点作为其前序节点。并使用代码判断前序节点是否已经成功释放锁，while( pred.locked）死循环。
     * 但也不会进行无休止的自旋，而会在若干次自旋后挂起线程。
     */
    private final StampedLock sl = new StampedLock();

    public void move(double a, double b) {

        /**
         * StampedLock 的锁像是一个时间戳，能够记录读写的操作。
         * 其内部会维护一个时间戳，在每次加锁，释放锁的时候，都会更新这个时间戳。这样才能在写之后触发重读的操作。
         */
        long stamp = sl.writeLock();
        try {
            x += a;
            y += b;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    public double distanceFromOrigin() {

        // StampedLock 的乐观读，不会先加锁
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;

        /**
         * 并且在每次操作之前，都需要验证当前的时间戳是否有效（与自己内部的时间戳进行比对），
         * 即验证数据是否被写操作过。如果有，则之前的时间戳就是失效的。
         */
        if (!sl.validate(stamp)) {

            /**
             * 如果验证失败的话，就转回到悲观锁。限制并阻塞之后的写操作。
             * 当然也可以使用死循环不停的去获取到最新的时间戳，及数据。
             */
            stamp = sl.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }

        /**
         * 返回正确舍入的一个double值的正平方根。
         *
         * 特殊情况：
         * 如果参数是NaN或小于为零，那么结果是NaN.
         * 如果参数是正无穷大，那么结果为正无穷大.
         * 如果参数是正零或负零，那么结果是一样的参数.
         * 否则，其结果是最接近真正的数学平方根的参数值的double值。
         */
        return Math.sqrt(currentX*currentX + currentY*currentY);
    }
}
