package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
public interface DeltaCounter extends PNCounter, DeltaCount {
    DeltaCounter increment();

    DeltaCounter decrement();

    DeltaCounter merge(Count var1);

    void reset();
}
