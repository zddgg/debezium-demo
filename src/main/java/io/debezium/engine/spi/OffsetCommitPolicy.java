package io.debezium.engine.spi;

import java.time.Duration;
import java.util.Properties;

@FunctionalInterface
public interface OffsetCommitPolicy {
    static OffsetCommitPolicy always() {
        return new AlwaysCommitOffsetPolicy();
    }

    static OffsetCommitPolicy periodic(Properties config) {
        return new PeriodicCommitOffsetPolicy(config);
    }

    boolean performCommit(long var1, Duration var3);

    default OffsetCommitPolicy or(OffsetCommitPolicy other) {
        return other == null ? this : (number, time) -> {
            return this.performCommit(number, time) || other.performCommit(number, time);
        };
    }

    default OffsetCommitPolicy and(OffsetCommitPolicy other) {
        return other == null ? this : (number, time) -> {
            return this.performCommit(number, time) && other.performCommit(number, time);
        };
    }

    public static class AlwaysCommitOffsetPolicy implements OffsetCommitPolicy {
        public boolean performCommit(long numberOfMessagesSinceLastCommit, Duration timeSinceLastCommit) {
            return true;
        }
    }

    public static class PeriodicCommitOffsetPolicy implements OffsetCommitPolicy {
        private final Duration minimumTime;

        public PeriodicCommitOffsetPolicy(Properties config) {
            this.minimumTime = Duration.ofMillis(Long.valueOf(config.getProperty("offset.flush.interval.ms")));
        }

        public boolean performCommit(long numberOfMessagesSinceLastCommit, Duration timeSinceLastCommit) {
            return timeSinceLastCommit.compareTo(this.minimumTime) >= 0;
        }
    }
}
