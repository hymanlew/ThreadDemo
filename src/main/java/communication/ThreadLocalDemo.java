package communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ThreadLocal 中保存的数据，在多线程操作中的隔离性：
 * ThreadLocal，是用于实现线程范围内的数据共享，即对于相同的程序代码，多个模块在同一个线程中运行时要共享一份数据，而在另外线程中
 * 运行时又共享另外一份数据。即多个操作都需要在同一个事务中完成，通常也在同一个线程中处理。
 *
 * 每个线程调用全局 ThreadLocal 对象的 set 方法，就相当于往其内部的 map 中增加一条记录，key分别是各自的线程，value是各自 set
 * 传入的值。在线程结束后可以调用 clear 方法用于释放内存，不调用也可以，因为线程结束后也会自动释放相关的 ThreadLocal 变量。
 * （会自动释放没有被调用引用的数据及 ThreadLocal 对象内存）。
 * 但是当 ThreadLocals与现代应用程序服务器一起使用时，由于应用程序服务器中的线程池在线程重用的概念上工作，因此它们永远不会被垃圾
 * 收集。相反它们会被重用来处理另一个请求，这就会造成严重的内存泄漏甚至是 OOM。此外他们还使用单独的类加载器。
 *
 * 所以在不再使用 ThreadLocals时必须要清理，使用 remove（）方法，该方法删除了此变量的当前线程值。
 * 不要使用 ThreadLocal.set（null） 来清除该值，它实际上不会清除该值，而是查找与当前线程关联的 Map并将键值对设置为当前线程并分别
 * 为null。最好将 ThreadLocal 视为需要在 finally块中关闭的资源，以 确保它始终关闭，即使在异常的情况下。
 *
 * 要注意，ThreadLocal 存储的一个数据代表一个变量，它也只能放一个数据。如果有两个变量都要线程范围内共享，则就要定义两个数据对象。
 * 如果有多个变量要线程共享时，那就先定义一个对象装下这些变量，然后在 ThreadLocal 中存储这个对象即可。
 */
public class ThreadLocalDemo {

    private static int data = 0;
    // ThreadLocal 底层实现
    private static Map<Thread,Integer> safeshare = new HashMap();
    private static ThreadLocal<Integer> local = new ThreadLocal<Integer>();
    private static ThreadLocal<User> localuser = new ThreadLocal<User>();

    static class A {
        public void get(){
            int data = safeshare.get(Thread.currentThread());
            System.out.println("A = "+Thread.currentThread().getName()+" get "+data);
        }
        public void getbylocal(){
            int data = local.get();
            System.out.println("A = "+Thread.currentThread().getName()+" get "+data);
        }
        public void getbyuser(){
            User user = localuser.get();
            System.out.println("A = "+Thread.currentThread().getName()+" "+user);
        }
        public void getbyuserIn(){
            User2 user = User2.getuser();
            System.out.println("A = "+Thread.currentThread().getName()+" "+user);
        }
    }

    static class B {
        public void get(){
            int data = safeshare.get(Thread.currentThread());
            System.out.println("B = "+Thread.currentThread().getName()+" get "+data);
        }
        public void getbylocal(){
            int data = local.get();
            System.out.println("B = "+Thread.currentThread().getName()+" get "+data);
        }
        public void getbyuser(){
            User user = localuser.get();
            System.out.println("B = "+Thread.currentThread().getName()+" "+user);
        }
        public void getbyuserIn(){
            User2 user = User2.getuser();
            System.out.println("B = "+Thread.currentThread().getName()+" "+user);
        }
    }

    public static void t1(){
        // 模拟两个线程操作两个对象，当不使用 map 时并分别获取数据，由于线程的异步执行，并且是数据共享，所以数据会出错
        for(int i=0; i<2; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (this){
                        data = new Random().nextInt(10);
                        System.out.println(Thread.currentThread().getName()+" put "+data);
                        safeshare.put(Thread.currentThread(),data);
                        new A().get();
                        new B().get();
                    }
                }
            }).start();
        }
    }

    public static void t2(){
        // 使用 ThreadLocal，在实现封装时，要注意不要让外界直接操作 ThreadLocal 变量。
        for(int i=0; i<2; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 加同步锁，或者将变量声明为线程的私有变量，才会同步执行（即为每个线程创建一个独立的对象实例）
                    int data = new Random().nextInt(10);
                    System.out.println(Thread.currentThread().getName()+" put "+data);
                    local.set(data);
                    new A().getbylocal();
                    new B().getbylocal();
                }
            }).start();
        }
    }

    public static void t3(){
        for(int i=0; i<2; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String data = new Random().nextInt(10)+"";
                    User user = new User("name"+data,data);
                    System.out.println(Thread.currentThread().getName()+" put "+data);
                    localuser.set(user);
                    new A().getbyuser();
                    new B().getbyuser();
                }
            }).start();
        }
    }

    public static void t4(){
        for(int i=0; i<2; i++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String data = new Random().nextInt(10)+"";
                    User2 user = User2.getuser();
                    user.setName("name"+data);
                    user.setPwd(data);

                    System.out.println(Thread.currentThread().getName()+" put "+user);
                    new A().getbyuserIn();
                    new B().getbyuserIn();
                }
            }).start();
        }
    }

    public static void main(String[] args) {
        // 模拟两个线程操作两个对象，并分别获取数据，会出错
        //t1();
        // 使用 TreadLocal 存储一个基本类型数据
        //t2();
        // 使用 TreadLocal 存储一个对象数据
        //t3();
        // 优化对象实例结构，把 TreadLocal 对象隐藏在后面以保护对象数据
        t4();
    }
}

class User {
    private String name;
    private String pwd;

    public User() {
    }

    public User(String name, String pwd) {
        this.name = name;
        this.pwd = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}

class User2 {
    private String name;
    private String pwd;

    // 饿汉模式
    //public static User2 user = new User2();
    //public static User2 getuser(){
    //    return user;
    //}

    // 懒汉模式
    //public static User2 user = null;
    //public static synchronized User2 getuser(){
    //    if(user == null){
    //        user = new User2();
    //    }
    //    return user;
    //}

    // 懒汉模式
    private static ThreadLocal<User2> local = new ThreadLocal<>();
    //private static User2 user = local.get();

    // 使用 ThreadLocal 时,需要创建多个实例,所以不创建为单例。
    public static User2 getuser(){
        // 因为 local 为静态变量只加载一次，所以一旦 user 被赋值，它就会直接返回。而不会重新判断并创建新对象。所以要放在方法里。
        User2 user = local.get();
        if(user == null){
            user = new User2();
            local.set(user);
        }
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
