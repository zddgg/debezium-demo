package io.debezium.data.geometry;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class Geography extends Geometry {
    public static final String LOGICAL_NAME = "io.debezium.data.geometry.Geography";

    public static SchemaBuilder builder() {
        return SchemaBuilder.struct().name("io.debezium.data.geometry.Geography").version(1).doc("Geography").field("wkb", Schema.BYTES_SCHEMA).field("srid", Schema.OPTIONAL_INT32_SCHEMA);
    }
}
