package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;

public class NanoTimestamp {
    public static final String SCHEMA_NAME = "io.debezium.time.NanoTimestamp";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.NanoTimestamp").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static long toEpochNanos(Object value, TemporalAdjuster adjuster) {
        LocalDateTime dateTime = Conversions.toLocalDateTime(value);
        if (adjuster != null) {
            dateTime = dateTime.with(adjuster);
        }

        return toEpochNanos(dateTime);
    }

    private static long toEpochNanos(LocalDateTime timestamp) {
        long nanoInDay = timestamp.toLocalTime().toNanoOfDay();
        long nanosOfDay = toEpochNanos(timestamp.toLocalDate());
        return nanosOfDay + nanoInDay;
    }

    private static long toEpochNanos(LocalDate date) {
        long epochDay = date.toEpochDay();
        return epochDay * Conversions.NANOSECONDS_PER_DAY;
    }

    private NanoTimestamp() {
    }
}
