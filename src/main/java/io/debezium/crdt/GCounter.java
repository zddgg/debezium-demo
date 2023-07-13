package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
public interface GCounter extends GCount {
    GCounter increment();

    long incrementAndGet();

    long getAndIncrement();

    GCounter merge(Count var1);
}
