package communication;

import java.lang.reflect.Field;
import java.util.function.Supplier;

/**
 * 造成线程不安全的原因有两个：不想共享的变量被共享了。想共享的没及时共享。
 *
 * ThreadLocal 的实现原理：
 * 在任何代码中执行 Thread.currentThread()，都可以获取到当前执行这段代码的Thread对象。要实现线程独享变量，就在获取当前 Thread
 * 对象后，在当前Thread对象中使用一个容器来存储这些变量，这样每个线程都持有一个本地变量容器，从而做到互相不干扰。Thread类中有一
 * 个ThreadLocalMap 容器，它是一个类似HashMap的存储结构，它使用 ThreadLocal对象作为 key，当前 Thread中的变量副本作为 value。
 *
 * ThreadLocal为什么会有内存泄露问题：
 * ThreadLocal本质上只是对当前Thread对象中ThreadLocalMap对象操作的一层封装，我们始终操作的只是一个map而已。当这个map一直存活
 * (线程一直存活)，并且我们忘了清除这个map中我们已经不需要的entry，就会造成内存泄露。
 */
public class ThreadLocalMemory {

    // 获得数组中非null元素个数
    private static int getSize(Object[] objects) {
        int count = 0;
        for(Object object : objects){
            if(object != null){
                count++;
            }
        }
        return count;
    }

    // 通过反射获得ThreadLocalMap中的底层数组
    private static Object[] getEntrys() throws NoSuchFieldException, IllegalAccessException {

        Thread mainthread = Thread.currentThread();
        /**
         * Field 是一个类，位于java.lang.reflect包下。在Java反射中该类描述的是类的属性信息，功能包括：
         *
         * 1，获取当前对象的成员变量的类型。
         * 2，对成员变量重新设值。
         */
        Field threadLocals = Thread.class.getDeclaredField("threadLocals");

        // 允许通过反射访问类中的属性（私有的）
        threadLocals.setAccessible(true);

        Object threadLocalMap = threadLocals.get(mainthread);

        /**
         * Class[] getDeclaredClasses()，返回类中定义的公共、私有、保护的内部类。
         * Class[] getClasses()，        返回类定义的公共的内部类,以及从父类、父接口那里继承来的内部类。
         */
        Class<?>[] declaredClasses = ThreadLocal.class.getDeclaredClasses();

        Field table = declaredClasses[0].getDeclaredField("table");
        table.setAccessible(true);

        return (Object[]) table.get(threadLocalMap);
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        Object[] entrys = getEntrys();
        System.out.println("初始化threadLocalMap的entrys数量为："+getSize(entrys));

        /**
         * Java8中ThreadLocal对象提供了一个Lambda构造方式，实现了非常简洁的构造方法：withInitial。这个方法采用 Lambda方式传
         * 入实现了 Supplier 函数接口的参数。
         * 它是用 ThreadLocal作为容器，当每个线程访问这个指定变量时，ThreadLocal会为每个线程提供一份变量，各个线程互不影响。
         */
        ThreadLocal<String> userId = ThreadLocal.withInitial(() -> "init id");
        //ThreadLocal.withInitial(new Supplier<Object>() {
        //    @Override
        //    public Object get() {
        //        return "init id";
        //    }
        //});

        userId.set("id in main thread");
        System.out.println("设置userId后threadLocalMap的entrys数量为："+getSize(entrys));

        // 失去Threadlocal对象的强引用，并且尝试调用gc回收
        userId = null;
        System.gc();
        System.out.println("userId置null后threadLocalMap的entrys数量为："+getSize(entrys));

        /**
         * 此时 ThreadLocal 中的 entry 为弱引用，当没有强引用指向ThreadLocal对象时也会被回收，但 value（id in main thread）
         * 回收不了。问题的根源不是弱引用，而是没有把entry从map中移除。ThreadLocalMap中key的弱引用至少可以在你忘了移除ThreadLocalMap
         * 对应entry的时候帮你删除entry中的key，可以说这个弱引用有益无害。弱引用表示这个锅我不背。
         *
         * 怎么防范内存泄露：
         * 很简单，使用完毕之后，调用ThreadLocal对象的remove()方法，实际上也是对ThreadLocalMap删除entry的一层包装。
         *
         * 其实不仅仅是ThreadLocal，我们操作数组、集合、Map等任何容器。如果这个容器生命周期比较长，我们都应该注意remove掉不再
         * 需要的元素。而且Map中的key最好是不可变元素（ThreadLocal也最好为final的）。
         *
         * 线程池与ThreadLocal：
         * 线程池既然是线程独享的，那么当使用线程池的时候，每次使用完毕后remove.
         */
    }
}
