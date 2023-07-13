package io.debezium.relational;

import io.debezium.data.Envelope;
import io.debezium.pipeline.AbstractChangeRecordEmitter;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.schema.DataCollectionSchema;
import io.debezium.util.Clock;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.header.ConnectHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class RelationalChangeRecordEmitter<P extends Partition> extends AbstractChangeRecordEmitter<P, TableSchema> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationalChangeRecordEmitter.class);
    public static final String PK_UPDATE_OLDKEY_FIELD = "__debezium.oldkey";
    public static final String PK_UPDATE_NEWKEY_FIELD = "__debezium.newkey";

    public RelationalChangeRecordEmitter(P partition, OffsetContext offsetContext, Clock clock, RelationalDatabaseConnectorConfig connectorConfig) {
        super(partition, offsetContext, clock, connectorConfig);
    }

    public void emitChangeRecords(DataCollectionSchema schema, Receiver<P> receiver) throws InterruptedException {
        TableSchema tableSchema = (TableSchema) schema;
        Envelope.Operation operation = this.getOperation();
        switch (operation) {
            case CREATE:
                this.emitCreateRecord(receiver, tableSchema);
                break;
            case READ:
                this.emitReadRecord(receiver, tableSchema);
                break;
            case UPDATE:
                this.emitUpdateRecord(receiver, tableSchema);
                break;
            case DELETE:
                this.emitDeleteRecord(receiver, tableSchema);
                break;
            case TRUNCATE:
                this.emitTruncateRecord(receiver, tableSchema);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

    }

    protected void emitCreateRecord(Receiver<P> receiver, TableSchema tableSchema) throws InterruptedException {
        Object[] newColumnValues = this.getNewColumnValues();
        Struct newKey = tableSchema.keyFromColumnData(newColumnValues);
        Struct newValue = tableSchema.valueFromColumnData(newColumnValues);
        Struct envelope = tableSchema.getEnvelopeSchema().create(newValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
        if (!this.skipEmptyMessages() || newColumnValues != null && newColumnValues.length != 0) {
            receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.CREATE, newKey, envelope, this.getOffset(), (ConnectHeaders) null);
        } else {
            LOGGER.warn("no new values found for table '{}' from create message at '{}'; skipping record", tableSchema, this.getOffset().getSourceInfo());
        }
    }

    protected void emitReadRecord(Receiver<P> receiver, TableSchema tableSchema) throws InterruptedException {
        Object[] newColumnValues = this.getNewColumnValues();
        Struct newKey = tableSchema.keyFromColumnData(newColumnValues);
        Struct newValue = tableSchema.valueFromColumnData(newColumnValues);
        Struct envelope = tableSchema.getEnvelopeSchema().read(newValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
        receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.READ, newKey, envelope, this.getOffset(), (ConnectHeaders) null);
    }

    protected void emitUpdateRecord(Receiver<P> receiver, TableSchema tableSchema) throws InterruptedException {
        Object[] oldColumnValues = this.getOldColumnValues();
        Object[] newColumnValues = this.getNewColumnValues();
        Struct oldKey = tableSchema.keyFromColumnData(oldColumnValues);
        Struct newKey = tableSchema.keyFromColumnData(newColumnValues);
        Struct newValue = tableSchema.valueFromColumnData(newColumnValues);
        Struct oldValue = tableSchema.valueFromColumnData(oldColumnValues);
        if (!this.skipEmptyMessages() || newColumnValues != null && newColumnValues.length != 0) {
            if (this.skipMessagesWithoutChange() && Objects.nonNull(newValue) && newValue.equals(oldValue)) {
                LOGGER.debug("No new values found for table '{}' in included columns from update message at '{}'; skipping record", tableSchema, this.getOffset().getSourceInfo());
            } else {
                if (oldKey != null && !Objects.equals(oldKey, newKey)) {
                    this.emitUpdateAsPrimaryKeyChangeRecord(receiver, tableSchema, oldKey, newKey, oldValue, newValue);
                } else {
                    Struct envelope = tableSchema.getEnvelopeSchema().update(oldValue, newValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
                    receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.UPDATE, newKey, envelope, this.getOffset(), (ConnectHeaders) null);
                }

            }
        } else {
            LOGGER.debug("no new values found for table '{}' from update message at '{}'; skipping record", tableSchema, this.getOffset().getSourceInfo());
        }
    }

    protected void emitDeleteRecord(Receiver<P> receiver, TableSchema tableSchema) throws InterruptedException {
        Object[] oldColumnValues = this.getOldColumnValues();
        Struct oldKey = tableSchema.keyFromColumnData(oldColumnValues);
        Struct oldValue = tableSchema.valueFromColumnData(oldColumnValues);
        if (!this.skipEmptyMessages() || oldColumnValues != null && oldColumnValues.length != 0) {
            Struct envelope = tableSchema.getEnvelopeSchema().delete(oldValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
            receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.DELETE, oldKey, envelope, this.getOffset(), (ConnectHeaders) null);
        } else {
            LOGGER.warn("no old values found for table '{}' from delete message at '{}'; skipping record", tableSchema, this.getOffset().getSourceInfo());
        }
    }

    protected void emitTruncateRecord(Receiver<P> receiver, TableSchema schema) throws InterruptedException {
        throw new UnsupportedOperationException("TRUNCATE not supported");
    }

    protected abstract Object[] getOldColumnValues();

    protected abstract Object[] getNewColumnValues();

    protected boolean skipEmptyMessages() {
        return false;
    }

    protected void emitUpdateAsPrimaryKeyChangeRecord(Receiver<P> receiver, TableSchema tableSchema, Struct oldKey, Struct newKey, Struct oldValue, Struct newValue) throws InterruptedException {
        ConnectHeaders headers = new ConnectHeaders();
        headers.add("__debezium.newkey", newKey, tableSchema.keySchema());
        Struct envelope = tableSchema.getEnvelopeSchema().delete(oldValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
        receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.DELETE, oldKey, envelope, this.getOffset(), headers);
        headers = new ConnectHeaders();
        headers.add("__debezium.oldkey", oldKey, tableSchema.keySchema());
        envelope = tableSchema.getEnvelopeSchema().create(newValue, this.getOffset().getSourceInfo(), this.getClock().currentTimeAsInstant());
        receiver.changeRecord(this.getPartition(), tableSchema, Envelope.Operation.CREATE, newKey, envelope, this.getOffset(), headers);
    }
}
