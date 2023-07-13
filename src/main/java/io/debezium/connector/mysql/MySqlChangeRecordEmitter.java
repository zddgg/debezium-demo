package io.debezium.connector.mysql;

import io.debezium.data.Envelope.Operation;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.relational.RelationalChangeRecordEmitter;
import io.debezium.relational.TableSchema;
import io.debezium.util.Clock;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;

import java.io.Serializable;

public class MySqlChangeRecordEmitter extends RelationalChangeRecordEmitter<MySqlPartition> {
    private final Operation operation;
    private final OffsetContext offset;
    private final Object[] before;
    private final Object[] after;

    public MySqlChangeRecordEmitter(MySqlPartition partition, OffsetContext offset, Clock clock, Operation operation, Serializable[] before, Serializable[] after, MySqlConnectorConfig connectorConfig) {
        super(partition, offset, clock, connectorConfig);
        this.offset = offset;
        this.operation = operation;
        this.before = before;
        this.after = after;
    }

    public OffsetContext getOffset() {
        return this.offset;
    }

    public Operation getOperation() {
        return this.operation;
    }

    protected Object[] getOldColumnValues() {
        return this.before;
    }

    protected Object[] getNewColumnValues() {
        return this.after;
    }

    protected void emitTruncateRecord(Receiver receiver, TableSchema tableSchema) throws InterruptedException {
        Struct envelope = tableSchema.getEnvelopeSchema().truncate(this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
        receiver.changeRecord(this.getPartition(), tableSchema, Operation.TRUNCATE, (Object) null, envelope, this.getOffset(), (ConnectHeaders) null);
    }
}
