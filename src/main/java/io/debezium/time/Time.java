package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.Duration;
import java.time.LocalTime;

public class Time {
    public static final String SCHEMA_NAME = "io.debezium.time.Time";
    private static final Duration ONE_DAY = Duration.ofDays(1L);

    public static SchemaBuilder builder() {
        return SchemaBuilder.int32().name("io.debezium.time.Time").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static int toMilliOfDay(Object value, boolean acceptLargeValues) {
        if (!(value instanceof Duration)) {
            LocalTime time = Conversions.toLocalTime(value);
            long micros = Math.floorDiv(time.toNanoOfDay(), Conversions.NANOSECONDS_PER_MILLISECOND);

            assert Math.abs(micros) < 2147483647L;

            return (int) micros;
        } else {
            Duration duration = (Duration) value;
            if (acceptLargeValues || !duration.isNegative() && duration.compareTo(ONE_DAY) <= 0) {
                return (int) ((Duration) value).toMillis();
            } else {
                throw new IllegalArgumentException("Time values must be between 00:00:00 and 24:00:00 (inclusive): " + duration);
            }
        }
    }

    private Time() {
    }
}
