package xyz.xuminghai.net_lock;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 2023/12/13 14:43 星期三<br/>
 * 网络锁客户端
 *
 * @author xuMingHai
 */
public class NetLockClient implements AutoCloseable {

    private final String topic;

    private final SocketChannel socketChannel;

    public NetLockClient(String topic) throws IOException {
        this.topic = topic;
        socketChannel = SocketChannel.open(new InetSocketAddress(2333));
    }

    public int lock() throws IOException {
        return sendMessage("lock");
    }

    public int unlock() throws IOException {
        return sendMessage("unlock");
    }

    private int sendMessage(String message) throws IOException {
        StringBuilder stringBuilder = new StringBuilder(topic.length() + message.length() + 2);
        stringBuilder.append(topic).append(':')
                .append(message).append('\n');
        socketChannel.write(ByteBuffer.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        socketChannel.read(byteBuffer);
        byteBuffer.flip();
        return byteBuffer.asIntBuffer().get();
    }

    @Override
    public void close() throws IOException {
        socketChannel.close();
    }
}
