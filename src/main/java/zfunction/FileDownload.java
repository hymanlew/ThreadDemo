package zfunction;

import java.util.concurrent.CountDownLatch;

public class FileDownload {

    public static void t1(){
        int threadSize=4;
        String serverPath = "http://file.ws.126.net/3g/client/netease_newsreader_android.apk";
        String localPath = "NewsReader.apk";

        /**
         * 用给定的计数初始化 CountDownLatch。由于调用了 countDown() 方法，所以在当前计数到达零之前，await 方法会一直受阻塞。
         * 在计数完成之后，会释放所有等待的线程，await 的所有后续调用都将立即返回。这种现象只出现一次——计数无法被重置。
         * 主要用于线程计数同步辅助。
         *
         * await(long, TimeUnit);等待超时，针对某些业务场景，如果某一个线程的操作耗时非常长或者发生了异常。但是并不想影响主线
         * 程的继续执行, 则可以使用await(long, TimeUnit)方法。
         * 即一个线程(或者多个线程)，等待另外n个线程执行long时间后继续执行.
         */
        CountDownLatch latch = new CountDownLatch(threadSize);
        DownUtil m = new DownUtil(serverPath, localPath, threadSize, latch);
        long startTime = System.currentTimeMillis();
        try {
            m.download();
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("全部下载结束,共耗时" + (endTime - startTime) / 1000 + "s");
    }

    public static void t2(){

    }

    public static void main(String[] args) {

    }
}
