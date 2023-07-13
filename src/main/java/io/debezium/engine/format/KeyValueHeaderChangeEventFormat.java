package io.debezium.engine.format;

public interface KeyValueHeaderChangeEventFormat<K extends SerializationFormat<?>, V extends SerializationFormat<?>, H extends SerializationFormat<?>> extends KeyValueChangeEventFormat<K, V> {
    static <K extends SerializationFormat<?>, V extends SerializationFormat<?>, H extends SerializationFormat<?>> KeyValueHeaderChangeEventFormat<K, V, H> of(final Class<K> keyFormat, final Class<V> valueFormat, final Class<H> headerFormat) {
        return new KeyValueHeaderChangeEventFormat<K, V, H>() {
            public Class<K> getKeyFormat() {
                return keyFormat;
            }

            public Class<V> getValueFormat() {
                return valueFormat;
            }

            public Class<H> getHeaderFormat() {
                return headerFormat;
            }
        };
    }

    Class<H> getHeaderFormat();
}
