package io.debezium.relational;

@FunctionalInterface
public interface ValueConverter {
    Object convert(Object var1);

    default ValueConverter or(ValueConverter fallback) {
        return fallback == null ? this : (data) -> {
            Object result = this.convert(data);
            return result == null && data != null ? fallback.convert(data) : result;
        };
    }

    default ValueConverter and(ValueConverter delegate) {
        return delegate == null ? this : (data) -> {
            return delegate.convert(this.convert(data));
        };
    }

    default ValueConverter nullOr() {
        return (data) -> {
            return data == null ? null : this.convert(data);
        };
    }

    static ValueConverter passthrough() {
        return (data) -> {
            return data;
        };
    }
}
