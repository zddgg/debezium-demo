package io.debezium.relational.history;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.metrics.Metrics;
import io.debezium.util.Clock;
import io.debezium.util.ElapsedTimeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class SchemaHistoryMetrics extends Metrics implements SchemaHistoryListener, SchemaHistoryMXBean {
    private static final String CONTEXT_NAME = "schema-history";
    private static final Duration PAUSE_BETWEEN_LOG_MESSAGES = Duration.ofSeconds(2L);
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaHistoryMetrics.class);
    private SchemaHistoryStatus status;
    private Instant recoveryStartTime;
    private AtomicLong changesRecovered;
    private AtomicLong totalChangesApplied;
    private Instant lastChangeAppliedTimestamp;
    private Instant lastChangeRecoveredTimestamp;
    private HistoryRecord lastAppliedChange;
    private HistoryRecord lastRecoveredChange;
    private final Clock clock;
    private final ElapsedTimeStrategy lastChangeAppliedLogDelay;
    private final ElapsedTimeStrategy lastChangeRecoveredLogDelay;

    public SchemaHistoryMetrics(CommonConnectorConfig connectorConfig, boolean multiPartitionMode) {
        super(connectorConfig, "schema-history", multiPartitionMode);
        this.status = SchemaHistoryStatus.STOPPED;
        this.recoveryStartTime = null;
        this.changesRecovered = new AtomicLong();
        this.totalChangesApplied = new AtomicLong();
        this.clock = Clock.system();
        this.lastChangeAppliedLogDelay = ElapsedTimeStrategy.constant(this.clock, PAUSE_BETWEEN_LOG_MESSAGES);
        this.lastChangeRecoveredLogDelay = ElapsedTimeStrategy.constant(this.clock, PAUSE_BETWEEN_LOG_MESSAGES);
        this.lastChangeAppliedLogDelay.hasElapsed();
        this.lastChangeRecoveredLogDelay.hasElapsed();
    }

    public String getStatus() {
        return this.status.toString();
    }

    public long getRecoveryStartTime() {
        return this.recoveryStartTime == null ? -1L : this.recoveryStartTime.getEpochSecond();
    }

    public long getChangesRecovered() {
        return this.changesRecovered.get();
    }

    public long getChangesApplied() {
        return this.totalChangesApplied.get();
    }

    public long getMilliSecondsSinceLastAppliedChange() {
        return this.lastChangeAppliedTimestamp == null ? -1L : Duration.between(this.lastChangeAppliedTimestamp, Instant.now()).toMillis();
    }

    public long getMilliSecondsSinceLastRecoveredChange() {
        return this.lastChangeRecoveredTimestamp == null ? -1L : Duration.between(this.lastChangeRecoveredTimestamp, Instant.now()).toMillis();
    }

    public String getLastAppliedChange() {
        return this.lastAppliedChange == null ? "" : this.lastAppliedChange.toString();
    }

    public String getLastRecoveredChange() {
        return this.lastRecoveredChange == null ? "" : this.lastRecoveredChange.toString();
    }

    public void started() {
        this.status = SchemaHistoryStatus.RUNNING;
        this.register();
    }

    public void stopped() {
        this.status = SchemaHistoryStatus.STOPPED;
        this.unregister();
    }

    public void recoveryStarted() {
        this.status = SchemaHistoryStatus.RECOVERING;
        this.recoveryStartTime = Instant.now();
        LOGGER.info("Started database schema history recovery");
    }

    public void recoveryStopped() {
        this.status = SchemaHistoryStatus.RUNNING;
        LOGGER.info("Finished database schema history recovery of {} change(s) in {} ms", this.changesRecovered.get(), Duration.between(this.recoveryStartTime, Instant.now()).toMillis());
    }

    public void onChangeFromHistory(HistoryRecord record) {
        this.lastRecoveredChange = record;
        this.changesRecovered.incrementAndGet();
        if (this.lastChangeRecoveredLogDelay.hasElapsed()) {
            LOGGER.info("Database schema history recovery in progress, recovered {} records", this.changesRecovered);
        }

        this.lastChangeRecoveredTimestamp = Instant.now();
    }

    public void onChangeApplied(HistoryRecord record) {
        this.lastAppliedChange = record;
        this.totalChangesApplied.incrementAndGet();
        if (this.lastChangeAppliedLogDelay.hasElapsed()) {
            LOGGER.info("Already applied {} database changes", this.totalChangesApplied);
        }

        this.lastChangeAppliedTimestamp = Instant.now();
    }

    public static enum SchemaHistoryStatus {
        STOPPED,
        RECOVERING,
        RUNNING;

        // $FF: synthetic method
        private static SchemaHistoryStatus[] $values() {
            return new SchemaHistoryStatus[]{STOPPED, RECOVERING, RUNNING};
        }
    }
}
