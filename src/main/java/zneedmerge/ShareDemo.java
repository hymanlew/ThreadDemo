package zneedmerge;

/**
 * 如果每个线程执行的代码相同，可以使用同一个 Runnable 对象，这个 Runnable 对象中有那些共享数据。如果每个线程执行的代码不同，
 * 这时需要用不同的 Runnable 对象，有两种方式来实现这些 Runnable 对象之间的数据共享：
 *
 * 1，将共享数据封装在另外一个对象中，然后将这个对象逐一传递给各个 Runnable 对象。每个线程对共享数据的操作方法也分配到那个对象
 * 身上去完成。这样容易实现针对该数据进行的各个操作的互斥和通信。
 *
 * 2，将这些 Runnable 对象作为某一个类中的内部类，共享数据作为这个外部类中的成员变量，每个线程对共享数据的操作方法也分配给外部
 * 类，以便实现对共享数据进行的各个操作的互斥和通信，作为内部类的各个 Runnable 对象调用外部类的这些方法。
 *
 * 3，以上两种方式的组合，将共享数据封装在另外一个对象中，每个线程对共享数据的操作方法也分配到那个对象身上去完成。对象作为这个
 * 外部类中的成员变量或方法中的局部变量，每个线程的 Runnable 对象作为外部类中的成员内部类或局部内部类。
 *
 * 总之，要同步互斥的几段代码最好是分别放在几个独立的方法中，这些方法再放在同一个类中，这样比较容易实现它们之间的同步互斥和通信。
 */
public class ShareDemo {

    // 每个线程执行的代码相同时，使用同一个 Runnable 对象
    private static int count = 100;
    public static void t1(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true){
                    count--;
                }
            }
        };
        new Thread(runnable).start();
        new Thread(runnable).start();
    }

    // 第一种方式实现数据共享，创建多个 Runnable 对象
    public static void t2(){
        final ShareData data = new ShareData();
        new Thread(new Runnable() {
            @Override
            public void run() {
                data.decrement();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                data.increment();
            }
        }).start();
    }

    public static void main(String[] args) {

    }
}

class ShareData {
    private int i = 0;

    public synchronized void increment(){
        i++;
    }

    public synchronized void decrement(){
        i--;
    }
}