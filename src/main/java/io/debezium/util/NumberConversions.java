package io.debezium.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

public class NumberConversions {
    public static final Byte BYTE_TRUE = 1;
    public static final Byte BYTE_FALSE = 0;
    public static final Short SHORT_TRUE = Short.valueOf((short) 1);
    public static final Short SHORT_FALSE = Short.valueOf((short) 0);
    public static final Integer INTEGER_TRUE = 1;
    public static final Integer INTEGER_FALSE = 0;
    public static final Long LONG_TRUE = 1L;
    public static final Long LONG_FALSE = 0L;
    public static final Float FLOAT_TRUE = 1.0F;
    public static final Float FLOAT_FALSE = 0.0F;
    public static final Double DOUBLE_TRUE = 1.0;
    public static final Double DOUBLE_FALSE = 0.0;
    public static final byte[] BYTE_ZERO = new byte[0];
    public static final ByteBuffer BYTE_BUFFER_ZERO = ByteBuffer.wrap(new byte[0]);

    public static BigDecimal getBigDecimal(Boolean data) {
        return data ? BigDecimal.ONE : BigDecimal.ZERO;
    }

    public static Byte getByte(boolean data) {
        return data ? BYTE_TRUE : BYTE_FALSE;
    }

    public static Short getShort(Boolean data) {
        return data ? SHORT_TRUE : SHORT_FALSE;
    }

    public static Integer getInteger(Boolean data) {
        return data ? INTEGER_TRUE : INTEGER_FALSE;
    }

    public static Long getLong(Boolean data) {
        return data ? LONG_TRUE : LONG_FALSE;
    }

    public static Float getFloat(Boolean data) {
        return data ? FLOAT_TRUE : FLOAT_FALSE;
    }

    public static Double getDouble(Boolean data) {
        return data ? DOUBLE_TRUE : DOUBLE_FALSE;
    }
}
