package io.debezium.pipeline.txmetadata;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@NotThreadSafe
public class TransactionContext {
    private static final String OFFSET_TRANSACTION_ID = "transaction_id";
    private static final String OFFSET_TABLE_COUNT_PREFIX = "transaction_data_collection_order_";
    private static final int OFFSET_TABLE_COUNT_PREFIX_LENGTH = "transaction_data_collection_order_".length();
    private String transactionId = null;
    private final Map<String, Long> perTableEventCount = new HashMap();
    private final Map<String, Long> viewPerTableEventCount;
    private long totalEventCount;

    public TransactionContext() {
        this.viewPerTableEventCount = Collections.unmodifiableMap(this.perTableEventCount);
        this.totalEventCount = 0L;
    }

    private void reset() {
        this.transactionId = null;
        this.totalEventCount = 0L;
        this.perTableEventCount.clear();
    }

    public Map<String, Object> store(Map<String, Object> offset) {
        offset.put("transaction_id", this.transactionId);
        String tableCountPrefix = "transaction_data_collection_order_";
        Iterator var3 = this.perTableEventCount.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<String, Long> e = (Map.Entry) var3.next();
            offset.put("transaction_data_collection_order_" + (String) e.getKey(), e.getValue());
        }

        return offset;
    }

    public static TransactionContext load(Map<String, ?> offsets) {
        TransactionContext context = new TransactionContext();
        context.transactionId = (String) offsets.get("transaction_id");
        Iterator var3 = offsets.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<String, Object> offset = (Map.Entry) var3.next();
            if (((String) offset.getKey()).startsWith("transaction_data_collection_order_")) {
                String dataCollectionId = ((String) offset.getKey()).substring(OFFSET_TABLE_COUNT_PREFIX_LENGTH);
                Long count = (Long) offset.getValue();
                context.perTableEventCount.put(dataCollectionId, count);
            }
        }

        context.totalEventCount = context.perTableEventCount.values().stream().mapToLong((x) -> {
            return x;
        }).sum();
        return context;
    }

    public boolean isTransactionInProgress() {
        return this.transactionId != null;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public long getTotalEventCount() {
        return this.totalEventCount;
    }

    public void beginTransaction(String txId) {
        this.reset();
        this.transactionId = txId;
    }

    public void endTransaction() {
        this.reset();
    }

    public long event(DataCollectionId source) {
        ++this.totalEventCount;
        String sourceName = source.toString();
        long dataCollectionEventOrder = (Long) this.perTableEventCount.getOrDefault(sourceName, 0L) + 1L;
        this.perTableEventCount.put(sourceName, dataCollectionEventOrder);
        return dataCollectionEventOrder;
    }

    public Map<String, Long> getPerTableEventCount() {
        return this.viewPerTableEventCount;
    }

    public String toString() {
        return "TransactionContext [currentTransactionId=" + this.transactionId + ", perTableEventCount=" + this.perTableEventCount + ", totalEventCount=" + this.totalEventCount + "]";
    }
}
