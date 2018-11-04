package zfunction;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownUtil2 {

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

    public DownUtil2(String path, String targetFile, int threadNum) {
        this.path = path;
        this.threadNum = threadNum;
        // 初始化threads数组
        threads = new DownThread[threadNum];
        this.targetFile = targetFile;
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
                // 每个线程使用一个RandomAccessFile进行下载
                RandomAccessFile currentPart = new RandomAccessFile(targetFile, "rw");
                // 定位该线程的下载位置
                currentPart.seek(startPos);
                // 创建下载线程
                threads[i] = new DownThread(i, startPos, currentPartSize, currentPart);
                // 启动下载线程
                threads[i].start();
            }
        }
    }

    // 获取下载的完成百分比
    public double getCompleteRate() {
        // 统计多条线程已经下载的总大小
        int sumSize = 0;
        for (int i = 0; i < threadNum; i++)
        {
            sumSize += threads[i].length;
        }
        // 返回已经完成的百分比
        return sumSize * 1.0 / fileSize;
    }

    private class DownThread extends Thread {
        // 当前线程的下载位置
        private int startPos;
        // 定义当前线程负责下载的文件大小
        private int currentPartSize;
        // 当前线程需要下载的文件块
        private RandomAccessFile currentPart;
        // 定义已经该线程已下载的字节数
        public int length;
        public int threadId;

        public DownThread(int threadId, int startPos, int currentPartSize,RandomAccessFile currentPart) {
            this.startPos = startPos;
            this.currentPartSize = currentPartSize;
            this.currentPart = currentPart;
            this.threadId = threadId;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
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

                int code = conn.getResponseCode();
                if(code == 200){
                    InputStream inStream = conn.getInputStream();

                    // 跳过startPos个字节，表明该线程只下载自己负责哪部分文件。
                    inStream.skip(this.startPos);
                    byte[] buffer = new byte[1024];
                    int hasRead = 0;

                    // 读取网络数据，并写入本地文件
                    while (length < currentPartSize && (hasRead = inStream.read(buffer)) != -1) {
                        currentPart.write(buffer, 0, hasRead);
                        // 累计该线程下载的总大小
                        length += hasRead;
                    }
                    currentPart.close();
                    inStream.close();
                }else {
                    throw new RuntimeException("线程" + threadId + "下载异常！");
                }
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
