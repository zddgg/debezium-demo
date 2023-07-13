package io.debezium.relational;

import java.util.Optional;

@FunctionalInterface
public interface DefaultValueConverter {
    Optional<Object> parseDefaultValue(Column var1, String var2);

    static DefaultValueConverter passthrough() {
        return (column, defaultValueExpression) -> {
            return Optional.ofNullable(defaultValueExpression);
        };
    }

    @FunctionalInterface
    public interface DefaultValueMapper {
        Object parse(Column var1, String var2) throws Exception;
    }
}
