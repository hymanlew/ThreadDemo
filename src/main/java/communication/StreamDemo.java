package communication;

import java.io.*;

/**
 * 在 java 中提供了各种各样的输入，输出流，可以方便地对数据进行操作。其中管道流（pipeStream）是一种特殊的流，用于在不同线程间
 * 直接传送数据。一个线程发送数据到输出管道，另一个线程从输入管道中读数据。通过管道不同线程间的通信，而无须借助于类似临时文件之
 * 类的东西。
 * 线程会将数据写入到“管道输出流”，而“管道输出流”又会将该数据传输给“管道输入流”，即而保存在“管道输入流”的缓冲中。
 *
 * 使用管道实现线程间通信的主要流程如下：建立输出流out和输入流in，将out和in绑定，out中写入的数据则会同步写入的in的缓冲区（实际
 * 情况是，out中写入数据就是往in的缓冲区写数据，out中没有数据缓冲区）。
 */
public class StreamDemo {

    public static void t1(){
        HandleData handleData = new HandleData();
        PipedOutputStream outputStream = new PipedOutputStream();
        PipedInputStream inputStream = new PipedInputStream();

        try {
            // 将输入输出流相关联起来，进行绑定，两种方式都可以
            //outputStream.connect(inputStream);
            inputStream.connect(outputStream);

            Thread th1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleData.write(outputStream);
                }
            });
            th1.start();

            Thread.sleep(2000);
            Thread th2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleData.read(inputStream);
                }
            });
            th2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void t2(){
        HandleData handleData = new HandleData();
        PipedWriter outputStream = new PipedWriter();
        PipedReader inputStream = new PipedReader();

        try {
            // 将输入输出流相关联起来，进行绑定，两种方式都可以
            //outputStream.connect(inputStream);
            inputStream.connect(outputStream);

            Thread th1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleData.writeStr(outputStream);
                }
            });
            th1.start();

            Thread.sleep(2000);
            Thread th2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    handleData.readStr(inputStream);
                }
            });
            th2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //t1();
        t2();
    }
}

class HandleData {
    public void write(PipedOutputStream outputStream){
        System.out.println("写入 ====");
        try {
            StringBuilder builder = new StringBuilder();
            // 循环写入 100 次 10 个字节，共 1000 个字节。一个汉字是两个字节，一个英文字母是一个字节，一个数字是一个字节
            for(int i=0; i<100; i++){
                builder.append("0123456789");
            }
            // 再写入 26 个字节
            builder.append("abcdefghijklmnopqrstuvwxyz");

            // data 总长度为 1026 个字节。虽然输入流默认的大小最多只能写入1024个字节，但是输出时是不受限制的。
            String data = builder.toString();
            System.out.println(data);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read(PipedInputStream inputStream){
        System.out.println("读取 ====");
        try {
            // 虽然 bytes 的大小可以设定为 2048 个字节，但最多只会从“管道输入流”中读取1024个字节。因为 “管道输入流”的缓冲区
            // 大小默认只有1024个字节。
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);

            // 因为输入流默认的大小最多只能写入1024个字节，所以超过这个数的数据也不会缓存，就读取不到。只能使用 while 循环才
            // 能读取到。
            while (length != -1){
                String data = new String(bytes,0,length);
                System.out.println(data);

                length = inputStream.read(bytes);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeStr(PipedWriter outputStream){
        System.out.println("写入 ====");
        try {
            StringBuilder builder = new StringBuilder();
            for(int i=0; i<3; i++){
                builder.append("我爱你！");
            }
            builder.append("abcdefghijklmnopqrstuvwxyz");

            // data 总长度为 126 个字节。
            String data = builder.toString();
            System.out.println(data);
            outputStream.write(data);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readStr(PipedReader inputStream){
        System.out.println("读取 ====");
        try {
            // 虽然 bytes 的大小可以设定为 2048 个字节，但最多只会从“管道输入流”中读取1024个字节。因为 “管道输入流”的缓冲区
            // 大小默认只有1024个字节。
            char[] bytes = new char[1024];
            int length = inputStream.read(bytes);

            while (length != -1){
                String data = new String(bytes,0,length);
                System.out.println(data);

                length = inputStream.read(bytes);
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}