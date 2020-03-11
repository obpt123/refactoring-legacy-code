package cn.xpbootcamp.legacy_code.service;

import java.util.UUID;

public class IdGeneratorImpl implements IdGenerator {

    @Override
    public String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}