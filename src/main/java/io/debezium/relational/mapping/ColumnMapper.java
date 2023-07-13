package io.debezium.relational.mapping;

import io.debezium.config.Configuration;
import io.debezium.relational.Column;
import io.debezium.relational.ValueConverter;
import org.apache.kafka.connect.data.SchemaBuilder;

@FunctionalInterface
public interface ColumnMapper {
    default void initialize(Configuration config) {
    }

    ValueConverter create(Column var1);

    default void alterFieldSchema(Column column, SchemaBuilder schemaBuilder) {
    }
}
