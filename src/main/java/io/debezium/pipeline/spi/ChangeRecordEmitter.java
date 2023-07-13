package io.debezium.pipeline.spi;

import io.debezium.data.Envelope;
import io.debezium.schema.DataCollectionSchema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;

public interface ChangeRecordEmitter<P extends Partition> {
    void emitChangeRecords(DataCollectionSchema var1, Receiver<P> var2) throws InterruptedException;

    P getPartition();

    OffsetContext getOffset();

    Envelope.Operation getOperation();

    public interface Receiver<P extends Partition> {
        void changeRecord(P var1, DataCollectionSchema var2, Envelope.Operation var3, Object var4, Struct var5, OffsetContext var6, ConnectHeaders var7) throws InterruptedException;
    }
}
