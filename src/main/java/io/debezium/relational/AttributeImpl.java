package io.debezium.relational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

final class AttributeImpl implements Attribute {
    private final String name;
    private final String value;

    AttributeImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return this.name;
    }

    public String value() {
        return this.value;
    }

    public String asString() {
        return this.value;
    }

    public Integer asInteger() {
        return this.value == null ? null : Integer.parseInt(this.value);
    }

    public Long asLong() {
        return this.value == null ? null : Long.parseLong(this.value);
    }

    public Boolean asBoolean() {
        return this.value == null ? null : Boolean.parseBoolean(this.value);
    }

    public BigInteger asBigInteger() {
        return this.value == null ? null : new BigInteger(this.value);
    }

    public BigDecimal asBigDecimal() {
        return this.value == null ? null : new BigDecimal(this.value);
    }

    public Float asFloat() {
        return this.value == null ? null : Float.parseFloat(this.value);
    }

    public Double asDouble() {
        return this.value == null ? null : Double.parseDouble(this.value);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Attribute)) {
            return false;
        } else {
            AttributeImpl attribute = (AttributeImpl) obj;
            return Objects.equals(this.name, attribute.name) && Objects.equals(this.value, attribute.value);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.value});
    }

    public String toString() {
        return "name='" + this.name + "', value='" + this.value + "'";
    }

    public AttributeEditor edit() {
        return Attribute.editor().name(this.name()).value(this.value());
    }
}
