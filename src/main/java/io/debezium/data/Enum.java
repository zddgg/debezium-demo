package io.debezium.data;

import io.debezium.schema.SchemaFactory;
import io.debezium.util.Strings;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.List;

public class Enum {
    public static final String LOGICAL_NAME = "io.debezium.data.Enum";
    public static final String VALUES_FIELD = "allowed";
    public static final int SCHEMA_VERSION = 1;

    public static SchemaBuilder builder(String allowedValues) {
        return SchemaFactory.get().datatypeEnumSchema(allowedValues);
    }

    public static SchemaBuilder builder(List<String> allowedValues) {
        return allowedValues == null ? builder("") : builder(Strings.join(",", (Iterable) allowedValues));
    }

    public static Schema schema(String allowedValues) {
        return builder(allowedValues).build();
    }

    public static Schema schema(List<String> allowedValues) {
        return allowedValues == null ? builder("").build() : builder(Strings.join(",", (Iterable) allowedValues)).build();
    }
}
