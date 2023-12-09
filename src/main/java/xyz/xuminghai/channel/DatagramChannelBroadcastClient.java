package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;

/**
 * 2023/12/1 21:03 星期五<br/>
 * UDP广播客户端
 *
 * @author xuMingHai
 */
public class DatagramChannelBroadcastClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatagramChannelBroadcastClient.class);

    /**
     * 数据包大小
     */
    private static final int PACKET_SIZE = 1024;

    public static void main(String[] args) {
        try (DatagramChannel datagramChannel = DatagramChannel.open()) {
            // 连接广播地址（UDP是无连接的，避免安全检查）
            datagramChannel.connect(new InetSocketAddress("255.255.255.255", 8888));
            // 开启广播传输
            datagramChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
            MessageHandler messageHandler = new MessageHandler(PACKET_SIZE);
            for (; ; ) {
                String message = messageHandler.handle(datagramChannel);
                LOGGER.info("广播消息：{}", message);
                // 关闭连接命令
                if ("close".equals(message)) {
                    LOGGER.info("关闭连接！！！");
                    break;
                }
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
