package io.debezium.schema;

import io.debezium.spi.common.ReplacementFunction;

public class FieldNameUnderscoreReplacementFunction implements ReplacementFunction {
    public String replace(char invalid) {
        return "_";
    }

    public boolean isValidNonFirstCharacter(char c) {
        return this.isValidFirstCharacter(c) || c >= '0' && c <= '9';
    }
}
