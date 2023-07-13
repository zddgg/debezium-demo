package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;

public class Timestamp {
    public static final String SCHEMA_NAME = "io.debezium.time.Timestamp";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.Timestamp").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static long toEpochMillis(Object value, TemporalAdjuster adjuster) {
        if (value instanceof Long) {
            return (Long) value;
        } else {
            LocalDateTime dateTime = Conversions.toLocalDateTime(value);
            if (adjuster != null) {
                dateTime = dateTime.with(adjuster);
            }

            return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    }

    private Timestamp() {
    }
}
