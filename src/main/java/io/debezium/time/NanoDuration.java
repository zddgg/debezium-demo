package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.temporal.ChronoUnit;

public class NanoDuration {
    public static final String SCHEMA_NAME = "io.debezium.time.NanoDuration";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.NanoDuration").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    private NanoDuration() {
    }

    public static long durationNanos(int years, int months, int days, int hours, int minutes, long seconds, long nanos) {
        long daysPerMonthAvg = ChronoUnit.MONTHS.getDuration().toDays();
        long numberOfDays = (long) (years * 12 + months) * daysPerMonthAvg + (long) days;
        long numberOfSeconds = ((numberOfDays * 24L + (long) hours) * 60L + (long) minutes) * 60L + seconds;
        return numberOfSeconds * ChronoUnit.SECONDS.getDuration().toNanos() + nanos;
    }

    public static long durationNanos(int years, int months, int days, int hours, int minutes, long seconds) {
        return durationNanos(years, months, days, hours, minutes, seconds, 0L);
    }
}
