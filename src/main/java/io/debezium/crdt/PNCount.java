package io.debezium.crdt;

public interface PNCount extends GCount {
    default long get() {
        return this.getIncrement() - this.getDecrement();
    }

    long getDecrement();
}
