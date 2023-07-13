package io.debezium.embedded;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import io.debezium.engine.format.KeyValueChangeEventFormat;
import io.debezium.engine.format.KeyValueHeaderChangeEventFormat;
import io.debezium.engine.format.SerializationFormat;

public class ConvertingEngineBuilderFactory implements DebeziumEngine.BuilderFactory {
    public <T, V extends SerializationFormat<T>> DebeziumEngine.Builder<RecordChangeEvent<T>> builder(ChangeEventFormat<V> format) {
        return new ConvertingEngineBuilder(format);
    }

    public <S, T, K extends SerializationFormat<S>, V extends SerializationFormat<T>> DebeziumEngine.Builder<ChangeEvent<S, T>> builder(KeyValueChangeEventFormat<K, V> format) {
        return new ConvertingEngineBuilder(format);
    }

    public <S, T, U, K extends SerializationFormat<S>, V extends SerializationFormat<T>, H extends SerializationFormat<U>> DebeziumEngine.Builder<ChangeEvent<S, T>> builder(KeyValueHeaderChangeEventFormat<K, V, H> format) {
        return new ConvertingEngineBuilder(format);
    }
}
