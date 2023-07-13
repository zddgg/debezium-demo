package io.debezium.connector.mysql;

import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.pipeline.metrics.DefaultChangeEventSourceMetricsFactory;
import io.debezium.pipeline.metrics.SnapshotChangeEventSourceMetrics;
import io.debezium.pipeline.metrics.StreamingChangeEventSourceMetrics;
import io.debezium.pipeline.source.spi.EventMetadataProvider;

public class MySqlChangeEventSourceMetricsFactory extends DefaultChangeEventSourceMetricsFactory<MySqlPartition> {
    final MySqlStreamingChangeEventSourceMetrics streamingMetrics;

    public MySqlChangeEventSourceMetricsFactory(MySqlStreamingChangeEventSourceMetrics streamingMetrics) {
        this.streamingMetrics = streamingMetrics;
    }

    public <T extends CdcSourceTaskContext> SnapshotChangeEventSourceMetrics<MySqlPartition> getSnapshotMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider eventMetadataProvider) {
        return new MySqlSnapshotChangeEventSourceMetrics((MySqlTaskContext) taskContext, changeEventQueueMetrics, eventMetadataProvider);
    }

    public <T extends CdcSourceTaskContext> StreamingChangeEventSourceMetrics<MySqlPartition> getStreamingMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider eventMetadataProvider) {
        return this.streamingMetrics;
    }

    public boolean connectionMetricHandledByCoordinator() {
        return false;
    }
}
