package io.debezium.jdbc;

import io.debezium.annotation.Immutable;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.data.Bits;
import io.debezium.data.SpecialValueDecimal;
import io.debezium.data.Xml;
import io.debezium.relational.Column;
import io.debezium.relational.ValueConverter;
import io.debezium.relational.ValueConverterProvider;
import io.debezium.time.*;
import io.debezium.util.HexConverter;
import io.debezium.util.NumberConversions;
import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.util.Base64;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;

@Immutable
public class JdbcValueConverters implements ValueConverterProvider {
    protected final Logger logger;
    protected final ZoneOffset defaultOffset;
    protected final String fallbackTimestampWithTimeZone;
    private final String fallbackTimeWithTimeZone;
    protected final boolean adaptiveTimePrecisionMode;
    protected final boolean adaptiveTimeMicrosecondsPrecisionMode;
    protected final DecimalMode decimalMode;
    protected final TemporalAdjuster adjuster;
    protected final BigIntUnsignedMode bigIntUnsignedMode;
    protected final CommonConnectorConfig.BinaryHandlingMode binaryMode;

    public JdbcValueConverters() {
        this((DecimalMode) null, TemporalPrecisionMode.ADAPTIVE, ZoneOffset.UTC, (TemporalAdjuster) null, (BigIntUnsignedMode) null, (CommonConnectorConfig.BinaryHandlingMode) null);
    }

    public JdbcValueConverters(DecimalMode decimalMode, TemporalPrecisionMode temporalPrecisionMode, ZoneOffset defaultOffset, TemporalAdjuster adjuster, BigIntUnsignedMode bigIntUnsignedMode, CommonConnectorConfig.BinaryHandlingMode binaryMode) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.defaultOffset = defaultOffset != null ? defaultOffset : ZoneOffset.UTC;
        this.adaptiveTimePrecisionMode = temporalPrecisionMode.equals(TemporalPrecisionMode.ADAPTIVE);
        this.adaptiveTimeMicrosecondsPrecisionMode = temporalPrecisionMode.equals(TemporalPrecisionMode.ADAPTIVE_TIME_MICROSECONDS);
        this.decimalMode = decimalMode != null ? decimalMode : DecimalMode.PRECISE;
        this.adjuster = adjuster;
        this.bigIntUnsignedMode = bigIntUnsignedMode != null ? bigIntUnsignedMode : BigIntUnsignedMode.PRECISE;
        this.binaryMode = binaryMode != null ? binaryMode : CommonConnectorConfig.BinaryHandlingMode.BYTES;
        this.fallbackTimestampWithTimeZone = ZonedTimestamp.toIsoString(OffsetDateTime.of(LocalDate.ofEpochDay(0L), LocalTime.MIDNIGHT, defaultOffset), defaultOffset, adjuster, (Integer) null);
        this.fallbackTimeWithTimeZone = ZonedTime.toIsoString((Object) OffsetTime.of(LocalTime.MIDNIGHT, defaultOffset), defaultOffset, adjuster);
    }

    public SchemaBuilder schemaBuilder(Column column) {
        switch (column.jdbcType()) {
            case -16:
            case -15:
            case -9:
            case 1:
            case 2011:
                return SchemaBuilder.string();
            case -8:
                return SchemaBuilder.bytes();
            case -7:
                if (column.length() > 1) {
                    return Bits.builder(column.length());
                }
            case 16:
                return SchemaBuilder.bool();
            case -6:
                return SchemaBuilder.int8();
            case -5:
                return SchemaBuilder.int64();
            case -4:
            case -3:
                return this.binaryMode.getSchema();
            case -2:
            case 2004:
                return this.binaryMode.getSchema();
            case -1:
            case 12:
            case 70:
            case 2005:
                return SchemaBuilder.string();
            case 0:
                this.logger.warn("Unexpected JDBC type: NULL");
                return null;
            case 2:
            case 3:
                return SpecialValueDecimal.builder(this.decimalMode, column.length(), (Integer) column.scale().get());
            case 4:
                return SchemaBuilder.int32();
            case 5:
                return SchemaBuilder.int16();
            case 6:
            case 8:
                return SchemaBuilder.float64();
            case 7:
                return SchemaBuilder.float32();
            case 91:
                if (!this.adaptiveTimePrecisionMode && !this.adaptiveTimeMicrosecondsPrecisionMode) {
                    return Date.builder();
                }

                return io.debezium.time.Date.builder();
            case 92:
                if (this.adaptiveTimeMicrosecondsPrecisionMode) {
                    return MicroTime.builder();
                } else {
                    if (this.adaptiveTimePrecisionMode) {
                        if (this.getTimePrecision(column) <= 3) {
                            return Time.builder();
                        }

                        if (this.getTimePrecision(column) <= 6) {
                            return MicroTime.builder();
                        }

                        return NanoTime.builder();
                    }

                    return org.apache.kafka.connect.data.Time.builder();
                }
            case 93:
                if (!this.adaptiveTimePrecisionMode && !this.adaptiveTimeMicrosecondsPrecisionMode) {
                    return Timestamp.builder();
                } else if (this.getTimePrecision(column) <= 3) {
                    return io.debezium.time.Timestamp.builder();
                } else {
                    if (this.getTimePrecision(column) <= 6) {
                        return MicroTimestamp.builder();
                    }

                    return NanoTimestamp.builder();
                }
            case 1111:
            case 2000:
            case 2001:
            case 2002:
            case 2003:
            case 2006:
            case 2012:
            default:
                return null;
            case 2009:
                return Xml.builder();
            case 2013:
                return ZonedTime.builder();
            case 2014:
                return ZonedTimestamp.builder();
        }
    }

    public ValueConverter converter(Column column, Field fieldDefn) {
        switch (column.jdbcType()) {
            case -16:
            case -15:
            case -9:
            case -1:
            case 1:
            case 12:
            case 70:
            case 2005:
            case 2009:
            case 2011:
                return (data) -> {
                    return this.convertString(column, fieldDefn, data);
                };
            case -8:
                return (data) -> {
                    return this.convertRowId(column, fieldDefn, data);
                };
            case -7:
                return this.convertBits(column, fieldDefn);
            case -6:
                return (data) -> {
                    return this.convertTinyInt(column, fieldDefn, data);
                };
            case -5:
                return (data) -> {
                    return this.convertBigInt(column, fieldDefn, data);
                };
            case -4:
            case -3:
            case -2:
            case 2004:
                return (data) -> {
                    return this.convertBinary(column, fieldDefn, data, this.binaryMode);
                };
            case 0:
                return (data) -> {
                    return null;
                };
            case 2:
                return (data) -> {
                    return this.convertNumeric(column, fieldDefn, data);
                };
            case 3:
                return (data) -> {
                    return this.convertDecimal(column, fieldDefn, data);
                };
            case 4:
                return (data) -> {
                    return this.convertInteger(column, fieldDefn, data);
                };
            case 5:
                return (data) -> {
                    return this.convertSmallInt(column, fieldDefn, data);
                };
            case 6:
                return (data) -> {
                    return this.convertFloat(column, fieldDefn, data);
                };
            case 7:
                return (data) -> {
                    return this.convertReal(column, fieldDefn, data);
                };
            case 8:
                return (data) -> {
                    return this.convertDouble(column, fieldDefn, data);
                };
            case 16:
                return (data) -> {
                    return this.convertBoolean(column, fieldDefn, data);
                };
            case 91:
                if (!this.adaptiveTimePrecisionMode && !this.adaptiveTimeMicrosecondsPrecisionMode) {
                    return (data) -> {
                        return this.convertDateToEpochDaysAsDate(column, fieldDefn, data);
                    };
                }

                return (data) -> {
                    return this.convertDateToEpochDays(column, fieldDefn, data);
                };
            case 92:
                return (data) -> {
                    return this.convertTime(column, fieldDefn, data);
                };
            case 93:
                if (!this.adaptiveTimePrecisionMode && !this.adaptiveTimeMicrosecondsPrecisionMode) {
                    return (data) -> {
                        return this.convertTimestampToEpochMillisAsDate(column, fieldDefn, data);
                    };
                } else if (this.getTimePrecision(column) <= 3) {
                    return (data) -> {
                        return this.convertTimestampToEpochMillis(column, fieldDefn, data);
                    };
                } else {
                    if (this.getTimePrecision(column) <= 6) {
                        return (data) -> {
                            return this.convertTimestampToEpochMicros(column, fieldDefn, data);
                        };
                    }

                    return (data) -> {
                        return this.convertTimestampToEpochNanos(column, fieldDefn, data);
                    };
                }
            case 1111:
            case 2000:
            case 2001:
            case 2002:
            case 2003:
            case 2006:
            case 2012:
            default:
                return null;
            case 2013:
                return (data) -> {
                    return this.convertTimeWithZone(column, fieldDefn, data);
                };
            case 2014:
                return (data) -> {
                    return this.convertTimestampWithZone(column, fieldDefn, data);
                };
        }
    }

    protected ValueConverter convertBits(Column column, Field fieldDefn) {
        if (column.length() > 1) {
            int numBits = column.length();
            int numBytes = numBits / 8 + (numBits % 8 == 0 ? 0 : 1);
            return (data) -> {
                return this.convertBits(column, fieldDefn, data, numBytes);
            };
        } else {
            return (data) -> {
                return this.convertBit(column, fieldDefn, data);
            };
        }
    }

    protected Object convertTimestampWithZone(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, this.fallbackTimestampWithTimeZone, (r) -> {
            try {
                r.deliver(ZonedTimestamp.toIsoString(data, this.defaultOffset, this.adjuster, column.length()));
            } catch (IllegalArgumentException var5) {
            }

        });
    }

    protected Object convertTimeWithZone(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, this.fallbackTimeWithTimeZone, (r) -> {
            try {
                r.deliver(ZonedTime.toIsoString((Object) data, this.defaultOffset, this.adjuster));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTime(Column column, Field fieldDefn, Object data) {
        if (this.adaptiveTimeMicrosecondsPrecisionMode) {
            return this.convertTimeToMicrosPastMidnight(column, fieldDefn, data);
        } else if (this.adaptiveTimePrecisionMode) {
            if (this.getTimePrecision(column) <= 3) {
                return this.convertTimeToMillisPastMidnight(column, fieldDefn, data);
            } else {
                return this.getTimePrecision(column) <= 6 ? this.convertTimeToMicrosPastMidnight(column, fieldDefn, data) : this.convertTimeToNanosPastMidnight(column, fieldDefn, data);
            }
        } else {
            return this.convertTimeToMillisPastMidnightAsDate(column, fieldDefn, data);
        }
    }

    protected Object convertTimestampToEpochMillis(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            try {
                r.deliver(io.debezium.time.Timestamp.toEpochMillis(data, this.adjuster));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimestampToEpochMicros(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            try {
                r.deliver(MicroTimestamp.toEpochMicros(data, this.adjuster));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimestampToEpochNanos(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            try {
                r.deliver(NanoTimestamp.toEpochNanos(data, this.adjuster));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimestampToEpochMillisAsDate(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, new java.util.Date(0L), (r) -> {
            try {
                r.deliver(new java.util.Date(io.debezium.time.Timestamp.toEpochMillis(data, this.adjuster)));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimeToMillisPastMidnight(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0, (r) -> {
            try {
                r.deliver(Time.toMilliOfDay(data, this.supportsLargeTimeValues()));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimeToMicrosPastMidnight(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            try {
                r.deliver(MicroTime.toMicroOfDay(data, this.supportsLargeTimeValues()));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimeToNanosPastMidnight(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            try {
                r.deliver(NanoTime.toNanoOfDay(data, this.supportsLargeTimeValues()));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertTimeToMillisPastMidnightAsDate(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, new java.util.Date(0L), (r) -> {
            try {
                r.deliver(new java.util.Date((long) Time.toMilliOfDay(data, this.supportsLargeTimeValues())));
            } catch (IllegalArgumentException var4) {
            }

        });
    }

    protected Object convertDateToEpochDays(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0, (r) -> {
            try {
                r.deliver(io.debezium.time.Date.toEpochDay(data, this.adjuster));
            } catch (IllegalArgumentException var5) {
                this.logger.warn("Unexpected JDBC DATE value for field {} with schema {}: class={}, value={}", new Object[]{fieldDefn.name(), fieldDefn.schema(), data.getClass(), data});
            }

        });
    }

    protected Object convertDateToEpochDaysAsDate(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, new java.util.Date(0L), (r) -> {
            try {
                int epochDay = io.debezium.time.Date.toEpochDay(data, this.adjuster);
                long epochMillis = TimeUnit.DAYS.toMillis((long) epochDay);
                r.deliver(new java.util.Date(epochMillis));
            } catch (IllegalArgumentException var7) {
                this.logger.warn("Unexpected JDBC DATE value for field {} with schema {}: class={}, value={}", new Object[]{fieldDefn.name(), fieldDefn.schema(), data.getClass(), data});
            }

        });
    }

    protected Object convertBinary(Column column, Field fieldDefn, Object data, CommonConnectorConfig.BinaryHandlingMode mode) {
        switch (mode) {
            case BASE64:
                return this.convertBinaryToBase64(column, fieldDefn, data);
            case BASE64_URL_SAFE:
                return this.convertBinaryToBase64UrlSafe(column, fieldDefn, data);
            case HEX:
                return this.convertBinaryToHex(column, fieldDefn, data);
            case BYTES:
            default:
                return this.convertBinaryToBytes(column, fieldDefn, data);
        }
    }

    protected Object convertBinaryToBytes(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, NumberConversions.BYTE_ZERO, (r) -> {
            if (data instanceof String) {
                r.deliver(this.toByteBuffer((String) data));
            } else if (data instanceof char[]) {
                r.deliver(this.toByteBuffer((char[]) data));
            } else if (data instanceof byte[]) {
                r.deliver(this.toByteBuffer(column, (byte[]) data));
            } else {
                r.deliver(this.unexpectedBinary(data, fieldDefn));
            }

        });
    }

    protected Object convertBinaryToBase64(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, "", (r) -> {
            Base64.Encoder base64Encoder = Base64.getEncoder();
            if (data instanceof String) {
                r.deliver(new String(base64Encoder.encode(((String) data).getBytes(StandardCharsets.UTF_8))));
            } else if (data instanceof char[]) {
                r.deliver(new String(base64Encoder.encode(this.toByteArray((char[]) data)), StandardCharsets.UTF_8));
            } else if (data instanceof byte[]) {
                r.deliver(new String(base64Encoder.encode(this.normalizeBinaryData(column, (byte[]) data)), StandardCharsets.UTF_8));
            } else {
                r.deliver(this.unexpectedBinary(data, fieldDefn));
            }

        });
    }

    protected Object convertBinaryToBase64UrlSafe(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, "", (r) -> {
            Base64.Encoder base64UrlSafeEncoder = Base64.getUrlEncoder();
            if (data instanceof String) {
                r.deliver(new String(base64UrlSafeEncoder.encode(((String) data).getBytes(StandardCharsets.UTF_8))));
            } else if (data instanceof char[]) {
                r.deliver(new String(base64UrlSafeEncoder.encode(this.toByteArray((char[]) data)), StandardCharsets.UTF_8));
            } else if (data instanceof byte[]) {
                r.deliver(new String(base64UrlSafeEncoder.encode(this.normalizeBinaryData(column, (byte[]) data)), StandardCharsets.UTF_8));
            } else {
                r.deliver(this.unexpectedBinary(data, fieldDefn));
            }

        });
    }

    protected Object convertBinaryToHex(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, "", (r) -> {
            if (data instanceof String) {
                r.deliver(HexConverter.convertToHexString(((String) data).getBytes(StandardCharsets.UTF_8)));
            } else if (data instanceof char[]) {
                r.deliver(HexConverter.convertToHexString(this.toByteArray((char[]) data)));
            } else if (data instanceof byte[]) {
                r.deliver(HexConverter.convertToHexString(this.normalizeBinaryData(column, (byte[]) data)));
            } else {
                r.deliver(this.unexpectedBinary(data, fieldDefn));
            }

        });
    }

    protected ByteBuffer toByteBuffer(Column column, byte[] data) {
        return ByteBuffer.wrap(this.normalizeBinaryData(column, data));
    }

    protected byte[] normalizeBinaryData(Column column, byte[] data) {
        return data;
    }

    protected byte[] unexpectedBinary(Object value, Field fieldDefn) {
        this.logger.warn("Unexpected JDBC BINARY value for field {} with schema {}: class={}, value={}", new Object[]{fieldDefn.name(), fieldDefn.schema(), value.getClass(), value});
        return null;
    }

    protected Object convertTinyInt(Column column, Field fieldDefn, Object data) {
        return this.convertSmallInt(column, fieldDefn, data);
    }

    protected Object convertSmallInt(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, NumberConversions.SHORT_FALSE, (r) -> {
            if (data instanceof Short) {
                r.deliver(data);
            } else if (data instanceof Number) {
                Number value = (Number) data;
                r.deliver(value.shortValue());
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getShort((Boolean) data));
            } else if (data instanceof String) {
                r.deliver(Short.valueOf((String) data));
            }

        });
    }

    protected Object convertInteger(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0, (r) -> {
            if (data instanceof Integer) {
                r.deliver(data);
            } else if (data instanceof Number) {
                Number value = (Number) data;
                r.deliver(value.intValue());
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getInteger((Boolean) data));
            } else if (data instanceof String) {
                r.deliver(Integer.valueOf((String) data));
            }

        });
    }

    protected Object convertBigInt(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0L, (r) -> {
            if (data instanceof Long) {
                r.deliver(data);
            } else if (data instanceof Number) {
                Number value = (Number) data;
                r.deliver(value.longValue());
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getLong((Boolean) data));
            } else if (data instanceof String) {
                r.deliver(Long.valueOf((String) data));
            }

        });
    }

    protected Object convertFloat(Column column, Field fieldDefn, Object data) {
        return this.convertDouble(column, fieldDefn, data);
    }

    protected Object convertDouble(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0.0, (r) -> {
            if (data instanceof Double) {
                r.deliver(data);
            } else if (data instanceof Number) {
                Number value = (Number) data;
                r.deliver(value.doubleValue());
            } else if (data instanceof SpecialValueDecimal) {
                r.deliver(((SpecialValueDecimal) data).toDouble());
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getDouble((Boolean) data));
            }

        });
    }

    protected Object convertReal(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, 0.0F, (r) -> {
            if (data instanceof Float) {
                r.deliver(data);
            } else if (data instanceof Number) {
                Number value = (Number) data;
                r.deliver(value.floatValue());
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getFloat((Boolean) data));
            }

        });
    }

    protected Object convertNumeric(Column column, Field fieldDefn, Object data) {
        return this.convertDecimal(column, fieldDefn, data);
    }

    protected Object convertDecimal(Column column, Field fieldDefn, Object data) {
        if (data instanceof SpecialValueDecimal) {
            return SpecialValueDecimal.fromLogical((SpecialValueDecimal) data, this.decimalMode, column.name());
        } else {
            Object decimal = this.toBigDecimal(column, fieldDefn, data);
            return decimal instanceof BigDecimal ? SpecialValueDecimal.fromLogical(new SpecialValueDecimal((BigDecimal) decimal), this.decimalMode, column.name()) : decimal;
        }
    }

    protected Object toBigDecimal(Column column, Field fieldDefn, Object data) {
        BigDecimal fallback = this.withScaleAdjustedIfNeeded(column, BigDecimal.ZERO);
        return this.convertValue(column, fieldDefn, data, fallback, (r) -> {
            if (data instanceof BigDecimal) {
                r.deliver(data);
            } else if (data instanceof Boolean) {
                r.deliver(NumberConversions.getBigDecimal((Boolean) data));
            } else if (data instanceof Short) {
                r.deliver(new BigDecimal(((Short) data).intValue()));
            } else if (data instanceof Integer) {
                r.deliver(new BigDecimal((Integer) data));
            } else if (data instanceof Long) {
                r.deliver(BigDecimal.valueOf((Long) data));
            } else if (data instanceof Float) {
                r.deliver(BigDecimal.valueOf(((Float) data).doubleValue()));
            } else if (data instanceof Double) {
                r.deliver(BigDecimal.valueOf((Double) data));
            } else if (data instanceof String) {
                r.deliver(new BigDecimal((String) data));
            }

        });
    }

    protected BigDecimal withScaleAdjustedIfNeeded(Column column, BigDecimal data) {
        if (column.scale().isPresent() && (Integer) column.scale().get() > data.scale()) {
            data = data.setScale((Integer) column.scale().get());
        }

        return data;
    }

    protected Object convertString(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, "", (r) -> {
            if (data instanceof SQLXML) {
                try {
                    r.deliver(((SQLXML) data).getString());
                } catch (SQLException var4) {
                    throw new RuntimeException("Error processing data from " + column.jdbcType() + " and column " + column + ": class=" + data.getClass(), var4);
                }
            } else {
                r.deliver(data.toString());
            }

        });
    }

    protected Object convertRowId(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, NumberConversions.BYTE_BUFFER_ZERO, (r) -> {
            if (data instanceof RowId) {
                RowId row = (RowId) data;
                r.deliver(ByteBuffer.wrap(row.getBytes()));
            }

        });
    }

    protected Object convertBit(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, false, (r) -> {
            if (data instanceof Boolean) {
                r.deliver(data);
            } else if (data instanceof Short) {
                r.deliver(((Short) data).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
            } else if (data instanceof Integer) {
                r.deliver((Integer) data == 0 ? Boolean.FALSE : Boolean.TRUE);
            } else if (data instanceof Long) {
                r.deliver(((Long) data).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
            } else if (data instanceof BitSet) {
                BitSet value = (BitSet) data;
                r.deliver(value.get(0));
            }

        });
    }

    protected Object convertBits(Column column, Field fieldDefn, Object data, int numBytes) {
        return this.convertValue(column, fieldDefn, data, new byte[0], (r) -> {
            if (data instanceof Boolean) {
                Boolean value = (Boolean) data;
                r.deliver(new byte[]{(byte) (value ? 1 : 0)});
            } else {
                byte[] bytes;
                if (data instanceof byte[]) {
                    bytes = (byte[]) data;
                    if (bytes.length == 1) {
                        r.deliver(bytes);
                    }

                    if (this.byteOrderOfBitType() == ByteOrder.BIG_ENDIAN) {
                        int i = 0;

                        for (int j = bytes.length - 1; j > i; --j) {
                            byte tmp = bytes[j];
                            bytes[j] = bytes[i];
                            bytes[i] = tmp;
                            ++i;
                        }
                    }

                    r.deliver(this.padLittleEndian(numBytes, bytes));
                } else if (data instanceof BitSet) {
                    bytes = ((BitSet) data).toByteArray();
                    r.deliver(this.padLittleEndian(numBytes, bytes));
                }
            }

        });
    }

    protected byte[] padLittleEndian(int numBytes, byte[] data) {
        if (data.length < numBytes) {
            byte[] padded = new byte[numBytes];
            System.arraycopy(data, 0, padded, 0, data.length);
            return padded;
        } else {
            return data;
        }
    }

    protected ByteOrder byteOrderOfBitType() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    protected Object convertBoolean(Column column, Field fieldDefn, Object data) {
        return this.convertValue(column, fieldDefn, data, false, (r) -> {
            if (data instanceof Boolean) {
                r.deliver(data);
            } else if (data instanceof Short) {
                r.deliver(((Short) data).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
            } else if (data instanceof Integer) {
                r.deliver((Integer) data == 0 ? Boolean.FALSE : Boolean.TRUE);
            } else if (data instanceof Long) {
                r.deliver(((Long) data).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE);
            }

        });
    }

    protected Object handleUnknownData(Column column, Field fieldDefn, Object data) {
        Class<?> dataClass = data.getClass();
        String clazzName = dataClass.isArray() ? dataClass.getSimpleName() : dataClass.getName();
        if (!column.isOptional() && !fieldDefn.schema().isOptional()) {
            throw new IllegalArgumentException("Unexpected value for JDBC type " + column.jdbcType() + " and column " + column + ": class=" + clazzName);
        } else {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Unexpected value for JDBC type {} and column {}: class={}", new Object[]{column.jdbcType(), column, clazzName});
            }

            return null;
        }
    }

    protected int getTimePrecision(Column column) {
        return column.length();
    }

    protected Object convertValue(Column column, Field fieldDefn, Object data, Object fallback, ValueConversionCallback callback) {
        if (data == null) {
            if (column.isOptional()) {
                return null;
            } else {
                Object schemaDefault = fieldDefn.schema().defaultValue();
                return schemaDefault != null ? schemaDefault : fallback;
            }
        } else {
            this.logger.trace("Value from data object: *** {} ***", data);
            ResultReceiver r = ResultReceiver.create();
            callback.convert(r);
            this.logger.trace("Callback is: {}", callback);
            this.logger.trace("Value from ResultReceiver: {}", r);
            return r.hasReceived() ? r.get() : this.handleUnknownData(column, fieldDefn, data);
        }
    }

    private boolean supportsLargeTimeValues() {
        return this.adaptiveTimePrecisionMode || this.adaptiveTimeMicrosecondsPrecisionMode;
    }

    private byte[] toByteArray(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        return byteBuffer.array();
    }

    private ByteBuffer toByteBuffer(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        return Charset.forName("UTF-8").encode(charBuffer);
    }

    private ByteBuffer toByteBuffer(String string) {
        return ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
    }

    public static enum DecimalMode {
        PRECISE,
        DOUBLE,
        STRING;

        // $FF: synthetic method
        private static DecimalMode[] $values() {
            return new DecimalMode[]{PRECISE, DOUBLE, STRING};
        }
    }

    public static enum BigIntUnsignedMode {
        PRECISE,
        LONG;

        // $FF: synthetic method
        private static BigIntUnsignedMode[] $values() {
            return new BigIntUnsignedMode[]{PRECISE, LONG};
        }
    }
}
