package xyz.xuminghai.net_lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 2023/12/13 22:03 星期三<br/>
 *
 * @author xuMingHai
 */
public class NetLockTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetLockTest.class);

    public static void main(String[] args) {
        LOGGER.info("尝试获取锁！！！");
        try (NetLockClient lock = new NetLockClient("test")) {
            int lockState = lock.lock();
            LOGGER.info("lockState = {}", lockState);
            try {
                LOGGER.info("获取锁成功，请输入消息：");
                LOGGER.info(String.valueOf(System.in.read()));
            }
            finally {
                int unlockState = lock.unlock();
                LOGGER.info("unlockState = {}", unlockState);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
