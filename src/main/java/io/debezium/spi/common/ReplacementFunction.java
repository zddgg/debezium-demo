package io.debezium.spi.common;

import io.debezium.common.annotation.Incubating;

@Incubating
public interface ReplacementFunction {
    String REPLACEMENT_CHAR = "_";
    ReplacementFunction UNDERSCORE_REPLACEMENT = (c) -> {
        return "_";
    };

    String replace(char var1);

    default boolean isValidFirstCharacter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }

    default boolean isValidNonFirstCharacter(char c) {
        return this.isValidFirstCharacter(c) || c == '.' || c >= '0' && c <= '9';
    }
}
