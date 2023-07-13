package io.debezium.pipeline.signal.actions.snapshotting;

import io.debezium.document.Array;
import io.debezium.document.Document;
import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.signal.actions.AbstractSnapshotSignal;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class StopSnapshot<P extends Partition> extends AbstractSnapshotSignal<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopSnapshot.class);
    public static final String NAME = "stop-snapshot";
    private final EventDispatcher<P, ? extends DataCollectionId> dispatcher;

    public StopSnapshot(EventDispatcher<P, ? extends DataCollectionId> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean arrived(SignalPayload<P> signalPayload) throws InterruptedException {
        List<String> dataCollections = getDataCollections(signalPayload.data);
        SnapshotType type = getSnapshotType(signalPayload.data);
        LOGGER.info("Requested stop of snapshot '{}' for data collections '{}'", type, dataCollections);
        switch (type) {
            case INCREMENTAL:
                this.dispatcher.getIncrementalSnapshotChangeEventSource().stopSnapshot(signalPayload.partition, signalPayload.offsetContext, signalPayload.additionalData, dataCollections);
            default:
                return true;
        }
    }

    public static List<String> getDataCollections(Document data) {
        Array dataCollectionsArray = data.getArray("data-collections");
        return dataCollectionsArray != null && !dataCollectionsArray.isEmpty() ? (List) dataCollectionsArray.streamValues().map((v) -> {
            return v.asString().trim();
        }).collect(Collectors.toList()) : null;
    }
}
