package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.temporal.ChronoUnit;

public class MicroDuration {
    public static final double DAYS_PER_MONTH_AVG = 30.4375;
    public static final String SCHEMA_NAME = "io.debezium.time.MicroDuration";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int64().name("io.debezium.time.MicroDuration").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    private MicroDuration() {
    }

    public static long durationMicros(int years, int months, int days, int hours, int minutes, double seconds, int micros, Double daysPerMonthAvg) {
        if (daysPerMonthAvg == null) {
            daysPerMonthAvg = (double) ChronoUnit.MONTHS.getDuration().toDays();
        }

        double numberOfDays = (double) (years * 12 + months) * daysPerMonthAvg + (double) days;
        double numberOfSeconds = ((numberOfDays * 24.0 + (double) hours) * 60.0 + (double) minutes) * 60.0 + seconds;
        return (long) (numberOfSeconds * 1000000.0 + (double) micros);
    }

    public static long durationMicros(int years, int months, int days, int hours, int minutes, double seconds, Double daysPerMonthAvg) {
        return durationMicros(years, months, days, hours, minutes, seconds, 0, daysPerMonthAvg);
    }
}
