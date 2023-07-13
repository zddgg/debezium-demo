package io.debezium.pipeline.signal.actions.snapshotting;

import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.signal.actions.SignalAction;
import io.debezium.pipeline.spi.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenIncrementalSnapshotWindow<P extends Partition> implements SignalAction<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIncrementalSnapshotWindow.class);
    public static final String NAME = "snapshot-window-open";

    public boolean arrived(SignalPayload<P> signalPayload) {
        signalPayload.offsetContext.getIncrementalSnapshotContext().openWindow(signalPayload.id);
        return true;
    }
}
