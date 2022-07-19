package com.llp.rpc.common.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于生成id
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
