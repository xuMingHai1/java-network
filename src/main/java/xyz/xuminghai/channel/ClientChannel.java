package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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
                byteBuffer.put(scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8));
                byteBuffer.flip();
                // 发送给服务器
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                StringBuilder sb = new StringBuilder();
                // 读取位置
                int length;
                // 接收服务器响应
                while ((length = socketChannel.read(byteBuffer)) != -1) {
                    sb.append(new String(byteBuffer.array(), 0, length, StandardCharsets.UTF_8));
                    byteBuffer.clear();
                    // 解析消息
                    if ('\n' == sb.charAt(sb.length() - 1)) {
                        break;
                    }
                }
                String message = sb.substring(0, sb.length() - 1);
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
