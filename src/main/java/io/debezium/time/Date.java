package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;

public class Date {
    public static final String SCHEMA_NAME = "io.debezium.time.Date";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int32().name("io.debezium.time.Date").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static int toEpochDay(Object value, TemporalAdjuster adjuster) {
        LocalDate date = Conversions.toLocalDate(value);
        if (adjuster != null) {
            date = date.with(adjuster);
        }

        return (int) date.toEpochDay();
    }

    private Date() {
    }
}
