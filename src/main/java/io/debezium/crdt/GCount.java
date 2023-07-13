package io.debezium.crdt;

public interface GCount extends Count {
    long getIncrement();
}
