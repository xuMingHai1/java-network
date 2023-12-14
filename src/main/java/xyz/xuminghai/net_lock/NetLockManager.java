package xyz.xuminghai.net_lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * 2023/12/13 20:57 星期三<br/>
 *
 * @author xuMingHai
 */
public class NetLockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetLockManager.class);

    private final Map<String, NetLockInfo> map = new HashMap<>();

    public void acquireLock(SocketChannel socketChannel, String topic) {
        NetLockInfo netLockInfo = map.computeIfAbsent(topic, NetLockInfo::new);
        // 没有所有者
        if (netLockInfo.getOwner() == null || netLockInfo.getOwner() == socketChannel) {
            netLockInfo.setOwner(socketChannel);
            netLockInfo.stateIncrement();
            sendMessage(socketChannel, netLockInfo);
        }
        else {
            // 锁被占有，进入等待队列
            netLockInfo.getWaitQueue().add(socketChannel);
        }
    }

    public void releaseLock(SocketChannel socketChannel, String topic) {
        NetLockInfo netLockInfo = map.get(topic);
        if (netLockInfo != null && netLockInfo.getOwner() != null
                && netLockInfo.getOwner() == socketChannel) {
            netLockInfo.stateDecrement();
            sendMessage(socketChannel, netLockInfo);
            if (netLockInfo.getState() == 0) {
                SocketChannel channel = netLockInfo.getWaitQueue().poll();
                if (channel != null) {
                    netLockInfo.setOwner(channel);
                    netLockInfo.stateIncrement();
                    sendMessage(channel, netLockInfo);
                }
            }
        }
    }

    public void removeSocketChannel(SocketChannel socketChannel) {
        List<String> list = new LinkedList<>();
        map.forEach((topic, netLockInfo) -> {
            if (netLockInfo.getOwner() == socketChannel) {
                list.add(topic);
            }
            else {
                netLockInfo.getWaitQueue().remove(socketChannel);
            }
        });
        list.forEach(topic -> {
            NetLockInfo netLockInfo = map.get(topic);
            Queue<SocketChannel> waitQueue = netLockInfo.getWaitQueue();
            if (waitQueue.isEmpty()) {
                map.remove(topic);
            }
            else {
                SocketChannel channel = waitQueue.poll();
                netLockInfo.setOwner(channel);
                netLockInfo.stateIncrement();
                sendMessage(channel, netLockInfo);
            }
        });
    }

    private void sendMessage(SocketChannel socketChannel, NetLockInfo netLockInfo) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4).putInt(netLockInfo.getState());
        byteBuffer.flip();
        try {
            socketChannel.write(byteBuffer);
        }
        catch (IOException e) {
            try {
                socketChannel.close();
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            LOGGER.error("写入数据错误：", e);
            Queue<SocketChannel> waitQueue = netLockInfo.getWaitQueue();
            SocketChannel channel = waitQueue.poll();
            if (channel == null) {
                map.remove(netLockInfo.getTopic());
            }
            else {
                netLockInfo.setOwner(channel);
                netLockInfo.stateIncrement();
                sendMessage(channel, netLockInfo);
            }
        }
    }

}
