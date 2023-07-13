package io.debezium.pipeline.source.snapshot.incremental;

import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IncrementalSnapshotChangeEventSource<P extends Partition, T extends DataCollectionId> {
    void closeWindow(P var1, String var2, OffsetContext var3) throws InterruptedException;

    void pauseSnapshot(P var1, OffsetContext var2) throws InterruptedException;

    void resumeSnapshot(P var1, OffsetContext var2) throws InterruptedException;

    void processMessage(P var1, DataCollectionId var2, Object var3, OffsetContext var4) throws InterruptedException;

    void init(P var1, OffsetContext var2);

    void addDataCollectionNamesToSnapshot(SignalPayload<P> var1, List<String> var2, Optional<String> var3, Optional<String> var4) throws InterruptedException;

    void stopSnapshot(P var1, OffsetContext var2, Map<String, Object> var3, List<String> var4);

    default void processHeartbeat(P partition, OffsetContext offsetContext) throws InterruptedException {
    }

    default void processFilteredEvent(P partition, OffsetContext offsetContext) throws InterruptedException {
    }

    default void processTransactionStartedEvent(P partition, OffsetContext offsetContext) throws InterruptedException {
    }

    default void processTransactionCommittedEvent(P partition, OffsetContext offsetContext) throws InterruptedException {
    }

    default void processSchemaChange(P partition, OffsetContext offsetContext, DataCollectionId dataCollectionId) throws InterruptedException {
    }
}
