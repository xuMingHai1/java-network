package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 2023/11/23 14:44 星期四<br/>
 *
 * @author xuMingHai
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        try (Socket socket = new Socket("::1", 8888);
             Scanner scanner = new Scanner(System.in)) {
            if (socket.isConnected()) {
                LOGGER.info("连接服务器成功！！！");
            }
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            // 缓冲区
            byte[] buffer = new byte[1024];
            StringBuilder sb;
            // 读取位置
            int bytesRead;
            for (; ; ) {
                // 清空消息缓冲
                sb = new StringBuilder();
                LOGGER.info("请输入消息：");
                // 控制台读取一行，发送到服务器
                outputStream.write(scanner.nextLine().getBytes(StandardCharsets.UTF_8));
                // 写入分隔符
                outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                // 接收服务器响应
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    String s;
                    int lastIndex = bytesRead - 1;
                    if (buffer[lastIndex] == '\n') {
                        s = new String(buffer, 0, lastIndex, StandardCharsets.UTF_8);
                        sb.append(s);
                        break;
                    }
                    s = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    sb.append(s);
                }
                LOGGER.info("服务器：{}", sb);
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
