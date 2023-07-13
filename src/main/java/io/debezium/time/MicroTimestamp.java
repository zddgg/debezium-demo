package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;

public class MicroTimestamp {
    public static final String SCHEMA_NAME = "io.debezium.time.MicroTimestamp";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.MicroTimestamp").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static long toEpochMicros(Object value, TemporalAdjuster adjuster) {
        LocalDateTime dateTime = Conversions.toLocalDateTime(value);
        if (adjuster != null) {
            dateTime = dateTime.with(adjuster);
        }

        return Conversions.toEpochMicros(dateTime.toInstant(ZoneOffset.UTC));
    }

    private MicroTimestamp() {
    }
}
