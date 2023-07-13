package io.debezium.pipeline.metrics;

import io.debezium.pipeline.metrics.traits.SnapshotMetricsMXBean;

public interface SnapshotChangeEventSourceMetricsMXBean extends ChangeEventSourceMetricsMXBean, SnapshotMetricsMXBean {
}
