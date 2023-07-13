package io.debezium.jdbc;

@FunctionalInterface
public interface ValueConversionCallback {
    void convert(ResultReceiver var1);
}
