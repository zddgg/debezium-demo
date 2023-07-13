package io.debezium.document;

import io.debezium.annotation.Immutable;

import java.math.BigDecimal;
import java.math.BigInteger;

@Immutable
final class NullValue implements Value {
    public static final Value INSTANCE = new NullValue();

    private NullValue() {
    }

    public int hashCode() {
        return 0;
    }

    public boolean equals(Object obj) {
        return obj == this;
    }

    public String toString() {
        return "null";
    }

    public int compareTo(Value that) {
        return this == that ? 0 : -1;
    }

    public Type getType() {
        return Type.NULL;
    }

    public Object asObject() {
        return null;
    }

    public String asString() {
        return null;
    }

    public Integer asInteger() {
        return null;
    }

    public Long asLong() {
        return null;
    }

    public Boolean asBoolean() {
        return null;
    }

    public Number asNumber() {
        return null;
    }

    public BigInteger asBigInteger() {
        return null;
    }

    public BigDecimal asBigDecimal() {
        return null;
    }

    public Float asFloat() {
        return null;
    }

    public Double asDouble() {
        return null;
    }

    public byte[] asBytes() {
        return null;
    }

    public Document asDocument() {
        return null;
    }

    public Array asArray() {
        return null;
    }

    public boolean isNull() {
        return true;
    }

    public boolean isString() {
        return false;
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }

    public boolean isLong() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isNumber() {
        return false;
    }

    public boolean isBigInteger() {
        return false;
    }

    public boolean isBigDecimal() {
        return false;
    }

    public boolean isBinary() {
        return false;
    }

    public boolean isDocument() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public Value convert() {
        return this;
    }

    public Value clone() {
        return this;
    }
}
