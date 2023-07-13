package io.debezium.heartbeat;

import io.debezium.config.Field;
import io.debezium.function.BlockingConsumer;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.Map;

public interface Heartbeat extends AutoCloseable {
    String HEARTBEAT_INTERVAL_PROPERTY_NAME = "heartbeat.interval.ms";
    Field HEARTBEAT_INTERVAL = Field.create("heartbeat.interval.ms").withDisplayName("Connector heartbeat interval (milli-seconds)").withType(Type.INT).withGroup(Field.createGroupEntry(Field.Group.ADVANCED_HEARTBEAT, 0)).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Length of an interval in milli-seconds in in which the connector periodically sends heartbeat messages to a heartbeat topic. Use 0 to disable heartbeat messages. Disabled by default.").withDefault(0).withValidation(Field::isNonNegativeInteger);
    Field HEARTBEAT_TOPICS_PREFIX = Field.create("heartbeat.topics.prefix").withDisplayName("A prefix used for naming of heartbeat topics").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.ADVANCED_HEARTBEAT, 1)).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("The prefix that is used to name heartbeat topics.Defaults to __debezium-heartbeat.").withDefault("__debezium-heartbeat");
    Heartbeat DEFAULT_NOOP_HEARTBEAT = new Heartbeat() {
        public void heartbeat(Map<String, ?> partition, Map<String, ?> offset, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        }

        public void forcedBeat(Map<String, ?> partition, Map<String, ?> offset, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        }

        public void heartbeat(Map<String, ?> partition, OffsetProducer offsetProducer, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        }

        public boolean isEnabled() {
            return false;
        }
    };

    void heartbeat(Map<String, ?> var1, Map<String, ?> var2, BlockingConsumer<SourceRecord> var3) throws InterruptedException;

    void heartbeat(Map<String, ?> var1, OffsetProducer var2, BlockingConsumer<SourceRecord> var3) throws InterruptedException;

    void forcedBeat(Map<String, ?> var1, Map<String, ?> var2, BlockingConsumer<SourceRecord> var3) throws InterruptedException;

    boolean isEnabled();

    default void close() {
    }

    @FunctionalInterface
    public interface OffsetProducer {
        Map<String, ?> offset();
    }
}
