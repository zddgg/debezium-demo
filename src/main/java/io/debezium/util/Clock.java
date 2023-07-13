package io.debezium.util;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public interface Clock {
    Clock SYSTEM = new Clock() {
        public long currentTimeInMillis() {
            return System.currentTimeMillis();
        }

        public long currentTimeInNanos() {
            return System.nanoTime();
        }

        public Instant currentTimeAsInstant() {
            return Instant.now();
        }
    };

    static Clock system() {
        return SYSTEM;
    }

    default Instant currentTime() {
        return Instant.ofEpochMilli(this.currentTimeInMillis());
    }

    default long currentTimeInNanos() {
        return this.currentTimeInMillis() * 1000000L;
    }

    default long currentTimeInMicros() {
        return TimeUnit.MICROSECONDS.convert(this.currentTimeInMillis(), TimeUnit.MILLISECONDS);
    }

    default Instant currentTimeAsInstant() {
        return Instant.ofEpochMilli(this.currentTimeInMillis());
    }

    long currentTimeInMillis();
}
