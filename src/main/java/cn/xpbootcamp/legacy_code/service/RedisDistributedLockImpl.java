package cn.xpbootcamp.legacy_code.service;

public class RedisDistributedLockImpl implements DistributedLock {

    public boolean lock(String transactionId) {
        // Here is connecting to redis server, please do not invoke directly
        throw new RuntimeException("Redis server is connecting......");
    }

    public void unlock(String transactionId) {
        // Here is connecting to redis server, please do not invoke directly
        throw new RuntimeException("Redis server is connecting......");
    }
}
