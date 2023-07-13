package io.debezium.engine.format;

public interface ChangeEventFormat<V extends SerializationFormat<?>> {
    static <V extends SerializationFormat<?>> ChangeEventFormat<V> of(Class<V> format) {
        return () -> {
            return format;
        };
    }

    Class<V> getValueFormat();
}
