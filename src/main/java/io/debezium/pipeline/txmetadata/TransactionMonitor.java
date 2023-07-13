package io.debezium.pipeline.txmetadata;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.function.BlockingConsumer;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.schema.SchemaFactory;
import io.debezium.schema.SchemaNameAdjuster;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

@NotThreadSafe
public class TransactionMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionMonitor.class);
    public static final String DEBEZIUM_TRANSACTION_KEY = "transaction";
    public static final String DEBEZIUM_TRANSACTION_ID_KEY = "id";
    public static final String DEBEZIUM_TRANSACTION_TOTAL_ORDER_KEY = "total_order";
    public static final String DEBEZIUM_TRANSACTION_DATA_COLLECTION_ORDER_KEY = "data_collection_order";
    public static final String DEBEZIUM_TRANSACTION_STATUS_KEY = "status";
    public static final String DEBEZIUM_TRANSACTION_EVENT_COUNT_KEY = "event_count";
    public static final String DEBEZIUM_TRANSACTION_COLLECTION_KEY = "data_collection";
    public static final String DEBEZIUM_TRANSACTION_DATA_COLLECTIONS_KEY = "data_collections";
    public static final String DEBEZIUM_TRANSACTION_TS_MS = "ts_ms";
    public static final Schema TRANSACTION_BLOCK_SCHEMA = SchemaFactory.get().transactionBlockSchema();
    private static final Schema EVENT_COUNT_PER_DATA_COLLECTION_SCHEMA = SchemaFactory.get().transactionEventCountPerDataCollectionSchema();
    protected final Schema transactionKeySchema;
    protected final Schema transactionValueSchema;
    private final EventMetadataProvider eventMetadataProvider;
    private final String topicName;
    private final BlockingConsumer<SourceRecord> sender;
    private final CommonConnectorConfig connectorConfig;

    public TransactionMonitor(CommonConnectorConfig connectorConfig, EventMetadataProvider eventMetadataProvider, SchemaNameAdjuster schemaNameAdjuster, BlockingConsumer<SourceRecord> sender, String topicName) {
        Objects.requireNonNull(eventMetadataProvider);
        this.transactionKeySchema = SchemaFactory.get().transactionKeySchema(schemaNameAdjuster);
        this.transactionValueSchema = SchemaFactory.get().transactionValueSchema(schemaNameAdjuster);
        this.topicName = topicName;
        this.eventMetadataProvider = eventMetadataProvider;
        this.sender = sender;
        this.connectorConfig = connectorConfig;
    }

    public void dataEvent(Partition partition, DataCollectionId source, OffsetContext offset, Object key, Struct value) throws InterruptedException {
        if (this.connectorConfig.shouldProvideTransactionMetadata()) {
            TransactionContext transactionContext = offset.getTransactionContext();
            String txId = this.eventMetadataProvider.getTransactionId(source, offset, key, value);
            if (txId == null) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Event '{}' has no transaction id", this.eventMetadataProvider.toSummaryString(source, offset, key, value));
                }

                if (transactionContext.isTransactionInProgress()) {
                    LOGGER.trace("Transaction was in progress, executing implicit transaction commit");
                    this.endTransaction(partition, offset, this.eventMetadataProvider.getEventTimestamp(source, offset, key, value));
                }

            } else {
                if (!transactionContext.isTransactionInProgress()) {
                    transactionContext.beginTransaction(txId);
                    this.beginTransaction(partition, offset, this.eventMetadataProvider.getEventTimestamp(source, offset, key, value));
                } else if (!transactionContext.getTransactionId().equals(txId)) {
                    this.endTransaction(partition, offset, this.eventMetadataProvider.getEventTimestamp(source, offset, key, value));
                    transactionContext.endTransaction();
                    transactionContext.beginTransaction(txId);
                    this.beginTransaction(partition, offset, this.eventMetadataProvider.getEventTimestamp(source, offset, key, value));
                }

                this.transactionEvent(offset, source, value);
            }
        }
    }

    public void transactionComittedEvent(Partition partition, OffsetContext offset, Instant timestamp) throws InterruptedException {
        if (this.connectorConfig.shouldProvideTransactionMetadata()) {
            if (offset.getTransactionContext().isTransactionInProgress()) {
                this.endTransaction(partition, offset, timestamp);
            }

            offset.getTransactionContext().endTransaction();
        }
    }

    public void transactionStartedEvent(Partition partition, String transactionId, OffsetContext offset, Instant timestamp) throws InterruptedException {
        if (this.connectorConfig.shouldProvideTransactionMetadata()) {
            offset.getTransactionContext().beginTransaction(transactionId);
            this.beginTransaction(partition, offset, timestamp);
        }
    }

    protected Struct prepareTxKey(OffsetContext offsetContext) {
        Struct key = new Struct(this.transactionKeySchema);
        key.put("id", offsetContext.getTransactionContext().getTransactionId());
        return key;
    }

    protected Struct prepareTxBeginValue(OffsetContext offsetContext, Instant timestamp) {
        Struct value = new Struct(this.transactionValueSchema);
        value.put("status", TransactionStatus.BEGIN.name());
        value.put("id", offsetContext.getTransactionContext().getTransactionId());
        value.put("ts_ms", timestamp.toEpochMilli());
        return value;
    }

    protected Struct prepareTxEndValue(OffsetContext offsetContext, Instant timestamp) {
        Struct value = new Struct(this.transactionValueSchema);
        value.put("status", TransactionStatus.END.name());
        value.put("id", offsetContext.getTransactionContext().getTransactionId());
        value.put("ts_ms", timestamp.toEpochMilli());
        value.put("event_count", offsetContext.getTransactionContext().getTotalEventCount());
        Set<Map.Entry<String, Long>> perTableEventCount = offsetContext.getTransactionContext().getPerTableEventCount().entrySet();
        List<Struct> valuePerTableCount = new ArrayList(perTableEventCount.size());
        Iterator var6 = perTableEventCount.iterator();

        while (var6.hasNext()) {
            Map.Entry<String, Long> tableEventCount = (Map.Entry) var6.next();
            Struct perTable = new Struct(EVENT_COUNT_PER_DATA_COLLECTION_SCHEMA);
            perTable.put("data_collection", tableEventCount.getKey());
            perTable.put("event_count", tableEventCount.getValue());
            valuePerTableCount.add(perTable);
        }

        value.put("data_collections", valuePerTableCount);
        return value;
    }

    protected Struct prepareTxStruct(OffsetContext offsetContext, long dataCollectionEventOrder, Struct value) {
        Struct txStruct = new Struct(TRANSACTION_BLOCK_SCHEMA);
        txStruct.put("id", offsetContext.getTransactionContext().getTransactionId());
        txStruct.put("total_order", offsetContext.getTransactionContext().getTotalEventCount());
        txStruct.put("data_collection_order", dataCollectionEventOrder);
        return txStruct;
    }

    private void transactionEvent(OffsetContext offsetContext, DataCollectionId source, Struct value) {
        long dataCollectionEventOrder = offsetContext.getTransactionContext().event(source);
        if (value == null) {
            LOGGER.debug("Event with key {} without value. Cannot enrich source block.");
        } else {
            Struct txStruct = this.prepareTxStruct(offsetContext, dataCollectionEventOrder, value);
            value.put("transaction", txStruct);
        }
    }

    private void beginTransaction(Partition partition, OffsetContext offsetContext, Instant timestamp) throws InterruptedException {
        Struct key = this.prepareTxKey(offsetContext);
        Struct value = this.prepareTxBeginValue(offsetContext, timestamp);
        this.sender.accept(new SourceRecord(partition.getSourcePartition(), offsetContext.getOffset(), this.topicName, (Integer) null, key.schema(), key, value.schema(), value));
    }

    private void endTransaction(Partition partition, OffsetContext offsetContext, Instant timestamp) throws InterruptedException {
        Struct key = this.prepareTxKey(offsetContext);
        Struct value = this.prepareTxEndValue(offsetContext, timestamp);
        this.sender.accept(new SourceRecord(partition.getSourcePartition(), offsetContext.getOffset(), this.topicName, (Integer) null, key.schema(), key, value.schema(), value));
    }
}
