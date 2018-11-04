package zfunction;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class DownUtil {

    // 定义下载资源的路径及文件名
    private String path;
    // 指定所下载的文件的保存路径及文件名
    private String targetFile;
    // 定义下载的线程对象
    private DownThread[] threads;
    // 定义下载的文件的总大小
    private int fileSize;
    // 定义需要使用多少线程下载资源
    private int threadNum;
    // 线程计数同步辅助
    private CountDownLatch latch;

    public DownUtil(String path, String targetFile, int threadNum, CountDownLatch latch) {
        this.path = path;
        this.threadNum = threadNum;
        // 初始化threads数组
        threads = new DownThread[threadNum];
        this.targetFile = targetFile;
        this.latch = latch;
    }

    public void download() throws Exception {

        // 生成一个网络路径，以便于网络连接
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty(
                "Accept",
                "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
                        + "application/x-shockwave-flash, application/xaml+xml, "
                        + "application/vnd.ms-xpsdocument, application/x-ms-xbap, "
                        + "application/x-ms-application, application/vnd.ms-excel, "
                        + "application/vnd.ms-powerpoint, application/msword, */*");
        conn.setRequestProperty("Accept-Language", "zh-CN");
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");

        int code = conn.getResponseCode();
        if(code == 200){
            // 得到数据的长度，即文件大小
            fileSize = conn.getContentLength();
            System.out.println("文件总长度："+fileSize);
        }
        conn.disconnect();

        if(fileSize > 0){
            // 平均每个线程下载的文件大小，计算每个线程下载的开始位置与结束位置。这里不必一定要加1，不加1也可以
            int currentPartSize = fileSize / threadNum + 1;

            /**
             * RandomAccessFile 类的父类是Object，没有继承字节流、字符流家族中任何一个类。并且它实现了 DataInput、DataOutput
             * 这两个接口，也就意味着这个类既可以读也可以写。适用于多线程下载与上传，随机读写文件。
             *
             * 1、它是JAVA I/O流体系中功能最丰富的文件内容访问类，它提供了众多方法来访问文件内容。
             * 2、由于可以自由访问文件的任意位置，所以如果需要访问文件的部分内容，RandomAccessFile将是更好的选择。
             * 3、可以用来访问保存数据记录的文件，文件的记录的大小不必相同，但是其大小和位置必须是可知的。
             *
             * 以下就是在客户端本地创建出来一个大小跟服务器端一样大小的临时文件。可读可写
             */
            RandomAccessFile file = new RandomAccessFile(targetFile, "rw");

            // 设置本地文件的大小
            file.setLength(fileSize);
            file.close();

            for (int i = 0; i < threadNum; i++) {
                // 计算每条线程的下载的开始位置，即是以倍数为开始位置
                int startPos = i * currentPartSize;
                // 每个线程下载的结束位置
                int endPos = startPos + currentPartSize - 1;
                if (i == threadNum-1) {
                    //最后一个线程下载的长度稍微长一点
                    endPos = fileSize;
                }
                System.out.println("线程" + i + "下载:" + startPos + "字节~" + endPos + "字节");
                threads[i] = new DownThread(i, startPos, endPos);
                threads[i].start();
            }
        }
    }

    private class DownThread extends Thread {
        // 线程号
        private int threadId;
        // 当前线程的下载位置
        private int startIndex;
        // 当前线程的下载结束位置
        private int endIndex;

        public DownThread(int threadId, int startIndex, int endIndex) {
            this.threadId = threadId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            try {
                System.out.println("线程" + threadId + "正在下载...");

                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //请求服务器下载部分的文件的指定位置
                conn.setRequestProperty("Range", "bytes=" + startIndex + "-" + endIndex);
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                System.out.println("线程" + threadId + "请求返回code=" + code);

                if(code == 200){
                    InputStream in = conn.getInputStream();
                    RandomAccessFile raf = new RandomAccessFile(targetFile, "rwd");
                    //随机写文件的时候定义开始写的位置，定位文件
                    raf.seek(startIndex);

                    int len = 0;
                    byte[] buffer = new byte[1024];
                    while ((len = in.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                    in.close();
                    raf.close();
                    System.out.println("线程" + threadId + "下载完毕");
                    //计数值减一
                    latch.countDown();
                }else {
                    throw new RuntimeException("线程" + threadId + "下载异常！");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }

        }
    }
}
