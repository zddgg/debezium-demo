package io.debezium.jdbc;

import io.debezium.config.EnumeratedValue;

public enum TemporalPrecisionMode implements EnumeratedValue {
    ADAPTIVE("adaptive"),
    ADAPTIVE_TIME_MICROSECONDS("adaptive_time_microseconds"),
    CONNECT("connect");

    private final String value;

    private TemporalPrecisionMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static TemporalPrecisionMode parse(String value) {
        if (value == null) {
            return null;
        } else {
            value = value.trim();
            TemporalPrecisionMode[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                TemporalPrecisionMode option = var1[var3];
                if (option.getValue().equalsIgnoreCase(value)) {
                    return option;
                }
            }

            return null;
        }
    }

    public static TemporalPrecisionMode parse(String value, String defaultValue) {
        TemporalPrecisionMode mode = parse(value);
        if (mode == null && defaultValue != null) {
            mode = parse(defaultValue);
        }

        return mode;
    }

    // $FF: synthetic method
    private static TemporalPrecisionMode[] $values() {
        return new TemporalPrecisionMode[]{ADAPTIVE, ADAPTIVE_TIME_MICROSECONDS, CONNECT};
    }
}
