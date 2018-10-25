package demotest;

/**
 * 暂停线程意味着此线程还可以恢复运行。在 java 中，可以使用 suspend 暂停，用 resume 恢复线程。
 * 但是使用此两个方法时，如果使用不当，极易造成公共的同步对象的独占，使得其他线程无法正常访问和使用。并且也会导致数据的不同步
 * 情况。
 */
public class ThreadDemoStrop {

    public static void t1(){
        try {
            Threada a = new Threada();
            a.start();
            Thread.sleep(1000);
            a.suspend();
            System.out.println(System.currentTimeMillis()+" == "+a.getI());

            Thread.sleep(3000);
            System.out.println(System.currentTimeMillis()+" == "+a.getI());

            a.resume();
            System.out.println(System.currentTimeMillis()+" == "+a.getI());
            a.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t2(){
        try {
            final SyncTesta testa = new SyncTesta();

            Thread thread1 = new Thread(){
                @Override
                public void run() {
                    super.run();
                    testa.print();
                }
            };
            thread1.setName("a");
            thread1.start();

            Thread.sleep(2000);
            Thread thread2 = new Thread(){
                @Override
                public void run() {
                    super.run();
                    //testa.print();
                    String result = testa.getResult();
                    if(result.equals("")){
                        System.out.println("如果不执行被暂停的方法时，其他方法可以正常访问！但是数据会导致不一致。");
                    }
                }
            };
            thread2.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){
        try {
            Threada threada = new Threada();
            threada.start();
            Thread.sleep(1000);
            System.out.println(threada.getName()+" ======== 2");
            threada.suspend();
            System.out.println("测试系统输出！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 演示暂停与恢复
        //t1();
        // 对象方法被独占
        //t2();
        // 系统方法被独占
        t3();
    }
}

class Threada extends Thread{
    private int i =0;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    //@Override
    //public void run() {
    //    try {
    //        while (true){
    //            if(this.isInterrupted()){
    //                throw new InterruptedException("线程被终止！");
    //            }
    //            i++;
    //        }
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //        System.out.println(e.getMessage());
    //    }
    //}

    @Override
    public void run() {

        while (true){
            // 以下两种方式可以正常实现对系统方法的锁定
            //i++;
            //System.out.println(i);

            try {
                i++;
                System.out.println(i);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 以下三种方式不能正常实现对系统方法的锁定，因为内部已经对当前线程进行了操作。例如 sleep，suspend。
            // 所以当对当前线程中的方法进行锁定时，必须在外部使用时挂起，执行 suspend，不能在内部。
            //try {
            //    i++;
            //    Thread.sleep(10);
            //    System.out.println(i);
            //    if(i==80){
            //        System.out.println(Thread.currentThread().getName()+" ======== 1");
            //    }
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}

            //i++;
            //if(i==1000){
            //    System.out.println(Thread.currentThread().getName()+" ======== 1");
            //    Thread.currentThread().suspend();
            //    //System.out.println(Thread.currentThread().getName()+" ======== 1");
            //}
            //System.out.println(i);

            //i++;
            //if(i==20000){
            //    System.out.println(i);
            //    this.suspend();
            //}
            //System.out.println(i);

        }
    }
}

class SyncTesta {
    boolean flag = false;
    synchronized public void print(){
        System.out.println("开始 ======");
        if(Thread.currentThread().getName().equals("a")){
            System.out.println("本方法被 a 线程 suspend 永远的暂停了，其他线程不能再访问！但其他方法单独被访问时不受影响。");
            Thread.currentThread().suspend();
            flag = true;
        }
        System.out.println("结束 ======");
    }

    public String getResult(){
        return flag ? "stop":"";
    }
}