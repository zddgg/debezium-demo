package io.debezium.embedded.spi;

import io.debezium.config.Configuration;
import io.debezium.embedded.EmbeddedEngine;

import java.time.Duration;
import java.util.Properties;

/**
 * @deprecated
 */
@Deprecated
@FunctionalInterface
public interface OffsetCommitPolicy extends io.debezium.engine.spi.OffsetCommitPolicy {
    static OffsetCommitPolicy always() {
        return new AlwaysCommitOffsetPolicy();
    }

    static OffsetCommitPolicy periodic(Configuration config) {
        return new PeriodicCommitOffsetPolicy(config);
    }

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

        public PeriodicCommitOffsetPolicy(Configuration config) {
            this.minimumTime = Duration.ofMillis(config.getLong(EmbeddedEngine.OFFSET_FLUSH_INTERVAL_MS));
        }

        public PeriodicCommitOffsetPolicy(Properties properties) {
            this(Configuration.from(properties));
        }

        public boolean performCommit(long numberOfMessagesSinceLastCommit, Duration timeSinceLastCommit) {
            return timeSinceLastCommit.compareTo(this.minimumTime) >= 0;
        }
    }
}
