package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
public interface PNCounter extends PNCount, GCounter {
    PNCounter increment();

    PNCounter decrement();

    long decrementAndGet();

    long getAndDecrement();

    PNCounter merge(Count var1);
}
