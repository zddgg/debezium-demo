package io.debezium.pipeline.metrics;

import io.debezium.annotation.ThreadSafe;
import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.data.Envelope;
import io.debezium.pipeline.ConnectorEvent;
import io.debezium.pipeline.meters.ConnectionMeter;
import io.debezium.pipeline.meters.StreamingMeter;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Struct;

import java.util.Map;

@ThreadSafe
public class DefaultStreamingChangeEventSourceMetrics<P extends Partition> extends PipelineMetrics<P> implements StreamingChangeEventSourceMetrics<P>, StreamingChangeEventSourceMetricsMXBean {
    private final ConnectionMeter connectionMeter;
    private final StreamingMeter streamingMeter;

    public <T extends CdcSourceTaskContext> DefaultStreamingChangeEventSourceMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider) {
        super(taskContext, "streaming", changeEventQueueMetrics, metadataProvider);
        this.streamingMeter = new StreamingMeter(taskContext, metadataProvider);
        this.connectionMeter = new ConnectionMeter();
    }

    public <T extends CdcSourceTaskContext> DefaultStreamingChangeEventSourceMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider, Map<String, String> tags) {
        super(taskContext, changeEventQueueMetrics, metadataProvider, tags);
        this.streamingMeter = new StreamingMeter(taskContext, metadataProvider);
        this.connectionMeter = new ConnectionMeter();
    }

    public boolean isConnected() {
        return this.connectionMeter.isConnected();
    }

    public String[] getCapturedTables() {
        return this.streamingMeter.getCapturedTables();
    }

    public void connected(boolean connected) {
        this.connectionMeter.connected(connected);
    }

    public Map<String, String> getSourceEventPosition() {
        return this.streamingMeter.getSourceEventPosition();
    }

    public long getMilliSecondsBehindSource() {
        return this.streamingMeter.getMilliSecondsBehindSource();
    }

    public long getNumberOfCommittedTransactions() {
        return this.streamingMeter.getNumberOfCommittedTransactions();
    }

    public void onEvent(P partition, DataCollectionId source, OffsetContext offset, Object key, Struct value, Envelope.Operation operation) {
        super.onEvent(partition, source, offset, key, value, operation);
        this.streamingMeter.onEvent(source, offset, key, value);
    }

    public void onConnectorEvent(P partition, ConnectorEvent event) {
    }

    public String getLastTransactionId() {
        return this.streamingMeter.getLastTransactionId();
    }

    public void reset() {
        super.reset();
        this.streamingMeter.reset();
        this.connectionMeter.reset();
    }
}
