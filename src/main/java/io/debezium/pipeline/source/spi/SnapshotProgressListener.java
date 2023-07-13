package io.debezium.pipeline.source.spi;

import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.TableId;
import io.debezium.spi.schema.DataCollectionId;

public interface SnapshotProgressListener<P extends Partition> {
    void snapshotStarted(P var1);

    void snapshotPaused(P var1);

    void snapshotResumed(P var1);

    void monitoredDataCollectionsDetermined(P var1, Iterable<? extends DataCollectionId> var2);

    void snapshotCompleted(P var1);

    void snapshotAborted(P var1);

    void dataCollectionSnapshotCompleted(P var1, DataCollectionId var2, long var3);

    void rowsScanned(P var1, TableId var2, long var3);

    void currentChunk(P var1, String var2, Object[] var3, Object[] var4);

    void currentChunk(P var1, String var2, Object[] var3, Object[] var4, Object[] var5);

    static <P extends Partition> SnapshotProgressListener<P> NO_OP() {
        return new SnapshotProgressListener<P>() {
            public void snapshotStarted(P partition) {
            }

            public void snapshotPaused(P partition) {
            }

            public void snapshotResumed(P partition) {
            }

            public void rowsScanned(P partition, TableId tableId, long numRows) {
            }

            public void monitoredDataCollectionsDetermined(P partition, Iterable<? extends DataCollectionId> dataCollectionIds) {
            }

            public void dataCollectionSnapshotCompleted(P partition, DataCollectionId dataCollectionId, long numRows) {
            }

            public void snapshotCompleted(P partition) {
            }

            public void snapshotAborted(P partition) {
            }

            public void currentChunk(P partition, String chunkId, Object[] chunkFrom, Object[] chunkTo) {
            }

            public void currentChunk(P partition, String chunkId, Object[] chunkFrom, Object[] chunkTo, Object[] tableTo) {
            }
        };
    }
}
