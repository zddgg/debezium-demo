package io.debezium.data;

public interface ValueWrapper<T> {
    T getWrappedValue();
}
