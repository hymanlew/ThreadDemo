package zinterview;

/**
 * 题目要求：
 * 共有子和父两个线程，要求子输出 1-10，父输出 1-50，子先执行然后父执行。然后再子先执行然后父执行，如果循环。
 *
 * 等待/通知模式的交叉备份：
 * 创建 20 个线程，其中 10 个将数据备份到 A 数据库，另外 10 个将数据备份到 B 数据库，并且备份时是 A,B 两个数据库
 * 同时交叉进行的。即 A 一下，B 一下。
 */
public class CirculationThread {

    public static void main(String[] args) {
        final CircuDemo circuDemo = new CircuDemo();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<3 ;i++){
                    circuDemo.sub();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<3 ;i++){
                    circuDemo.maind();
                }
            }
        }).start();
    }
}

/**
 * 要用到共同数据（包括同步锁）或共同算法的若干方法应该归在同一类身上，这种设计正好体现了高类聚和程序的健壮性。
 * 并且锁是要加在线程访问的执行资源类的内部方法中，而不是加在线程代码中，以实现高类聚性。
 */
class CircuDemo {
    volatile private boolean flag = true;

    public synchronized void sub(){
        /**
         * 这里也可以使用 if 判断，但是尽量不用，而是用 while 更好。
         * 因为使用 wait 方法时，有可能会出现伪唤醒（即没有接到 notify 而自动唤醒）。为了防止这种情况，使用 while 则会更加安全。
         *
         * 并且还有一种情况，就是 if 只会判断一次，在 wait 状态接到 notify 时，该线程就会被唤醒，就会往下继续执行代码，而不会再
         * 进行条件判断。
         * 而使用 while 时，即使当前线程已经被唤醒，只要它处在一个循环体中，它继续往下执行完循环内的代码后，还是会循环判断当前的
         * 条件。这样就可以避免这种问题。
         */
        while (!flag){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int i=1; i<11 ;i++){
            System.out.println("sub == "+i);
        }
        flag = false;
        this.notify();
    }

    public synchronized void maind(){
        // 这里也可以使用 if 判断，但是 while 更好。
        while (flag){
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 1; i < 51; i++) {
            System.out.println("main == " + i);
        }
        flag = true;
        this.notify();
    }
}