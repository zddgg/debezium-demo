package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
class StateBasedPNCounter implements PNCounter {
    private long adds;
    private long removes;

    protected StateBasedPNCounter() {
        this(0L, 0L);
    }

    protected StateBasedPNCounter(long adds, long removes) {
        this.adds = adds;
        this.removes = removes;
    }

    public PNCounter increment() {
        ++this.adds;
        return this;
    }

    public PNCounter decrement() {
        ++this.removes;
        return this;
    }

    public long incrementAndGet() {
        return ++this.adds - this.removes;
    }

    public long decrementAndGet() {
        return this.adds - ++this.removes;
    }

    public long getAndIncrement() {
        return this.adds++ - this.removes;
    }

    public long getAndDecrement() {
        return this.adds - this.removes++;
    }

    public long get() {
        return this.adds - this.removes;
    }

    public long getIncrement() {
        return this.adds;
    }

    public long getDecrement() {
        return this.removes;
    }

    public PNCounter merge(Count other) {
        if (other instanceof PNCount) {
            PNCount changes = (PNCount) other;
            this.adds += changes.getIncrement();
            this.removes += changes.getDecrement();
        } else if (other instanceof GCount) {
            GCount changes = (GCount) other;
            this.adds += changes.getIncrement();
        } else if (other instanceof Count) {
            this.adds += other.get();
        }

        return this;
    }

    public String toString() {
        return "+" + this.adds + " -" + this.removes;
    }
}
