package io.debezium.document;

import io.debezium.annotation.Immutable;
import io.debezium.util.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

@Immutable
final class ConvertingValue implements Value {
    private final Value value;

    ConvertingValue(Value value) {
        assert value != null;

        this.value = value;
    }

    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object obj) {
        return this.value.equals(obj);
    }

    public String toString() {
        return this.value.toString();
    }

    public int compareTo(Value that) {
        return this.value.compareTo(that);
    }

    public Type getType() {
        return this.value.getType();
    }

    public Object asObject() {
        return this.value.asObject();
    }

    public String asString() {
        return this.value.isNull() ? null : this.value.toString();
    }

    public Boolean asBoolean() {
        if (this.value.isBoolean()) {
            return this.value.asBoolean();
        } else if (this.value.isNumber()) {
            return this.value.asNumber().intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
        } else {
            return this.value.isString() ? Boolean.valueOf(this.asString()) : null;
        }
    }

    public Integer asInteger() {
        if (this.value.isInteger()) {
            return this.value.asInteger();
        } else if (this.value.isNumber()) {
            return this.asNumber().intValue();
        } else {
            if (this.value.isString()) {
                try {
                    return Integer.valueOf(this.asString());
                } catch (NumberFormatException var2) {
                }
            }

            return null;
        }
    }

    public Long asLong() {
        if (this.value.isLong()) {
            return this.value.asLong();
        } else if (this.value.isNumber()) {
            return this.asNumber().longValue();
        } else {
            if (this.value.isString()) {
                try {
                    return Long.valueOf(this.asString());
                } catch (NumberFormatException var2) {
                }
            }

            return null;
        }
    }

    public Float asFloat() {
        if (this.value.isFloat()) {
            return this.value.asFloat();
        } else if (this.value.isNumber()) {
            return this.asNumber().floatValue();
        } else {
            if (this.value.isString()) {
                try {
                    return Float.valueOf(this.asString());
                } catch (NumberFormatException var2) {
                }
            }

            return null;
        }
    }

    public Double asDouble() {
        if (this.value.isDouble()) {
            return this.value.asDouble();
        } else if (this.value.isNumber()) {
            return this.asNumber().doubleValue();
        } else {
            if (this.value.isString()) {
                try {
                    return Double.valueOf(this.asString());
                } catch (NumberFormatException var2) {
                }
            }

            return null;
        }
    }

    public Number asNumber() {
        if (this.value.isNumber()) {
            return this.value.asNumber();
        } else if (this.value.isString()) {
            String str = this.value.asString();
            Number number = Strings.asNumber(str);
            if (number instanceof Short) {
                number = ((Number) number).intValue();
            }

            return (Number) number;
        } else {
            return null;
        }
    }

    public BigInteger asBigInteger() {
        if (this.value.isBigInteger()) {
            return this.value.asBigInteger();
        } else if (this.value.isBigDecimal()) {
            return this.value.asBigDecimal().toBigInteger();
        } else if (this.value instanceof Number) {
            return BigInteger.valueOf(this.asLong());
        } else {
            if (this.value.isString()) {
                try {
                    return new BigInteger(this.asString());
                } catch (NumberFormatException var2) {
                }
            }

            return null;
        }
    }

    public BigDecimal asBigDecimal() {
        if (this.value.isBigDecimal()) {
            return this.value.asBigDecimal();
        } else if (this.value.isBigInteger()) {
            return new BigDecimal(this.value.asBigInteger());
        } else if (!this.value.isInteger() && !this.value.isLong()) {
            if (!this.value.isFloat() && !this.value.isDouble()) {
                if (this.value.isString()) {
                    try {
                        return new BigDecimal(this.asString());
                    } catch (NumberFormatException var2) {
                    }
                }

                return null;
            } else {
                return BigDecimal.valueOf(this.asDouble());
            }
        } else {
            return BigDecimal.valueOf(this.asLong());
        }
    }

    public byte[] asBytes() {
        if (this.value.isBinary()) {
            return this.value.asBytes();
        } else {
            return this.value.isString() ? this.value.asString().getBytes() : null;
        }
    }

    public Document asDocument() {
        return this.value.isDocument() ? this.value.asDocument() : null;
    }

    public Array asArray() {
        return this.value.isArray() ? this.value.asArray() : null;
    }

    public boolean isNull() {
        return this.value.isNull();
    }

    public boolean isString() {
        return this.value.isString();
    }

    public boolean isBoolean() {
        return this.value.isBoolean();
    }

    public boolean isInteger() {
        return this.value.isInteger();
    }

    public boolean isLong() {
        return this.value.isLong();
    }

    public boolean isFloat() {
        return this.value.isFloat();
    }

    public boolean isDouble() {
        return this.value.isDouble();
    }

    public boolean isNumber() {
        return this.value.isNumber();
    }

    public boolean isBigInteger() {
        return this.value.isBigInteger();
    }

    public boolean isBigDecimal() {
        return this.value.isBigDecimal();
    }

    public boolean isDocument() {
        return this.value.isDocument();
    }

    public boolean isArray() {
        return this.value.isArray();
    }

    public boolean isBinary() {
        return this.value.isBinary();
    }

    public Value convert() {
        return this;
    }

    public Value clone() {
        Value clonedValue = this.value.clone();
        return clonedValue == this.value ? this : new ConvertingValue(clonedValue);
    }
}
