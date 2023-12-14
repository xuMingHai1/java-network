package xyz.xuminghai.net_lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.xuminghai.channel.MessageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 2023/12/12 22:14 星期二<br/>
 *
 * @author xuMingHai
 */
public class NetLockServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetLockServer.class);

    private static final NetLockManager NET_LOCK_MANAGER = new NetLockManager();

    public static void main(String[] args) {
        MessageHandler messageHandler = new MessageHandler();

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(2333));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            for (; ; ) {
                // 阻塞获取事件
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    // 接受连接事件
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    // 客户端可读
                    else if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        String message = "";
                        try {
                            message = messageHandler.handle(socketChannel);
                            LOGGER.info("message = {}", message);
                        }
                        catch (IOException e) {
                            LOGGER.error("客户端读取异常：", e);
                            selectionKey.cancel();
                            NET_LOCK_MANAGER.removeSocketChannel(socketChannel);
                        }

                        if (!message.isEmpty()) {
                            String[] split = message.split(":");
                            String topic = split[0];
                            String lock = split[1];
                            if ("lock".equals(lock)) {
                                NET_LOCK_MANAGER.acquireLock(socketChannel, topic);
                            }
                            else if ("unlock".equals(lock)) {
                                NET_LOCK_MANAGER.releaseLock(socketChannel, topic);
                            }
                        }


                    }
                    keyIterator.remove();
                }
            }

        }
        catch (IOException e) {
            LOGGER.error("IO异常", e);
        }

    }


}
