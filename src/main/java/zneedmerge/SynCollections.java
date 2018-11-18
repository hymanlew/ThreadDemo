package zneedmerge;

import java.util.*;
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
 * concurrentHashMap：类似于 HashMap，区别就在于它是线程安全的。读读不互斥，读写，写写互斥。
 * ConcurrentSkipListMap：它是一个可排序的 map，使用时需要传入一个比较器。
 * ConcurrentSkipListSet：类似于 HashSet，区别就在于它是线程安全的。读读不互斥，读写，写写互斥。
 *
 * CopyOnWriteArrayList：
 * CopyOnWriteArraySet：，。
 *
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
        // 在 JDK 提供并发集合之前，为了保证集合线程安全通常采用的是使用集合工具类的方法生成线程安全对象。
        // 该方法返回一个线程安全的 map，其底层就是运用了 synchronized 同步块锁的技术
        Map map = Collections.synchronizedMap(new HashMap<>());

        /**
         * 另外有很重要的一点，在使用传统集合类并且遍历（Iterator）时，只能进行读操作，不能进行写操作。否则会抛出异常。
         * 这是由于 Iterator 迭代器的底层，是采用计数的方法。即是对被遍历的对象先进行元素的计数，就是对应着读操作的次数，如果
         * 还有写操作，就会造成迭代器中的数字与被遍历的对象的元素数量不相等，而抛出异常。
         *

         */
        //Collection collection = new ArrayList();

        // 则解决的办法就是使用并发包中的，线程安全的类
        Collection collection = new CopyOnWriteArrayList();
        collection.add(1);
        collection.add(2);
        collection.add(3);
        collection.add(4);

        Iterator iterator = collection.iterator();
        while (iterator.hasNext()){
            Integer id = (Integer) iterator.next();
            if(id == 2){
                collection.remove(id);
            }else {
                System.out.println(id);
            }

            /**
             * 另外还有一种情况，就是在进行写操作时，迭代器中的计数下标 cursor自动加 1，使得其值正好与被遍历的对象的元素数量相等，
             * 从而停止遍历，则对象中的元素就不会全部被遍历到，同时程序也不抛异常。（即迭代器将写操作按照读操作来计数）。
             */
            //if(id == 3){
            //    collection.remove(id);
            //}else {
            //    System.out.println(id);
            //  }
        }
        System.out.println(collection.toString());
    }

    public static void main(String[] args) {
        //t1();
        t2();
    }
}
