package io.debezium.pipeline.metrics;

import io.debezium.annotation.ThreadSafe;
import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.data.Envelope;
import io.debezium.metrics.Metrics;
import io.debezium.pipeline.ConnectorEvent;
import io.debezium.pipeline.meters.CommonEventMeter;
import io.debezium.pipeline.source.spi.DataChangeEventListener;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Struct;

import java.util.Map;

@ThreadSafe
public abstract class PipelineMetrics<P extends Partition> extends Metrics implements DataChangeEventListener<P>, ChangeEventSourceMetricsMXBean {
    protected final EventMetadataProvider metadataProvider;
    private final ChangeEventQueueMetrics changeEventQueueMetrics;
    protected final CdcSourceTaskContext taskContext;
    private final CommonEventMeter commonEventMeter;

    protected <T extends CdcSourceTaskContext> PipelineMetrics(T taskContext, String contextName, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider) {
        super(taskContext, contextName);
        this.taskContext = taskContext;
        this.changeEventQueueMetrics = changeEventQueueMetrics;
        this.metadataProvider = metadataProvider;
        this.commonEventMeter = new CommonEventMeter(taskContext.getClock(), metadataProvider);
    }

    protected <T extends CdcSourceTaskContext> PipelineMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider, Map<String, String> tags) {
        super(taskContext, tags);
        this.taskContext = taskContext;
        this.changeEventQueueMetrics = changeEventQueueMetrics;
        this.metadataProvider = metadataProvider;
        this.commonEventMeter = new CommonEventMeter(taskContext.getClock(), metadataProvider);
    }

    public void onEvent(P partition, DataCollectionId source, OffsetContext offset, Object key, Struct value, Envelope.Operation operation) {
        this.commonEventMeter.onEvent(source, offset, key, value, operation);
    }

    public void onFilteredEvent(P partition, String event) {
        this.commonEventMeter.onFilteredEvent();
    }

    public void onFilteredEvent(P partition, String event, Envelope.Operation operation) {
        this.commonEventMeter.onFilteredEvent(operation);
    }

    public void onErroneousEvent(P partition, String event) {
        this.commonEventMeter.onErroneousEvent();
    }

    public void onErroneousEvent(P partition, String event, Envelope.Operation operation) {
        this.commonEventMeter.onErroneousEvent(operation);
    }

    public void onConnectorEvent(P partition, ConnectorEvent event) {
    }

    public String getLastEvent() {
        return this.commonEventMeter.getLastEvent();
    }

    public long getMilliSecondsSinceLastEvent() {
        return this.commonEventMeter.getMilliSecondsSinceLastEvent();
    }

    public long getTotalNumberOfEventsSeen() {
        return this.commonEventMeter.getTotalNumberOfEventsSeen();
    }

    public long getTotalNumberOfCreateEventsSeen() {
        return this.commonEventMeter.getTotalNumberOfCreateEventsSeen();
    }

    public long getTotalNumberOfUpdateEventsSeen() {
        return this.commonEventMeter.getTotalNumberOfUpdateEventsSeen();
    }

    public long getTotalNumberOfDeleteEventsSeen() {
        return this.commonEventMeter.getTotalNumberOfDeleteEventsSeen();
    }

    public long getNumberOfEventsFiltered() {
        return this.commonEventMeter.getNumberOfEventsFiltered();
    }

    public long getNumberOfErroneousEvents() {
        return this.commonEventMeter.getNumberOfErroneousEvents();
    }

    public void reset() {
        this.commonEventMeter.reset();
    }

    public int getQueueTotalCapacity() {
        return this.changeEventQueueMetrics.totalCapacity();
    }

    public int getQueueRemainingCapacity() {
        return this.changeEventQueueMetrics.remainingCapacity();
    }

    public long getMaxQueueSizeInBytes() {
        return this.changeEventQueueMetrics.maxQueueSizeInBytes();
    }

    public long getCurrentQueueSizeInBytes() {
        return this.changeEventQueueMetrics.currentQueueSizeInBytes();
    }
}
