package io.debezium.pipeline.source.snapshot.incremental;

import io.debezium.relational.Table;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IncrementalSnapshotContext<T> {
    DataCollection<T> currentDataCollectionId();

    DataCollection<T> nextDataCollection();

    List<DataCollection<T>> addDataCollectionNamesToSnapshot(String var1, List<String> var2, Optional<String> var3, Optional<String> var4);

    int dataCollectionsToBeSnapshottedCount();

    boolean openWindow(String var1);

    boolean closeWindow(String var1);

    void pauseSnapshot();

    void resumeSnapshot();

    boolean isSnapshotPaused();

    boolean isNonInitialChunk();

    boolean snapshotRunning();

    void startNewChunk();

    void nextChunkPosition(Object[] var1);

    String currentChunkId();

    Object[] chunkEndPosititon();

    void sendEvent(Object[] var1);

    void maximumKey(Object[] var1);

    Optional<Object[]> maximumKey();

    boolean deduplicationNeeded();

    Map<String, Object> store(Map<String, Object> var1);

    void revertChunk();

    void setSchema(Table var1);

    Table getSchema();

    boolean isSchemaVerificationPassed();

    void setSchemaVerificationPassed(boolean var1);

    void stopSnapshot();

    boolean removeDataCollectionFromSnapshot(String var1);

    List<DataCollection<T>> getDataCollections();

    void unsetCorrelationId();

    String getCorrelationId();
}
