package io.debezium.pipeline.metrics;

import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.pipeline.metrics.spi.ChangeEventSourceMetricsFactory;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.Partition;

public class DefaultChangeEventSourceMetricsFactory<P extends Partition> implements ChangeEventSourceMetricsFactory<P> {
    public <T extends CdcSourceTaskContext> SnapshotChangeEventSourceMetrics<P> getSnapshotMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider eventMetadataProvider) {
        return new DefaultSnapshotChangeEventSourceMetrics(taskContext, changeEventQueueMetrics, eventMetadataProvider);
    }

    public <T extends CdcSourceTaskContext> StreamingChangeEventSourceMetrics<P> getStreamingMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider eventMetadataProvider) {
        return new DefaultStreamingChangeEventSourceMetrics(taskContext, changeEventQueueMetrics, eventMetadataProvider);
    }
}
