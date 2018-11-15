package zinterview;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 题目要求：做一个缓存系统
 */
public class CacheDemo {

    private static Map<String,Object> cache = new HashMap();
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static Object getData(String key){
        lock.readLock().lock();
        Object value = null;
        try {
            value = cache.get(key);
            // 如果当前缓存中没有对应的数据，则就需要从数据库中的取得数据，并写入到缓存中。这就需要阻塞读操作，而改为写操作。
            // 并且写操作是互斥的。
            if(value == null){
                lock.readLock().unlock();
                lock.writeLock().lock();

                // 防止写操作时出现异常，而不能释放锁，所以要捕捉异常。并最后强制释放锁。
                try {
                    // 再次判断 value 的值是否为空。就是要防止多线程并发时，其中一个线程在写，而其他线程在写操作阻塞时，在之
                    // 后的执行中重复写入数据。
                    if(value == null){
                        value = "数据库中的数据 ==";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    lock.writeLock().unlock();
                }

                // 最后还是要加上读锁，以实现读操作可以异步并发的执行。
                lock.readLock().lock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            lock.readLock().unlock();
        }
        return value;
    }

    public static void main(String[] args) {

    }
}
