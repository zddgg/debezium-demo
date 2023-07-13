package io.debezium.function;

import java.util.Objects;

@FunctionalInterface
public interface BlockingFunction<T, R> {
    R apply(T var1) throws InterruptedException;

    default <V> BlockingFunction<T, V> andThen(BlockingFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t) -> {
            return after.apply(this.apply(t));
        };
    }

    static <T> BlockingFunction<T, T> identity() {
        return (t) -> {
            return t;
        };
    }
}
