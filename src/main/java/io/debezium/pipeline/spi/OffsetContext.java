package io.debezium.pipeline.spi;

import io.debezium.connector.SnapshotRecord;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotContext;
import io.debezium.pipeline.txmetadata.TransactionContext;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;
import java.util.Map;

public interface OffsetContext {
    Map<String, ?> getOffset();

    Schema getSourceInfoSchema();

    Struct getSourceInfo();

    boolean isSnapshotRunning();

    void markSnapshotRecord(SnapshotRecord var1);

    void preSnapshotStart();

    void preSnapshotCompletion();

    void postSnapshotCompletion();

    void event(DataCollectionId var1, Instant var2);

    TransactionContext getTransactionContext();

    default void incrementalSnapshotEvents() {
    }

    default IncrementalSnapshotContext<?> getIncrementalSnapshotContext() {
        return null;
    }

    public interface Loader<O extends OffsetContext> {
        O load(Map<String, ?> var1);
    }
}
