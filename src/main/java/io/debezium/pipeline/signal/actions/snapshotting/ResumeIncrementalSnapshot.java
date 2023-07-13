package io.debezium.pipeline.signal.actions.snapshotting;

import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.signal.actions.AbstractSnapshotSignal;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;

public class ResumeIncrementalSnapshot<P extends Partition> extends AbstractSnapshotSignal<P> {
    public static final String NAME = "resume-snapshot";
    private final EventDispatcher<P, ? extends DataCollectionId> dispatcher;

    public ResumeIncrementalSnapshot(EventDispatcher<P, ? extends DataCollectionId> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean arrived(SignalPayload<P> signalPayload) throws InterruptedException {
        this.dispatcher.getIncrementalSnapshotChangeEventSource().resumeSnapshot(signalPayload.partition, signalPayload.offsetContext);
        return true;
    }
}
