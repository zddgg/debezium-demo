package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
class StateBasedGCounter implements GCounter {
    private long adds;

    protected StateBasedGCounter() {
        this(0L);
    }

    protected StateBasedGCounter(long adds) {
        this.adds = adds;
    }

    public GCounter increment() {
        ++this.adds;
        return this;
    }

    public long incrementAndGet() {
        return ++this.adds;
    }

    public long getAndIncrement() {
        return (long) (this.adds++);
    }

    public long get() {
        return this.adds;
    }

    public long getIncrement() {
        return this.adds;
    }

    public GCounter merge(Count other) {
        if (other instanceof GCount) {
            GCount changes = (GCount) other;
            this.adds += changes.getIncrement();
        } else if (other instanceof Count) {
            this.adds += other.get();
        }

        return this;
    }

    public String toString() {
        return "+" + this.adds;
    }
}
