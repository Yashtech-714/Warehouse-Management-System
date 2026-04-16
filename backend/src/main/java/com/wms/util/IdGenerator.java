package com.wms.util;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
