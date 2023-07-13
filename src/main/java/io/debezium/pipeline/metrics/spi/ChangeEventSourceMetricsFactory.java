package io.debezium.pipeline.metrics.spi;

import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.pipeline.metrics.SnapshotChangeEventSourceMetrics;
import io.debezium.pipeline.metrics.StreamingChangeEventSourceMetrics;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.Partition;

public interface ChangeEventSourceMetricsFactory<P extends Partition> {
    <T extends CdcSourceTaskContext> SnapshotChangeEventSourceMetrics<P> getSnapshotMetrics(T var1, ChangeEventQueueMetrics var2, EventMetadataProvider var3);

    <T extends CdcSourceTaskContext> StreamingChangeEventSourceMetrics<P> getStreamingMetrics(T var1, ChangeEventQueueMetrics var2, EventMetadataProvider var3);

    default boolean connectionMetricHandledByCoordinator() {
        return true;
    }
}
