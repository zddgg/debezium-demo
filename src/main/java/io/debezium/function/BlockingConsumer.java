package io.debezium.function;

@FunctionalInterface
public interface BlockingConsumer<T> {
    void accept(T var1) throws InterruptedException;
}
