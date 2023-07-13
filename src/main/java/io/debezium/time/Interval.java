package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Interval {
    public static final String SCHEMA_NAME = "io.debezium.time.Interval";

    public static SchemaBuilder builder() {
        return SchemaBuilder.string().name("io.debezium.time.Interval").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    private Interval() {
    }

    public static String toIsoString(int years, int months, int days, int hours, int minutes, BigDecimal seconds) {
        if (seconds.scale() > 9) {
            seconds = seconds.setScale(9, RoundingMode.DOWN);
        }

        return "P" + years + "Y" + months + "M" + days + "DT" + hours + "H" + minutes + "M" + seconds.stripTrailingZeros().toPlainString() + "S";
    }
}
