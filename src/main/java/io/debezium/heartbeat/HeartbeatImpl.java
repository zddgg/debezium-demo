package io.debezium.heartbeat;

import io.debezium.function.BlockingConsumer;
import io.debezium.schema.SchemaFactory;
import io.debezium.schema.SchemaNameAdjuster;
import io.debezium.util.Clock;
import io.debezium.util.Threads;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class HeartbeatImpl implements Heartbeat {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatImpl.class);
    static final int DEFAULT_HEARTBEAT_INTERVAL = 0;
    static final String DEFAULT_HEARTBEAT_TOPICS_PREFIX = "__debezium-heartbeat";
    public static final String SERVER_NAME_KEY = "serverName";
    private final String topicName;
    private final Duration heartbeatInterval;
    private final String key;
    private final Schema keySchema;
    private final Schema valueSchema;
    private volatile Threads.Timer heartbeatTimeout;

    public HeartbeatImpl(Duration heartbeatInterval, String topicName, String key, SchemaNameAdjuster schemaNameAdjuster) {
        this.topicName = topicName;
        this.key = key;
        this.heartbeatInterval = heartbeatInterval;
        this.keySchema = SchemaFactory.get().heartbeatKeySchema(schemaNameAdjuster);
        this.valueSchema = SchemaFactory.get().heartbeatValueSchema(schemaNameAdjuster);
        this.heartbeatTimeout = this.resetHeartbeat();
    }

    public void heartbeat(Map<String, ?> partition, Map<String, ?> offset, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        if (this.heartbeatTimeout.expired()) {
            this.forcedBeat(partition, offset, consumer);
            this.heartbeatTimeout = this.resetHeartbeat();
        }

    }

    public void heartbeat(Map<String, ?> partition, OffsetProducer offsetProducer, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        if (this.heartbeatTimeout.expired()) {
            this.forcedBeat(partition, offsetProducer.offset(), consumer);
            this.heartbeatTimeout = this.resetHeartbeat();
        }

    }

    public void forcedBeat(Map<String, ?> partition, Map<String, ?> offset, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        LOGGER.debug("Generating heartbeat event");
        if (offset != null && !offset.isEmpty()) {
            consumer.accept(this.heartbeatRecord(partition, offset));
        }
    }

    public boolean isEnabled() {
        return true;
    }

    private Struct serverNameKey(String serverName) {
        Struct result = new Struct(this.keySchema);
        result.put("serverName", serverName);
        return result;
    }

    private Struct messageValue() {
        Struct result = new Struct(this.valueSchema);
        result.put("ts_ms", Instant.now().toEpochMilli());
        return result;
    }

    private SourceRecord heartbeatRecord(Map<String, ?> sourcePartition, Map<String, ?> sourceOffset) {
        Integer partition = 0;
        return new SourceRecord(sourcePartition, sourceOffset, this.topicName, partition, this.keySchema, this.serverNameKey(this.key), this.valueSchema, this.messageValue());
    }

    private Threads.Timer resetHeartbeat() {
        return Threads.timer(Clock.SYSTEM, this.heartbeatInterval);
    }
}
