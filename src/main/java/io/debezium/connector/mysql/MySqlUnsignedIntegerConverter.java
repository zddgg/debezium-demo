package io.debezium.connector.mysql;

import java.math.BigDecimal;

public class MySqlUnsignedIntegerConverter {
    private static final short TINYINT_MAX_VALUE = 255;
    private static final int SMALLINT_MAX_VALUE = 65535;
    private static final int MEDIUMINT_MAX_VALUE = 16777215;
    private static final long INT_MAX_VALUE = 4294967295L;
    private static final BigDecimal BIGINT_MAX_VALUE = new BigDecimal("18446744073709551615");
    private static final short TINYINT_CORRECTION = 256;
    private static final int SMALLINT_CORRECTION = 65536;
    private static final int MEDIUMINT_CORRECTION = 16777216;
    private static final long INT_CORRECTION = 4294967296L;
    private static final BigDecimal BIGINT_CORRECTION;

    private MySqlUnsignedIntegerConverter() {
    }

    public static short convertUnsignedTinyint(short originalNumber) {
        return originalNumber < 0 ? (short) (originalNumber + 256) : originalNumber;
    }

    public static int convertUnsignedSmallint(int originalNumber) {
        return originalNumber < 0 ? originalNumber + 65536 : originalNumber;
    }

    public static int convertUnsignedMediumint(int originalNumber) {
        return originalNumber < 0 ? originalNumber + 16777216 : originalNumber;
    }

    public static long convertUnsignedInteger(long originalNumber) {
        return originalNumber < 0L ? originalNumber + 4294967296L : originalNumber;
    }

    public static BigDecimal convertUnsignedBigint(BigDecimal originalNumber) {
        return originalNumber.compareTo(BigDecimal.ZERO) == -1 ? originalNumber.add(BIGINT_CORRECTION) : originalNumber;
    }

    static {
        BIGINT_CORRECTION = BIGINT_MAX_VALUE.add(BigDecimal.ONE);
    }
}
