package io.debezium.spi.converter;

import io.debezium.common.annotation.Incubating;

import java.util.OptionalInt;

@Incubating
public interface RelationalColumn extends ConvertedField {
    int jdbcType();

    int nativeType();

    String typeName();

    String typeExpression();

    OptionalInt length();

    OptionalInt scale();

    boolean isOptional();

    Object defaultValue();

    boolean hasDefaultValue();

    default String charsetName() {
        return null;
    }
}
