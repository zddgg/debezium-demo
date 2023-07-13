package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.spi.common.ReplacementFunction;

@Incubating
public class UnicodeReplacementFunction implements ReplacementFunction {
    public String replace(char invalid) {
        String hex = Integer.toHexString(invalid);
        if (hex.length() <= 2) {
            hex = "00" + hex;
        }

        return "_u" + hex;
    }

    public boolean isValidFirstCharacter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }
}
