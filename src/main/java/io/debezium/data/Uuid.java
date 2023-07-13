package io.debezium.data;

import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class Uuid {
    public static final String LOGICAL_NAME = "io.debezium.data.Uuid";
    public static final int SCHEMA_VERSION = 1;

    public static SchemaBuilder builder() {
        return SchemaFactory.get().datatypeUuidSchema();
    }

    public static Schema schema() {
        return builder().build();
    }
}
