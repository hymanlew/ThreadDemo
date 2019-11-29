package zsupplement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SimpleDateFormat 类主要负责日期的转换与格式化，但在多线程中，使用此类容易造成数据转换及处理的不准确。即它是非线程安全的。
 */
public class DateFormatDemo {
    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 15, 0,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public static void t1() {
        String[] dates = {"2018-11-01", "2018-11-02", "2018-11-03", "2018-11-04", "2018-11-05", "2018-11-06", "2018-11-07",
                "2018-11-08", "2018-11-09", "2018-11-10"};

        // 虽然每个线程都会创建一个 runnable 对象，但是使用的却是同一个 dateformat 对象，所以会转换错误甚至是抛出异常。
        for (int i = 0; i < pool.getCorePoolSize(); i++) {
            System.out.println(dates[i] + " = pool");
            pool.execute(new DThread(dateFormat, dates[i]));
        }

        // 解决的办法就是创建多个 SimpleDateFormat 对象的实例，在本例中是定义了一工具类。
        for (int i = 0; i < pool.getCorePoolSize(); i++) {
            final int k = i;
            System.out.println(dates[i] + " = pool");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(dates[k] + " = runnable");
                        Date date = DateTools.parse("yyyy-MM-dd", dates[k]);
                        String ndate = DateTools.format("yyyy-MM-dd", date);
                        if (!ndate.equals(dates[k])) {
                            System.out.println(Thread.currentThread().getName() + " 转换错误，原日期：" + dates + "，转换日期：" + ndate);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // 还有一种办法就是使用 ThreadLocal 类将线程绑定到指定的 SimpleDateFormat 对象的实例，来保证其线程安全。
        for (int i = 0; i < pool.getCorePoolSize(); i++) {
            final int k = i;
            System.out.println(dates[i] + " = pool");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(dates[k] + " = runnable");
                        Date date = DateTools.getDateFormat("yyyy-MM-dd").parse(dates[k]);
                        String ndate = DateTools.getDateFormat("yyyy-MM-dd").format(date).toString();
                        if (!ndate.equals(dates[k])) {
                            System.out.println(Thread.currentThread().getName() + " 转换错误，原日期：" + dates + "，转换日期：" + ndate);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        t1();

        pool.shutdown();
    }
}


class DThread implements Runnable {
    private SimpleDateFormat sdf;
    private String dates;

    public DThread(SimpleDateFormat sdf, String dates) {
        this.sdf = sdf;
        this.dates = dates;
    }

    @Override
    public void run() {
        try {
            System.out.println(dates + " = runnable");
            Date date = sdf.parse(dates);
            String ndate = sdf.format(date).toString();
            if (!ndate.equals(dates)) {
                System.out.println(Thread.currentThread().getName() + " 转换错误，原日期：" + dates + "，转换日期：" + ndate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

class DateTools {
    public static Date parse(String parttern, String date) throws ParseException {
        return new SimpleDateFormat(parttern).parse(date);
    }

    public static String format(String parttern, Date date) {
        return new SimpleDateFormat(parttern).format(date);
    }

    private static ThreadLocal<SimpleDateFormat> local = new ThreadLocal<>();

    public static SimpleDateFormat getDateFormat(String parttern) {
        SimpleDateFormat sdf = null;
        sdf = local.get();
        if (sdf == null) {
            sdf = new SimpleDateFormat(parttern);
            local.set(sdf);
        }
        return sdf;
    }
}