package io.debezium.crdt;

import io.debezium.annotation.NotThreadSafe;

@NotThreadSafe
class StateBasedPNDeltaCounter extends StateBasedPNCounter implements DeltaCounter {
    private PNCounter delta;

    protected StateBasedPNDeltaCounter() {
        this(0L, 0L, 0L, 0L);
    }

    protected StateBasedPNDeltaCounter(long totalAdds, long totalRemoves, long recentAdds, long recentRemoves) {
        super(totalAdds, totalRemoves);
        this.delta = new StateBasedPNCounter(recentAdds, recentRemoves);
    }

    public DeltaCounter increment() {
        super.increment();
        this.delta.increment();
        return this;
    }

    public DeltaCounter decrement() {
        super.decrement();
        this.delta.decrement();
        return this;
    }

    public long incrementAndGet() {
        this.delta.incrementAndGet();
        return super.incrementAndGet();
    }

    public long decrementAndGet() {
        this.delta.decrementAndGet();
        return super.decrementAndGet();
    }

    public long getAndIncrement() {
        this.delta.getAndIncrement();
        return super.getAndIncrement();
    }

    public long getAndDecrement() {
        this.delta.getAndDecrement();
        return super.getAndDecrement();
    }

    public PNCount getChanges() {
        return this.delta;
    }

    public boolean hasChanges() {
        return this.delta.getIncrement() != 0L || this.delta.getDecrement() != 0L;
    }

    public Count getPriorCount() {
        final long value = super.get() - this.delta.get();
        return new Count() {
            public long get() {
                return value;
            }
        };
    }

    public void reset() {
        this.delta = new StateBasedPNCounter();
    }

    public DeltaCounter merge(Count other) {
        if (other instanceof DeltaCount) {
            DeltaCount that = (DeltaCount) other;
            this.delta.merge(that.getChanges());
            super.merge(that.getChanges());
        } else {
            super.merge(other);
        }

        return this;
    }

    public String toString() {
        String var10000 = super.toString();
        return var10000 + " (changes " + this.delta + ")";
    }
}
