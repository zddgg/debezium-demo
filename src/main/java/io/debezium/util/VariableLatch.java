package io.debezium.util;

import io.debezium.annotation.ThreadSafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@ThreadSafe
public class VariableLatch {
    private final Sync sync;

    public static VariableLatch create() {
        return create(0);
    }

    public static VariableLatch create(int initialValue) {
        return new VariableLatch(initialValue);
    }

    public VariableLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        } else {
            this.sync = new Sync(count);
        }
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public void countDown(int count) {
        this.sync.releaseShared(1 * Math.abs(count));
    }

    public void countUp() {
        this.sync.releaseShared(-1);
    }

    public void countUp(int count) {
        this.sync.releaseShared(-1 * Math.abs(count));
    }

    public long getCount() {
        return (long) this.sync.getCount();
    }

    public String toString() {
        String var10000 = super.toString();
        return var10000 + "[Count = " + this.sync.getCount() + "]";
    }

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            this.setState(count);
        }

        int getCount() {
            return this.getState();
        }

        protected int tryAcquireShared(int acquires) {
            return this.getState() == 0 ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            int c;
            int nextc;
            do {
                c = this.getState();
                if (c == 0 && releases >= 0) {
                    return false;
                }

                nextc = c - releases;
                if (nextc < 0) {
                    nextc = 0;
                }
            } while (!this.compareAndSetState(c, nextc));

            return nextc == 0;
        }
    }
}
