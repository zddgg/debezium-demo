package io.debezium.relational.mapping;

import io.debezium.relational.Column;
import io.debezium.relational.ValueConverter;
import io.debezium.util.Strings;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.Locale;

public class PropagateSourceTypeToSchemaParameter implements ColumnMapper {
    private static final String TYPE_NAME_PARAMETER_KEY = "__debezium.source.column.type";
    private static final String TYPE_LENGTH_PARAMETER_KEY = "__debezium.source.column.length";
    private static final String TYPE_SCALE_PARAMETER_KEY = "__debezium.source.column.scale";
    private static final String COLUMN_COMMENT_PARAMETER_KEY = "__debezium.source.column.comment";

    public ValueConverter create(Column column) {
        return null;
    }

    public void alterFieldSchema(Column column, SchemaBuilder schemaBuilder) {
        schemaBuilder.parameter("__debezium.source.column.type", column.typeName().toUpperCase(Locale.ENGLISH));
        if (column.length() != -1) {
            schemaBuilder.parameter("__debezium.source.column.length", String.valueOf(column.length()));
        }

        if (column.scale().isPresent()) {
            schemaBuilder.parameter("__debezium.source.column.scale", String.valueOf(column.scale().get()));
        }

        if (!Strings.isNullOrEmpty(column.comment())) {
            schemaBuilder.parameter("__debezium.source.column.comment", column.comment());
        }

    }
}
