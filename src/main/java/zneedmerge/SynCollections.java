package zneedmerge;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 传统方式的集合类在多并发环境下时是非线程安全的，例如 HashSet，HashMap，ArrayList 等等。
 * hashMap：键值对是唯一的，即 KEY 是唯一的，值可以重复。
 * hashSet：其底层就是使用了 hashMap，只是只保留了 key 的可用性。即 value 是固定的没有用到，存储时只是存储 key，所以能保持值唯一。
 *
 * 传统方式下用 Collections 工具类提供的 synchronizedCollection 方法来获得同步集合。
 *
 * java.util.concurrent 包下的并发集合有五种：
 * concurrentHashMap，CopyOnWriteArrayList，CopyOnWriteArraySet，ConcurrentSkipListMap，ConcurrentSkipListSet。
 */
public class SynCollections {

    public static void t1(){
        // 观察 HashSet 底层原理，它只是存储 key。
        HashSet set = new HashSet();
        set.add("1");
        set.add("2");
        set.add("1");
        set.add("2");
        System.out.println(set.toArray().toString());
    }

    public static void t2(){
        // 该方法返回一个线程安全的 map，其底层就是运用了 synchronized 同步块锁的技术
        Map map = Collections.synchronizedMap(new HashMap<>());

    }

    public static void main(String[] args) {
t1();
    }
}
