package cn.xpbootcamp.legacy_code.service;

import java.util.UUID;

public class IdGeneratorImpl implements IdGenerator {

    @Override
    public String newId() {
        return UUID.randomUUID().toString();
    }
}