package xyz.xuminghai.net_lock;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 2023/12/12 22:25 星期二<br/>
 *
 * @author xuMingHai
 */
public class NetLockInfo {

    private final String topic;

    private SocketChannel owner;

    private int state;

    private Queue<SocketChannel> waitQueue;

    public NetLockInfo(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public SocketChannel getOwner() {
        return owner;
    }

    public void setOwner(SocketChannel owner) {
        this.owner = owner;
        this.state = 0;
    }

    public void stateIncrement() {
        state++;
    }

    public void stateDecrement() {
        state--;
    }

    public int getState() {
        return state;
    }

    public Queue<SocketChannel> getWaitQueue() {
        if (waitQueue == null) {
            waitQueue = new LinkedList<>();
        }
        return waitQueue;
    }
}
