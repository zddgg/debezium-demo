package io.debezium.relational;

import org.apache.kafka.connect.data.Struct;

@FunctionalInterface
public interface StructGenerator {
    Struct generateValue(Object[] var1);
}
