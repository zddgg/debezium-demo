package io.debezium.relational;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.SchemaBuilder;

public interface ValueConverterProvider {
    SchemaBuilder schemaBuilder(Column var1);

    ValueConverter converter(Column var1, Field var2);
}
