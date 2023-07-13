package io.debezium.data;

import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.BitSet;

public class Bits {
    public static final String LOGICAL_NAME = "io.debezium.data.Bits";
    public static final String LENGTH_FIELD = "length";
    public static final int SCHEMA_VERSION = 1;

    public static SchemaBuilder builder(int length) {
        return SchemaFactory.get().datatypeBitsSchema(length);
    }

    public static Schema schema(int length) {
        return builder(length).build();
    }

    public static byte[] fromBitSet(Schema schema, BitSet value) {
        return value.toByteArray();
    }

    public static BitSet toBitSet(Schema schema, byte[] value) {
        return BitSet.valueOf(value);
    }
}
