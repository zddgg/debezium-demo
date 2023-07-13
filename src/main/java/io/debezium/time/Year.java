package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class Year {
    public static final String SCHEMA_NAME = "io.debezium.time.Year";

    public static SchemaBuilder builder() {
        return SchemaBuilder.int32().name("io.debezium.time.Year").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    private Year() {
    }
}
