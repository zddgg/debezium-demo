package io.debezium.schema;

public class FieldNameUnicodeReplacementFunction extends UnicodeReplacementFunction {
    public boolean isValidNonFirstCharacter(char c) {
        return this.isValidFirstCharacter(c) || c >= '0' && c <= '9';
    }
}
