package io.debezium.connector.mysql;

import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Collect;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;
import java.util.Map;

class MySqlEventMetadataProvider implements EventMetadataProvider {
    public Instant getEventTimestamp(DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        if (value == null) {
            return null;
        } else {
            Struct sourceInfo = value.getStruct("source");
            if (source == null) {
                return null;
            } else {
                Long timestamp = sourceInfo.getInt64("ts_ms");
                return timestamp == null ? null : Instant.ofEpochMilli(timestamp);
            }
        }
    }

    public Map<String, String> getEventSourcePosition(DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        if (value == null) {
            return null;
        } else {
            Struct sourceInfo = value.getStruct("source");
            return source == null ? null : Collect.hashMapOf("file", sourceInfo.getString("file"), "pos", Long.toString(sourceInfo.getInt64("pos")), "row", Integer.toString(sourceInfo.getInt32("row")));
        }
    }

    public String getTransactionId(DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        return ((MySqlOffsetContext) offset).getTransactionId();
    }
}
