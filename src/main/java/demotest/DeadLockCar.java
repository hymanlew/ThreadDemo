package demotest;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 观察死锁现象：
 * 使用 JDK 自带的监测工具，cmd -->  JDK 文件夹/bin（在 IDEA project strucs / SDKS 中可查看到）  -->  执行 jps 命令。
 * 找到当前类执行的线程 id（DeadLockCar）  -->  执行 jstack -l pid号码  -->  在最下方会提示出发现一个死锁（DeadLockCar）。
 *
 * @author hyman
 * @since 2019-12-08
 */
public class DeadLockCar extends Thread {

    protected Object myDirect;
    static ReentrantLock south = new ReentrantLock();
    static ReentrantLock north = new ReentrantLock();
    static ReentrantLock west = new ReentrantLock();
    static ReentrantLock east = new ReentrantLock();

    public DeadLockCar(Object obj){
        this.myDirect = obj;
        if(myDirect == south){
            this.setName("south");
        }
        if(myDirect == north){
            this.setName("north");
        }
        if(myDirect == west){
            this.setName("west");
        }
        if(myDirect == east){
            this.setName("east");
        }
    }

    @Override
    public void run() {
        if(myDirect == south){
            try {
                /**
                 * lockInterruptibly() 如果当前线程未被中断，则获取锁定。如果已被中断则出现异常，即中断等待锁。
                 *
                 * lock 与 lockInterruptibly 比较，区别在于：
                 * lock 优先考虑获取锁，待获取锁成功后，才响应中断。
                 * lockInterruptibly 优先考虑响应中断，而不是响应锁的普通获取或重入获取。
                 */
                west.lockInterruptibly();
                Thread.sleep(500);
                south.lockInterruptibly();

                System.out.println("cat to south has passed!");
            } catch (InterruptedException e) {
                System.out.println("cat to south is killed!");
                e.printStackTrace();
            }finally {
                /**
                 * 判断锁是否被当前线程获取了
                 */
                if(west.isHeldByCurrentThread()){
                    west.unlock();
                }
                if(south.isHeldByCurrentThread()){
                    south.unlock();
                }
            }
        }

        if(myDirect == north){
            try {
                east.lockInterruptibly();
                Thread.sleep(500);
                north.lockInterruptibly();

                System.out.println("cat to north has passed!");
            } catch (InterruptedException e) {
                System.out.println("cat to north is killed!");
                e.printStackTrace();
            }finally {
                if(east.isHeldByCurrentThread()){
                    east.unlock();
                }
                if(north.isHeldByCurrentThread()){
                    north.unlock();
                }
            }
        }

        if(myDirect == west){
            try {
                north.lockInterruptibly();
                Thread.sleep(500);
                west.lockInterruptibly();

                System.out.println("cat to west has passed!");
            } catch (InterruptedException e) {
                System.out.println("cat to west is killed!");
                e.printStackTrace();
            }finally {
                if(west.isHeldByCurrentThread()){
                    west.unlock();
                }
                if(north.isHeldByCurrentThread()){
                    north.unlock();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        DeadLockCar carsouth = new DeadLockCar(south);
        DeadLockCar carnorth = new DeadLockCar(north);
        DeadLockCar carwest = new DeadLockCar(west);
        carsouth.start();
        carnorth.start();
        carwest.start();
        Thread.sleep(1000);
    }
}
