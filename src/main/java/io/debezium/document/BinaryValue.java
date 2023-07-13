package io.debezium.document;

import io.debezium.annotation.Immutable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

@Immutable
final class BinaryValue implements Value {
    private final byte[] value;

    BinaryValue(byte[] value) {
        assert value != null;

        this.value = value;
    }

    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Value) {
            Value that = (Value) obj;
            if (that.isNull()) {
                return false;
            } else if (that.isBinary()) {
                return Arrays.equals(this.value, that.asBytes());
            } else {
                return that.isString() ? Arrays.equals(this.value, that.asString().getBytes()) : false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return new String(this.value);
    }

    public int compareTo(Value that) {
        if (that.isNull()) {
            return 1;
        } else {
            return that.isBinary() ? this.value.length - that.asBytes().length : 1;
        }
    }

    public Type getType() {
        return Type.BINARY;
    }

    public Object asObject() {
        return this.value;
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
        return this.value;
    }

    public Document asDocument() {
        return null;
    }

    public Array asArray() {
        return null;
    }

    public boolean isNull() {
        return false;
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
        return true;
    }

    public boolean isDocument() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public Value convert() {
        return new ConvertingValue(this);
    }

    public Value clone() {
        byte[] copy = new byte[this.value.length];
        System.arraycopy(this.value, 0, copy, 0, this.value.length);
        return new BinaryValue(copy);
    }
}
