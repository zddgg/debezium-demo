package io.debezium.pipeline.meters;

import io.debezium.annotation.ThreadSafe;
import io.debezium.data.Envelope;
import io.debezium.pipeline.metrics.traits.CommonEventMetricsMXBean;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Clock;
import org.apache.kafka.connect.data.Struct;

import java.util.concurrent.atomic.AtomicLong;

@ThreadSafe
public class CommonEventMeter implements CommonEventMetricsMXBean {
    protected final AtomicLong totalNumberOfEventsSeen = new AtomicLong();
    protected final AtomicLong totalNumberOfCreateEventsSeen = new AtomicLong();
    protected final AtomicLong totalNumberOfUpdateEventsSeen = new AtomicLong();
    protected final AtomicLong totalNumberOfDeleteEventsSeen = new AtomicLong();
    private final AtomicLong numberOfEventsFiltered = new AtomicLong();
    protected final AtomicLong numberOfErroneousEvents = new AtomicLong();
    protected final AtomicLong lastEventTimestamp = new AtomicLong(-1L);
    private volatile String lastEvent;
    private final Clock clock;
    private final EventMetadataProvider metadataProvider;

    public CommonEventMeter(Clock clock, EventMetadataProvider metadataProvider) {
        this.clock = clock;
        this.metadataProvider = metadataProvider;
    }

    public void onEvent(DataCollectionId source, OffsetContext offset, Object key, Struct value, Envelope.Operation operation) {
        this.updateCommonEventMetrics(operation);
        this.lastEvent = this.metadataProvider.toSummaryString(source, offset, key, value);
    }

    private void updateCommonEventMetrics() {
        this.updateCommonEventMetrics((Envelope.Operation) null);
    }

    private void updateCommonEventMetrics(Envelope.Operation operation) {
        this.totalNumberOfEventsSeen.incrementAndGet();
        this.lastEventTimestamp.set(this.clock.currentTimeInMillis());
        if (operation != null) {
            switch (operation) {
                case CREATE:
                    this.totalNumberOfCreateEventsSeen.incrementAndGet();
                    break;
                case UPDATE:
                    this.totalNumberOfUpdateEventsSeen.incrementAndGet();
                    break;
                case DELETE:
                    this.totalNumberOfDeleteEventsSeen.incrementAndGet();
            }
        }

    }

    public void onFilteredEvent() {
        this.numberOfEventsFiltered.incrementAndGet();
        this.updateCommonEventMetrics();
    }

    public void onFilteredEvent(Envelope.Operation operation) {
        this.numberOfEventsFiltered.incrementAndGet();
        this.updateCommonEventMetrics(operation);
    }

    public void onErroneousEvent() {
        this.numberOfErroneousEvents.incrementAndGet();
        this.updateCommonEventMetrics();
    }

    public void onErroneousEvent(Envelope.Operation operation) {
        this.numberOfErroneousEvents.incrementAndGet();
        this.updateCommonEventMetrics(operation);
    }

    public String getLastEvent() {
        return this.lastEvent;
    }

    public long getMilliSecondsSinceLastEvent() {
        return this.lastEventTimestamp.get() == -1L ? -1L : this.clock.currentTimeInMillis() - this.lastEventTimestamp.get();
    }

    public long getTotalNumberOfEventsSeen() {
        return this.totalNumberOfEventsSeen.get();
    }

    public long getTotalNumberOfCreateEventsSeen() {
        return this.totalNumberOfCreateEventsSeen.get();
    }

    public long getTotalNumberOfUpdateEventsSeen() {
        return this.totalNumberOfUpdateEventsSeen.get();
    }

    public long getTotalNumberOfDeleteEventsSeen() {
        return this.totalNumberOfDeleteEventsSeen.get();
    }

    public long getNumberOfEventsFiltered() {
        return this.numberOfEventsFiltered.get();
    }

    public long getNumberOfErroneousEvents() {
        return this.numberOfErroneousEvents.get();
    }

    public void reset() {
        this.totalNumberOfEventsSeen.set(0L);
        this.totalNumberOfCreateEventsSeen.set(0L);
        this.totalNumberOfUpdateEventsSeen.set(0L);
        this.totalNumberOfDeleteEventsSeen.set(0L);
        this.lastEventTimestamp.set(-1L);
        this.numberOfEventsFiltered.set(0L);
        this.numberOfErroneousEvents.set(0L);
        this.lastEvent = null;
    }
}
