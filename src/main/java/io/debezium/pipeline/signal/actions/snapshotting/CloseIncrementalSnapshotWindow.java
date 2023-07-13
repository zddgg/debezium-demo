package io.debezium.pipeline.signal.actions.snapshotting;

import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.signal.actions.SignalAction;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseIncrementalSnapshotWindow<P extends Partition> implements SignalAction<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloseIncrementalSnapshotWindow.class);
    public static final String NAME = "snapshot-window-close";
    private final EventDispatcher<P, ? extends DataCollectionId> dispatcher;

    public CloseIncrementalSnapshotWindow(EventDispatcher<P, ? extends DataCollectionId> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean arrived(SignalPayload<P> signalPayload) throws InterruptedException {
        this.dispatcher.getIncrementalSnapshotChangeEventSource().closeWindow(signalPayload.partition, signalPayload.id, signalPayload.offsetContext);
        return true;
    }
}
