package xyz.xuminghai.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 2023/11/23 13:41 星期四<br/>
 * 回声服务器
 *
 * @author xuMingHai
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        // 创建服务套接字
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            LOGGER.info("服务器已启动。。。");
            for (; ; ) {
                Socket socket = serverSocket.accept();
                LOGGER.info("客户端连接成功！！！");
                new Thread(() -> {
                    // 缓冲区
                    byte[] buffer = new byte[1024];
                    // 读取位置
                    int bytesRead;
                    try {
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            // 发送回客户端
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        socket.close();
                        LOGGER.info("客户端关闭连接！！！");
                    }
                    catch (IOException e) {
                        LOGGER.error("客户端错误", e);
                    }
                }).start();
            }
        }
        catch (IOException e) {
            LOGGER.error("服务器错误", e);
        }
    }

}
