package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 2023/11/27 23:48 星期一<br/>
 *
 * @author xuMingHai
 */
public class ClientChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientChannel.class);

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(8888));
             Scanner scanner = new Scanner(System.in)) {
            if (socketChannel.isConnected()) {
                LOGGER.info("连接服务器成功！！！");
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            for (; ; ) {
                LOGGER.info("请输入消息：");
                // 发送消息给服务器
                socketChannel.write(ByteBuffer.wrap(scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8)));
                byte[] messageBuffer = new byte[0];
                String message;
                do {
                    int length = socketChannel.read(byteBuffer);
                    int oldLength = messageBuffer.length;
                    messageBuffer = Arrays.copyOf(messageBuffer, oldLength + length);
                    System.arraycopy(byteBuffer.array(), 0, messageBuffer, oldLength, length);
                    byteBuffer.clear();
                    message = new String(messageBuffer, StandardCharsets.UTF_8);
                } while (message.charAt(message.length() - 1) != '\n');
                message = message.substring(0, message.length() - 1);
                LOGGER.info("服务器：{}", message);
                // 关闭连接命令
                if ("close".equals(message)) {
                    LOGGER.info("关闭连接！！！");
                    break;
                }
            }
        }
        catch (IOException e) {
            LOGGER.info("服务器连接错误", e);
        }
    }

}
