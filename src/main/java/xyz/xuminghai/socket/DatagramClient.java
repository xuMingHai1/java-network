package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 2023/11/24 0:58 星期五<br/>
 * 使用UDP协议的本机广播客户端
 *
 * @author xuMingHai
 */
public class DatagramClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramClient.class);

    /**
     * 数据包大小
     */
    private static final int PACKET_SIZE = 1024;


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
            // 发送空消息连接
            datagramSocket.send(new DatagramPacket(new byte[0], 0));
            LOGGER.info("客户端向服务器注册成功！！！");
            // 一个线程等待控制台输入消息
            new Thread(() -> {
                for (; ; ) {
                    LOGGER.info("请输入消息：");
                    byte[] message = scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8);
                    try {
                        // 拆分消息数组，符合数据包大小
                        int length = message.length;
                        // 输入消息大于数据包大小
                        if (length > PACKET_SIZE) {
                            // 偏移量和长度索引位置
                            int offset = 0, index = PACKET_SIZE;
                            for (; index <= length; index += PACKET_SIZE) {
                                datagramSocket.send(new DatagramPacket(message, offset, PACKET_SIZE));
                                // 更新偏移量
                                offset = index;
                            }
                            // 剩余的字节
                            if (length > offset) {
                                datagramSocket.send(new DatagramPacket(message, offset, (length - offset)));
                            }
                        }
                        else {
                            datagramSocket.send(new DatagramPacket(message, length));
                        }
                    }
                    catch (IOException e) {
                        LOGGER.error("IO 错误", e);
                    }
                }
            }).start();

            // 接收UDP广播消息
            // 面向数据包的形式
            byte[] buffer = new byte[PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            for (; ; ) {
                byte[] messageBuffer = new byte[0];
                String message;
                do {
                    datagramSocket.receive(datagramPacket);
                    byte[] data = datagramPacket.getData();
                    int offset = datagramPacket.getOffset();
                    int length = datagramPacket.getLength();
                    int oldLength = messageBuffer.length;
                    messageBuffer = Arrays.copyOf(messageBuffer, oldLength + length);
                    System.arraycopy(data, offset, messageBuffer, oldLength, length);
                    message = new String(messageBuffer, StandardCharsets.UTF_8);
                } while (message.charAt(message.length() - 1) != '\n');
                LOGGER.info("广播接收到消息：{}", message.substring(0, message.length() - 1));
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
