package io.debezium.pipeline.metrics.traits;

import java.util.Map;

public interface SnapshotMetricsMXBean extends SchemaMetricsMXBean {
    int getTotalTableCount();

    int getRemainingTableCount();

    boolean getSnapshotRunning();

    boolean getSnapshotPaused();

    boolean getSnapshotCompleted();

    boolean getSnapshotAborted();

    long getSnapshotDurationInSeconds();

    long getSnapshotPausedDurationInSeconds();

    Map<String, Long> getRowsScanned();

    String getChunkId();

    String getChunkFrom();

    String getChunkTo();

    String getTableFrom();

    String getTableTo();
}
