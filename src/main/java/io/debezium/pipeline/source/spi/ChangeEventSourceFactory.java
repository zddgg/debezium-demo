package io.debezium.pipeline.source.spi;

import io.debezium.pipeline.notification.NotificationService;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotChangeEventSource;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Optional;

public interface ChangeEventSourceFactory<P extends Partition, O extends OffsetContext> {
    SnapshotChangeEventSource<P, O> getSnapshotChangeEventSource(SnapshotProgressListener<P> var1);

    StreamingChangeEventSource<P, O> getStreamingChangeEventSource();

    default Optional<IncrementalSnapshotChangeEventSource<P, ? extends DataCollectionId>> getIncrementalSnapshotChangeEventSource(O offsetContext, SnapshotProgressListener<P> snapshotProgressListener, DataChangeEventListener<P> dataChangeEventListener, NotificationService<P, O> notificationService) {
        return Optional.empty();
    }
}
