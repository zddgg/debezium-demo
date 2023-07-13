package io.debezium.pipeline;

import io.debezium.connector.SnapshotRecord;
import io.debezium.connector.common.BaseSourceInfo;
import io.debezium.pipeline.spi.OffsetContext;
import org.apache.kafka.connect.data.Struct;

public abstract class CommonOffsetContext<T extends BaseSourceInfo> implements OffsetContext {
    protected final T sourceInfo;

    public CommonOffsetContext(T sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public Struct getSourceInfo() {
        return this.sourceInfo.struct();
    }

    public void markSnapshotRecord(SnapshotRecord record) {
        this.sourceInfo.setSnapshot(record);
    }

    public void postSnapshotCompletion() {
        this.sourceInfo.setSnapshot(SnapshotRecord.FALSE);
    }

    public void incrementalSnapshotEvents() {
        this.sourceInfo.setSnapshot(SnapshotRecord.INCREMENTAL);
    }
}
