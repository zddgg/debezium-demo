package io.debezium.connector.mysql;

import io.debezium.pipeline.metrics.SnapshotChangeEventSourceMetricsMXBean;

public interface MySqlSnapshotChangeEventSourceMetricsMXBean extends SnapshotChangeEventSourceMetricsMXBean {
    boolean getHoldingGlobalLock();
}
