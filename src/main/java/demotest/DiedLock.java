package demotest;

/**
 * java 线程死锁是指，不同的线程都在等待根本不可能被释放的锁，从而导致所有的任务都无法继续完成。所以，死锁是必须避免的，这会造
 * 成线程的假死。
 */
public class DiedLock {

    public static void t1(){
        /**
         * 观察死锁现象：
         * 使用 JDK 自带的监测工具，cmd -->  JDK 文件夹/bin（在 IDEA project strucs / SDKS 中可查看到）  -->  执行 jps 命令。
         * 找到当前类执行的线程 id（DiedLock）  -->  执行 jstack -l pid号码  -->  在最下方会提示出发现一个死锁（Deallock）。
         *
         * 死锁是程序设计的 bug，所以在设计时就要避免双方互相持有对方锁的情况。要特别注意死锁跟本例中的嵌套的同步块是没有关系的。
         */
        try {
            Mythreadc myth = new Mythreadc();
            myth.setName("a");
            Thread th1 = new Thread(myth);
            th1.start();

            Thread.sleep(1000);
            myth.setName("b");
            Thread th2 = new Thread(myth);
            th2.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 观察死锁现象
        t1();
    }
}

class Mythreadc implements Runnable{
    public String name;
    public Object lock1 = new Object();
    public Object lock2 = new Object();

    @Override
    public void run() {
        if(name.equals("a")){
            synchronized (lock1){
                try {
                    System.out.println("name = "+name);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2){
                    System.out.println("按照 lock1 -- lock2 顺序执行了！");
                }
            }
        }

        if(name.equals("b")){
            synchronized (lock2){
                try {
                    System.out.println("name = "+name);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock1){
                    System.out.println("按照 lock2 -- lock1 顺序执行了！");
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}