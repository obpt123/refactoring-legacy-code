package cn.xpbootcamp.legacy_code.service;

public interface DistributedLock {

    boolean lock(String lockKey);

    void unlock(String lockKey);

    default boolean runWithLock(String lockKey, Action action) {
        boolean locked = lock(lockKey);
        try {
            if (locked && action != null) {
                action.run();
            }
            return locked;
        } finally {
            if (locked) {
                unlock(lockKey);
            }
        }
    }

}
