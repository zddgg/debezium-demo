package io.debezium.engine;

public interface Header<T> {
    String getKey();

    T getValue();
}
