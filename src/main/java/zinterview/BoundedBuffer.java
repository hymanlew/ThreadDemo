package zinterview;

import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设计可缓冲队列，即设计一个缓冲区。一头接收数据，一头发送数据。
 */
public class BoundedBuffer {
    final Lock lock = new ReentrantLock();
    final Condition needClear = lock.newCondition();
    final Condition needPut = lock.newCondition();

    // 定义一个缓冲队列，默认可装下 100 个元素。测试时可以适当减小容量。
    final Object[] items = new Object[100];
    int puti,takei,count;

    public void put(Object o) throws Exception{
        lock.lock();
        try {
            while (count == items.length){
                System.out.println("缓冲区已经满了，等待取出值 ===== 满");
                needClear.await();
            }
            items[puti] = o;
            System.out.println("放入 = "+ items[puti]);

            // 当当前数组队列已经满时，即下标数字等于数组长度时，就从头开始放。虽然该队列是存取交替进行的，但是也不能完全确
            // 定之前的位置是空的。所以就需要用 count 去判断，是否是已经全满了。
            if(++puti == items.length){
                puti = 0;
            }
            ++count;
            needPut.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public Object take() throws Exception{
        lock.lock();
        try {
            while (count == 0){
                System.out.println("缓冲区已经空了，等待存入值 ===== 空");
                needPut.await();
            }
            Object o = items[takei];
            System.out.println("取出 = "+ o);

            // 如果当前数组队列已经空了，即下标数字等于数组长度时，就从头开始取。虽然该队列是存取交替进行的，但是也不能完全确
            // 定之前的位置是有数据的。所以就需要用 count 去判断，是否是已经全空了。
            if(++takei == items.length){
                takei = 0;
            }
            --count;
            needClear.signalAll();
            return o;
        }finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        BoundedBuffer boundedBuffer = new BoundedBuffer();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new LinkedBlockingDeque<>());

        for(int i=0; i<10; i++){
            final int index = i;
            System.out.println(index+"===================");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true){
                            Thread.sleep(new Random().nextInt(2000));
                            // 因为本部分代码是无限循环，无法结束本轮循环而进入到下一个 i 值，所以存入的值都是 0，1。
                            //boundedBuffer.put(index);
                            boundedBuffer.put(new Random().nextInt(100));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true){
                            Thread.sleep(new Random().nextInt(2000));
                            boundedBuffer.take();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        pool.shutdown();
    }
}
