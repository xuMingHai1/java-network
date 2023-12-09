package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.locks.LockSupport;

/**
 * 2023/12/8 0:22 星期五<br/>
 * 异步通道服务器
 *
 * @author xuMingHai
 */
public class AsynchronousServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousServer.class);


    public static void main(String[] args) {

        try (AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open()) {
            // 绑定服务端口
            server.bind(new InetSocketAddress(8888));

            server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel channel, Object attachment) {
                    try {
                        LOGGER.info("[{}]客户端连接成功！！！", channel.getRemoteAddress());
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // 回调方法
                    server.accept(null, this);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    channel.read(byteBuffer, null, new CompletionHandler<Integer, Object>() {

                        @Override
                        public void completed(Integer result, Object attachment) {
                            if (result == -1) {
                                try {
                                    LOGGER.info("[{}]客户端断开连接！！！", channel.getRemoteAddress());
                                    channel.close();
                                }
                                catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return;
                            }
                            final CompletionHandler<Integer, Object> readHandler = this;
                            byteBuffer.flip();

                            channel.write(byteBuffer, null, new CompletionHandler<Integer, Object>() {
                                @Override
                                public void completed(Integer result, Object attachment) {
                                    byteBuffer.clear();
                                    // 继续读取
                                    channel.read(byteBuffer, null, readHandler);
                                }

                                @Override
                                public void failed(Throwable exc, Object attachment) {
                                    LOGGER.error("写入失败：", exc);
                                }
                            });
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            LOGGER.error("读取失败：", exc);
                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    LOGGER.error("连接失败：", exc);
                }
            });
            LOGGER.info("异步服务器启动成功！！！");
            LockSupport.park();
        }
        catch (IOException e) {
            LOGGER.error("IO错误：", e);
        }

    }

}
