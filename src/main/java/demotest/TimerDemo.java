package demotest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 在 JDK 中 timer 类主要负责计划任务的功能，也就是在指定的时间开始执行某一个任务。但封装任务的类却是 TimerTask 类。
 * TimerTask 是一个抽象类，并实现了 Runnable 接口，所以可以执行任务。任务代码要放入其子类中。
 *
 * TimerTask 是以队列的方式一个一个被顺序执行的，所以执行的时间有可能和预期的时间不一致。因为前面的任务有可能消耗的时间较长，
 * 则后面的任务运行的时间也会被延迟。
 */
public class TimerDemo {
    /**
     * 但尽量不要直接使用 timer 类，因为多线程并行处理定时任务时，Timer 运行多个 TimerTask 时，只要其中之一没有捕获抛出的异常，
     * 则其他任务也都会自动终止运行。
     * 使用 ScheduledExecutorService 则没有这个问题。
     *
     * 并且 Timer 的构造方法中，其实就是创建一个新的线程来执行任务。并且该线程会一直运行，但不是守护线程。
     * 通过构造方法中加入 boolean 值，来创建守护线程（传入 true）。
     */
    private static Timer timer = new Timer();
    private static Timer timer2 = new Timer(true);

    //ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
    private static ScheduledExecutorService service = new ScheduledThreadPoolExecutor(3);

    public static void t1(){
        try {
            Mytask mytask = new Mytask();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 如果执行任务的计划时间早于当前时间，则任务会立即执行。如果晚于当前时间，则会到指定的时间才执行任务。
            String dates = "2018-11-16 13:22:10";
            Date date = format.parse(dates);
            System.out.println("字符串时间："+date+"，当前时间："+new Date());

            // 运行如果相同，都是会一直运行，无论是否为守护线程。
            //timer.schedule(mytask,date);
            timer2.schedule(mytask,date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void t2(){
        try {
            Mytask mytask = new Mytask();
            Mytask2 mytask2 = new Mytask2();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String dates = "2018-11-16 13:22:10";
            Date date = format.parse(dates);
            System.out.println("字符串时间："+date+"，当前时间："+new Date());

            String dates2 = "2018-11-16 13:33:10";
            Date date2 = format.parse(dates2);
            System.out.println("字符串时间："+date2+"，当前时间："+new Date());

            timer.schedule(mytask,date);
            // 在指定的时间执行任务之后，再按照指定的间隔时间周期性的无限循环地执行。
            timer.schedule(mytask2,date2,3000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void t3(){
        try {
            Mytask mytask = new Mytask();
            Mytask3 mytask2 = new Mytask3();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String dates = "2018-11-16 13:22:10";
            Date date = format.parse(dates);
            System.out.println("字符串时间："+date+"，当前时间："+new Date());

            timer.schedule(mytask,date,2000);
            timer.schedule(mytask2,date,2000);

            // Timer 的 cancel 方法的作用是将任务队列中的全部任务都清除。但如果是在并发且没有延迟的情况下，有时 Timer 对象
            // 会因为没有抢到 queue 队列锁，而使得任务继续执行。
            Thread.sleep(8000);
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void t4(){
        Mytask mytask = new Mytask();
        Date date = new Date();

        // 该方法的作用是以当前时间为基础，延迟指定的毫秒数后执行一次。
        timer.schedule(mytask,2000);

        // 该方法的作用是以当前时间为基础，延迟指定的毫秒数后，再以指定的间隔时间无限次的执行某一任务。
        timer.schedule(mytask,2000,2000);

        /**
         * 该方法与 schedule 方法都会按顺序执行，所以不用考虑非线程安全的问题。这两个方法主要的区别只在于不延时的情况：
         *
         * schedule 方法：如果执行任务时没有被延时，则下一次任务的执行时间参考的是上一次任务的 ‘开始’ 时间来计算的。
         * scheduleAtFixedRate 方法：如果执行任务时没有被延时，则下一次任务的执行时间参考的是上一次任务的 ‘结束’ 时间来计算的。
         *
         * 延时的情况则没有区别，就是两个方法如果执行任务时被延时，则下一次任务的执行时间参考的是上一次任务的 ‘结束’ 时间来计算的。
         */
        timer.scheduleAtFixedRate(mytask,2000,2000);
    }

    public static void main(String[] args) {
        // timer 执行任务
        //t1();
        // timer 允许执行多个任务
        //t2();
        // timer 清除自身的任务
        t3();
    }
}

class Mytask extends TimerTask {
    @Override
    public void run() {
        System.out.println("定时任务 1 开始 == "+new Date());
    }
}

class Mytask2 extends TimerTask {
    @Override
    public void run() {
        System.out.println("定时任务 2 开始 == "+new Date());
    }
}

class Mytask3 extends TimerTask {
    @Override
    public void run() {
        System.out.println("定时任务 3 开始 == "+new Date());
        // TimerTask 的 cancel 方法的作用是将自身从任务队列中清除，但其他任务不受影响。
        this.cancel();
    }
}