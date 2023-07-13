package io.debezium.pipeline.metrics;

import io.debezium.pipeline.source.spi.SnapshotProgressListener;
import io.debezium.pipeline.spi.Partition;

public interface SnapshotChangeEventSourceMetrics<P extends Partition> extends ChangeEventSourceMetrics<P>, SnapshotProgressListener<P> {
}
