package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 2023/11/24 0:58 星期五<br/>
 *
 * @author xuMingHai
 */
public class DatagramClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramClient.class);


    public static void main(String[] args) {
        // 服务器地址
        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 8888);

        try (DatagramSocket datagramSocket = new DatagramSocket();
             Scanner scanner = new Scanner(System.in)) {
            // 连接服务器
            datagramSocket.connect(inetSocketAddress);
            if (datagramSocket.isConnected()) {
                LOGGER.info("已经设置连接");
            }
            // 一个线程等待控制台输入消息
            new Thread(() -> {
                for (; ; ) {
                    LOGGER.info("请输入消息：");
                    byte[] message = scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8);
                    try {
                        datagramSocket.send(new DatagramPacket(message, message.length));
                    }
                    catch (IOException e) {
                        LOGGER.error("IO 错误", e);
                    }
                }
            }).start();

            // 接收UDP广播消息
            // 面向数据包的形式
            byte[] buffer = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            for (; ; ) {
                StringBuilder sb = new StringBuilder();
                byte[] data;
                int length;
                do {
                    datagramSocket.receive(datagramPacket);
                    data = datagramPacket.getData();
                    length = datagramPacket.getLength();
                    sb.append(new String(data, 0, length, StandardCharsets.UTF_8));
                } while (sb.charAt(sb.length() - 1) != '\n');
                LOGGER.info("广播接收到消息：{}", sb.substring(0, sb.length() - 1));
            }
        }
        catch (SocketException e) {
            LOGGER.error("Socket 错误", e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
