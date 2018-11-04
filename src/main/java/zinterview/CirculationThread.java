package zinterview;

/**
 * 题目要求：
 *
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
    private boolean flag = true;

    public synchronized void sub(){
        // 这里也可以使用 if 判断，但是 while 更好。
        // 因为使用 wait 方法时，有可能会出现伪唤醒（即没有接到 notify 而自动唤醒）。为了防止这种情况，使用 while 则会更加安全。
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