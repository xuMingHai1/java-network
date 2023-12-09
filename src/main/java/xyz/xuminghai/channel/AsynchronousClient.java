package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

/**
 * 2023/12/9 15:54 星期六<br/>
 * 异步客户端
 *
 * @author xuMingHai
 */
public class AsynchronousClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousClient.class);


    public static void main(String[] args) {
        Thread mainThread = Thread.currentThread();
        try (AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            channel.connect(new InetSocketAddress("localhost", 8888), null, new CompletionHandler<Void, Object>() {
                @Override
                public void completed(Void result, Object attachment) {
                    try {
                        LOGGER.info("[{}]服务器连接成功!!!", channel.getRemoteAddress());
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    channel.write(ByteBuffer.wrap(scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8)), null, new CompletionHandler<Integer, Object>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            CompletionHandler<Integer, Object> writeCompletionHandler = this;
                            channel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {
                                byte[] messageBuffer = new byte[0];
                                String message;

                                @Override
                                public void completed(Integer result, Object attachment) {
                                    // 解析消息
                                    int length = result;
                                    int oldLength = messageBuffer.length;
                                    messageBuffer = Arrays.copyOf(messageBuffer, oldLength + length);
                                    System.arraycopy(byteBuffer.array(), 0, messageBuffer, oldLength, length);
                                    byteBuffer.clear();
                                    message = new String(messageBuffer, StandardCharsets.UTF_8);
                                    if (message.charAt(message.length() - 1) != '\n') {
                                        channel.read(byteBuffer, null, this);
                                    }
                                    else {
                                        message = message.substring(0, message.length() - 1);
                                        LOGGER.info("服务器：{}", message);
                                        if ("close".equals(message)) {
                                            LOGGER.info("客户端关闭连接！！！");
                                            LockSupport.unpark(mainThread);
                                        }
                                        else {
                                            channel.write(ByteBuffer.wrap(scanner.nextLine().concat("\n").getBytes(StandardCharsets.UTF_8)), null, writeCompletionHandler);
                                        }
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, Object attachment) {
                                    LOGGER.error("读取失败：", exc);
                                }
                            });
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            LOGGER.error("写入失败：", exc);
                        }
                    });

                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    LOGGER.error("连接失败：", exc);
                }
            });
            LockSupport.park();
        }
        catch (IOException e) {
            LOGGER.error("通道打开错误");
        }

    }

}
