package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 2023/12/1 21:02 星期五<br/>
 * UDP广播发射器
 *
 * @author xuMingHai
 */
public class DatagramChannelBroadcastLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramChannelBroadcastLauncher.class);

    /**
     * 数据包大小
     */
    private static final int PACKET_SIZE = 1024;

    public static void main(String[] args) {

        try (DatagramChannel datagramChannel = DatagramChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            // 局域网广播地址
            datagramChannel.connect(new InetSocketAddress("255.255.255.255", 8888));
            // 开启广播传输
            datagramChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
            LOGGER.info("SO_BROADCAST = {}", datagramChannel.getOption(StandardSocketOptions.SO_BROADCAST));
            for (; ; ) {
                LOGGER.info("请输入广播消息：");
                // 控制台输入写入
                String message = scanner.nextLine();
                byte[] messageBytes = message.concat("\n").getBytes(StandardCharsets.UTF_8);
                // 根据指定数据包大小拆分发送
                int length = messageBytes.length;
                if (length > PACKET_SIZE) {
                    // 偏移量和长度索引位置
                    int offset = 0, index = PACKET_SIZE;
                    for (; index <= length; index += PACKET_SIZE) {
                        datagramChannel.write(ByteBuffer.wrap(messageBytes, offset, PACKET_SIZE));
                        // 更新偏移量
                        offset = index;
                    }
                    // 剩余的字节
                    if (length > offset) {
                        datagramChannel.write(ByteBuffer.wrap(messageBytes, offset, (length - offset)));
                    }
                }
                // 小于或等于指定指定数据包大小
                else {
                    datagramChannel.write(ByteBuffer.wrap(messageBytes));
                }
                // 关闭广播
                if ("close".equals(message)) {
                    break;
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("IO错误：", e);
        }

    }

}
