package io.debezium.pipeline.source.spi;

import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;
import java.util.Map;

public interface EventMetadataProvider {
    Instant getEventTimestamp(DataCollectionId var1, OffsetContext var2, Object var3, Struct var4);

    Map<String, String> getEventSourcePosition(DataCollectionId var1, OffsetContext var2, Object var3, Struct var4);

    String getTransactionId(DataCollectionId var1, OffsetContext var2, Object var3, Struct var4);

    default String toSummaryString(DataCollectionId source, OffsetContext offset, Object key, Struct value) {
        return (new EventFormatter()).sourcePosition(this.getEventSourcePosition(source, offset, key, value)).key(key).toString();
    }
}
