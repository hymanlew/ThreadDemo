package ThreadForSingle;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleModelDemo2 implements Serializable{

    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new LinkedBlockingQueue<>());

    /**
     * 使用 enum 枚举数据类型实现单例模式：
     * 枚举 enum 和静态代码块的特性相似，在使用枚举类时，构造方法会被自动调用，并且也是只调用一次，也可以应用这个特性实现单例
     * 设计模式。
     *
     * 但是此段代码或者说这个调用模式（enum 的设计模式），已经将枚举类进行了曝露，违反了 ‘职责单一原则’，所以要将枚举类声明为
     * 内部类。
     */
    public static void t1(){
        for(int i=0; i<pool.getCorePoolSize(); i++){
            System.out.println(Myobject.enumFactory.getDemo().hashCode()+" == enum");

        }
    }


    /**
     * 职责单一原则：
     * 又称单一功能原则，它规定一个类应该只有一个发生变化的原因。软件开发过程中常说的高内聚低耦合就是单一职责的前身（低耦合就
     * 是指少使用继承，不同的类之间避免有直接的关联。而高内聚就是多使用实现接口的方式，或者构造方法传入对象调用的方式编码）。
     *
     * 即编码时，接口的功能是单一的，对外调用即可。而不同的对象使用时，具体执行的都是接口的实现类，而非直接调用实现类。所以该接
     * 口就是职责单一的，也是低耦合高内聚的。
     *
     * 所以要将枚举类声明为内部类以符合单一的原则，因为在其他对象调用它时，也是无法正常使用的。这就要职责单一。
     */
    public enum Myobject2 {
        enumFactory;
        private SingleModelDemo2 demo;

        private Myobject2(){
            System.out.println("调用了单一原则的 enum 枚举构造方法！");
            demo = new SingleModelDemo2();
        }

        public SingleModelDemo2 getDemo(){
            return demo;
        }
    }
    public static void t2(){
        for(int i=0; i<pool.getCorePoolSize(); i++){
            System.out.println(Myobject2.enumFactory.getDemo().hashCode()+" == enum");
        }
    }


    public static void main(String[] args) {
        // 使用静态内部类的方式，可以创建线程安全的单例对象
        //t1();
        // 使用单一原则的枚举类
        t2();
    }
}

enum Myobject {

    enumFactory;

    private SingleModelDemo2 demo;

    private Myobject(){
        System.out.println("调用了 enum 枚举构造方法！");
        demo = new SingleModelDemo2();
    }

    public SingleModelDemo2 getDemo(){
        return demo;
    }
}