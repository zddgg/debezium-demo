package io.debezium.engine.format;

public interface KeyValueChangeEventFormat<K extends SerializationFormat<?>, V extends SerializationFormat<?>> {
    static <K extends SerializationFormat<?>, V extends SerializationFormat<?>> KeyValueChangeEventFormat<K, V> of(final Class<K> keyFormat, final Class<V> valueFormat) {
        return new KeyValueChangeEventFormat<K, V>() {
            public Class<K> getKeyFormat() {
                return keyFormat;
            }

            public Class<V> getValueFormat() {
                return valueFormat;
            }
        };
    }

    Class<V> getValueFormat();

    Class<K> getKeyFormat();
}
