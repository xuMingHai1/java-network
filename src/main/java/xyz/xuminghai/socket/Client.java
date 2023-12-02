package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/**
 * 2023/11/23 14:44 星期四<br/>
 *
 * @author xuMingHai
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8888);
             Scanner scanner = new Scanner(System.in)) {

            if (socket.isConnected()) {
                LOGGER.info("连接服务器成功！！！");
            }

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            // 数据缓冲区
            byte[] buffer = new byte[1024];

            for (; ; ) {
                LOGGER.info("请输入消息：");
                // 控制台读取一行，写入分隔符，发送到服务器
                outputStream.write(scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8));
                byte[] messageBuffer = new byte[0];
                String message;
                do {
                    int length = inputStream.read(buffer);
                    int oldLength = messageBuffer.length;
                    messageBuffer = Arrays.copyOf(messageBuffer, oldLength + length);
                    System.arraycopy(buffer, 0, messageBuffer, oldLength, length);
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
