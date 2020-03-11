package cn.xpbootcamp.legacy_code.service;

public class SystemClockerImpl implements SystemClocker {

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}