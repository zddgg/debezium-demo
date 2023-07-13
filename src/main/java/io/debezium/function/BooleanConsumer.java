package io.debezium.function;

import java.util.Objects;

@FunctionalInterface
public interface BooleanConsumer {
    void accept(boolean var1);

    default BooleanConsumer andThen(BooleanConsumer after) {
        Objects.requireNonNull(after);
        return (t) -> {
            this.accept(t);
            after.accept(t);
        };
    }
}
