package com.vdamo.ordering.common.support;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    private static final int SEQUENCE_LIMIT = 1000;

    private final AtomicInteger sequence = new AtomicInteger();

    public synchronized long nextId() {
        long timestamp = Instant.now().toEpochMilli() * SEQUENCE_LIMIT;
        int next = sequence.updateAndGet(current -> (current + 1) % SEQUENCE_LIMIT);
        return timestamp + next;
    }
}
