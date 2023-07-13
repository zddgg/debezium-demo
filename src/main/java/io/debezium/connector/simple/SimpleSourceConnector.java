package io.debezium.connector.simple;

import io.debezium.config.Configuration;
import io.debezium.util.Collect;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.RetriableException;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleSourceConnector extends SourceConnector {
    protected static final String VERSION = "1.0";
    public static final String TOPIC_NAME = "topic.name";
    public static final String RECORD_COUNT_PER_BATCH = "record.count.per.batch";
    public static final String BATCH_COUNT = "batch.count";
    public static final String DEFAULT_TOPIC_NAME = "simple.topic";
    public static final String INCLUDE_TIMESTAMP = "include.timestamp";
    public static final String RETRIABLE_ERROR_ON = "error.retriable.on";
    public static final int DEFAULT_RECORD_COUNT_PER_BATCH = 1;
    public static final int DEFAULT_BATCH_COUNT = 10;
    public static final boolean DEFAULT_INCLUDE_TIMESTAMP = false;
    private Map<String, String> config;

    public String version() {
        return "1.0";
    }

    public void start(Map<String, String> props) {
        this.config = props;
    }

    public Class<? extends Task> taskClass() {
        return SimpleConnectorTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> configs = new ArrayList();
        configs.add(this.config);
        return configs;
    }

    public void stop() {
    }

    public ConfigDef config() {
        return new ConfigDef();
    }

    public static class SimpleConnectorTask extends SourceTask {
        private static boolean isThrownErrorOnRecord;
        private int recordsPerBatch;
        private int errorOnRecord;
        private List<SourceRecord> records;
        private final AtomicBoolean running = new AtomicBoolean();

        public String version() {
            return "1.0";
        }

        public void start(Map<String, String> props) {
            if (this.running.compareAndSet(false, true)) {
                Configuration config = Configuration.from(props);
                this.recordsPerBatch = config.getInteger("record.count.per.batch", 1);
                int batchCount = config.getInteger("batch.count", 10);
                String topic = config.getString("topic.name", "simple.topic");
                boolean includeTimestamp = config.getBoolean("include.timestamp", false);
                this.errorOnRecord = config.getInteger("error.retriable.on", -1);
                Map<String, ?> partition = Collect.hashMapOf("source", "simple");
                Schema keySchema = SchemaBuilder.struct().name("simple.key").field("id", Schema.INT32_SCHEMA).build();
                Schema valueSchema = SchemaBuilder.struct().name("simple.value").field("batch", Schema.INT32_SCHEMA).field("record", Schema.INT32_SCHEMA).field("timestamp", Schema.OPTIONAL_INT64_SCHEMA).build();
                Map<String, ?> lastOffset = this.context.offsetStorageReader().offset(partition);
                long lastId = lastOffset == null ? 0L : (Long) lastOffset.get("id");
                this.records = new LinkedList();
                long initialTimestamp = System.currentTimeMillis();
                int id = 0;

                for (int batch = 0; batch != batchCount; ++batch) {
                    for (int recordNum = 0; recordNum != this.recordsPerBatch; ++recordNum) {
                        ++id;
                        if ((long) id > lastId) {
                            if (!this.running.get()) {
                                return;
                            }

                            Map<String, ?> offset = Collect.hashMapOf("id", id);
                            Struct key = new Struct(keySchema);
                            key.put("id", id);
                            Struct value = new Struct(valueSchema);
                            value.put("batch", batch + 1);
                            value.put("record", recordNum + 1);
                            if (includeTimestamp) {
                                value.put("timestamp", initialTimestamp + (long) id);
                            }

                            SourceRecord record = new SourceRecord(partition, offset, topic, 1, keySchema, key, valueSchema, value);
                            this.records.add(record);
                        }
                    }
                }
            }

        }

        public List<SourceRecord> poll() throws InterruptedException {
            if (this.records.isEmpty()) {
                (new CountDownLatch(1)).await();
            }

            if (!this.running.get()) {
                return null;
            } else {
                List<SourceRecord> results = new ArrayList();

                for (int record = 0; record < this.recordsPerBatch && record < this.records.size(); ++record) {
                    SourceRecord fetchedRecord = (SourceRecord) this.records.get(record);
                    Integer id = ((Struct) fetchedRecord.key()).getInt32("id");
                    if (id == this.errorOnRecord && !isThrownErrorOnRecord) {
                        isThrownErrorOnRecord = true;
                        throw new RetriableException("Error on record " + this.errorOnRecord);
                    }

                    results.add(fetchedRecord);
                }

                this.records.removeAll(results);
                return results;
            }
        }

        public void stop() {
            this.running.set(false);
        }
    }
}
