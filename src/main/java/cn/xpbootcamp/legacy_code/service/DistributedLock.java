package cn.xpbootcamp.legacy_code.service;

public interface DistributedLock {
    
    boolean lock(String transactionId);

    void unlock(String transactionId);
}
