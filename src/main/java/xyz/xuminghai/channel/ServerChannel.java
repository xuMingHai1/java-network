package xyz.xuminghai.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 2023/11/27 15:03 星期一<br/>
 * Selector 用于监视一个或多个 NIO 通道并识别一个或多个通道何时可用于数据传输。
 * 单个线程管理多个通道，多路复用。
 *
 * @author xuMingHai
 */
public class ServerChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerChannel.class);


    public static void main(String[] args) {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            // 设置服务器地址
            serverSocketChannel.bind(new InetSocketAddress(8888));
            // 设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 服务器socket注册客户端连接
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            for (; ; ) {
                // 阻塞直到注册事件发生
                selector.select();
                // 注册事件发生的channel
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    // 新的连接
                    if (selectionKey.isAcceptable()) {
                        LOGGER.info("建立客户端连接！！！");
                        try {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            // 设置非阻塞模式
                            socketChannel.configureBlocking(false);
                            // 创建直接缓冲区，绑定客户端socket
                            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                            // 客户端socket注册读兴趣
                            socketChannel.register(selector, SelectionKey.OP_READ, byteBuffer);
                        }
                        catch (IOException e) {
                            LOGGER.error("客户端连接错误", e);
                        }
                    }
                    // 读取
                    else if (selectionKey.isReadable()) {
                        LOGGER.info("读取客户端消息！！！");
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        // 获取客户端socket绑定的直接缓冲区
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        try {
                            int read = socketChannel.read(byteBuffer);
                            LOGGER.info("byteBuffer = {}", byteBuffer);
                            if (read == -1) {
                                LOGGER.info("客户端关闭连接！！！");
                                selectionKey.cancel();
                                socketChannel.close();
                            }
                            else {
                                // 反转缓冲区读取
                                byteBuffer.flip();
                                socketChannel.write(byteBuffer);
                            }
                            // 清空缓冲区
                            byteBuffer.clear();
                        }
                        catch (IOException e) {
                            LOGGER.error("客户端读取错误", e);
                            // 取消注册
                            selectionKey.cancel();
                            // 关闭连接
                            socketChannel.close();
                        }
                    }
                    // 删除已处理的selectionKey
                    keyIterator.remove();
                    LOGGER.info("删除事件key");
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("服务器打开或选择器打开错误", e);
        }
    }


}
