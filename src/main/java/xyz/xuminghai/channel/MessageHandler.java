package xyz.xuminghai.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 2023/12/6 15:22 星期三<br/>
 * 消息处理器
 *
 * @author xuMingHai
 */
public class MessageHandler {

    private final ByteBuffer byteBuffer;

    public MessageHandler(int capacity) {
        byteBuffer = ByteBuffer.allocate(capacity);
    }

    public MessageHandler() {
        this(1024);
    }

    public String handle(ByteChannel byteChannel) throws IOException {
        byte[] messageBuffer = new byte[0];
        String message;
        do {
            int length = byteChannel.read(byteBuffer);
            int oldLength = messageBuffer.length;
            messageBuffer = Arrays.copyOf(messageBuffer, oldLength + length);
            System.arraycopy(byteBuffer.array(), 0, messageBuffer, oldLength, length);
            byteBuffer.clear();
            message = new String(messageBuffer, StandardCharsets.UTF_8);
        } while (message.charAt(message.length() - 1) != '\n');
        return message.substring(0, message.length() - 1);
    }

}
