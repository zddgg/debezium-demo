package io.debezium.connector.mysql;

import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.pipeline.metrics.DefaultSnapshotChangeEventSourceMetrics;
import io.debezium.pipeline.source.spi.EventMetadataProvider;

import java.util.concurrent.atomic.AtomicBoolean;

class MySqlSnapshotChangeEventSourceMetrics extends DefaultSnapshotChangeEventSourceMetrics<MySqlPartition> implements MySqlSnapshotChangeEventSourceMetricsMXBean {
    private final AtomicBoolean holdingGlobalLock = new AtomicBoolean();

    MySqlSnapshotChangeEventSourceMetrics(MySqlTaskContext taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider eventMetadataProvider) {
        super(taskContext, changeEventQueueMetrics, eventMetadataProvider);
    }

    public boolean getHoldingGlobalLock() {
        return this.holdingGlobalLock.get();
    }

    public void globalLockAcquired() {
        this.holdingGlobalLock.set(true);
    }

    public void globalLockReleased() {
        this.holdingGlobalLock.set(false);
    }

    public long getTotalNumberOfEventsSeen() {
        return this.getRowsScanned().values().stream().mapToLong((x) -> {
            return x;
        }).sum();
    }
}
