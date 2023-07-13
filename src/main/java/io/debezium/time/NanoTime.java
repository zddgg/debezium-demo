package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.Duration;
import java.time.LocalTime;

public class NanoTime {
    public static final String SCHEMA_NAME = "io.debezium.time.NanoTime";
    private static final Duration ONE_DAY = Duration.ofDays(1L);

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.NanoTime").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static long toNanoOfDay(Object value, boolean acceptLargeValues) {
        if (!(value instanceof Duration)) {
            LocalTime time = Conversions.toLocalTime(value);
            return time.toNanoOfDay();
        } else {
            Duration duration = (Duration) value;
            if (acceptLargeValues || !duration.isNegative() && duration.compareTo(ONE_DAY) <= 0) {
                return ((Duration) value).toNanos();
            } else {
                throw new IllegalArgumentException("Time values must be between 00:00:00 and 24:00:00 (inclusive): " + duration);
            }
        }
    }

    private NanoTime() {
    }
}
