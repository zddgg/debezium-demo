package io.debezium.engine;

public interface RecordChangeEvent<V> {
    V record();
}
