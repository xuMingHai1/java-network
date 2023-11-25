package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 2023/11/25 1:27 星期六<br/>
 *
 * @author xuMingHai
 */
public class DatagramServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramServer.class);

    private static final ConcurrentHashMap<SocketAddress, Object> CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    private static final Object VALUE = new Object();

    private static final ArrayBlockingQueue<byte[]> ARRAY_BLOCKING_QUEUE = new ArrayBlockingQueue<>(100);


    public static void main(String[] args) {
        try (DatagramSocket datagramSocket = new DatagramSocket(8888, InetAddress.getLoopbackAddress())) {
            // 广播给客户端消息
            new Thread(() -> {
                try {
                    for (; ; ) {
                        // 从队列中获取元素
                        byte[] data = ARRAY_BLOCKING_QUEUE.take();
                        CONCURRENT_HASH_MAP.forEachKey(Long.MAX_VALUE, socketAddress -> {
                            try {
                                datagramSocket.send(new DatagramPacket(data, data.length, socketAddress));
                            }
                            catch (IOException e) {
                                // 发生异常时，删除客户端
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            // 接收客户端消息
            byte[] buffer = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            for (; ; ) {
                datagramSocket.receive(datagramPacket);
                // 添加客户端
                CONCURRENT_HASH_MAP.put(datagramPacket.getSocketAddress(), VALUE);
                // 发送消息到队列
                byte[] data = new byte[datagramPacket.getLength()];
                System.arraycopy(datagramPacket.getData(), 0, data, 0, datagramPacket.getLength());
                ARRAY_BLOCKING_QUEUE.put(data);
            }

        }
        catch (SocketException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            LOGGER.error("IO 异常", e);
        }
        catch (InterruptedException e) {
            LOGGER.error("添加中断", e);
        }
    }

}
