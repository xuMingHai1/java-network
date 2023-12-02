package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 2023/11/25 1:27 星期六<br/>
 *
 * @author xuMingHai
 */
public class DatagramServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramServer.class);

    /**
     * 数据包大小
     */
    private static final int PACKET_SIZE = 1024;

    private static final Set<SocketAddress> SET = new HashSet<>();

    public static void main(String[] args) {
        try (DatagramSocket datagramSocket = new DatagramSocket(8888, InetAddress.getLoopbackAddress())) {
            // 客户端消息缓冲
            byte[] buffer = new byte[PACKET_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            for (; ; ) {
                datagramSocket.receive(datagramPacket);
                // 添加客户端
                if (SET.add(datagramPacket.getSocketAddress())) {
                    LOGGER.info("客户端注册成功！！！");
                }
                int length = datagramPacket.getLength();
                // 不是连接消息，进行广播
                if (length > 0) {
                    // 获取数据内容
                    byte[] data = new byte[datagramPacket.getLength()];
                    System.arraycopy(datagramPacket.getData(), 0, data, 0, datagramPacket.getLength());
                    // 广播消息
                    SET.forEach(socketAddress -> {
                        try {
                            datagramSocket.send(new DatagramPacket(data, data.length, socketAddress));
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

            }

        }
        catch (SocketException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            LOGGER.error("IO 异常", e);
        }
    }

}
