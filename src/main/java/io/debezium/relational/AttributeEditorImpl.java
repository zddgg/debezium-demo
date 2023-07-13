package io.debezium.relational;

import io.debezium.DebeziumException;

import java.math.BigDecimal;
import java.math.BigInteger;

final class AttributeEditorImpl implements AttributeEditor {
    private String name;
    private String value;

    protected AttributeEditorImpl() {
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

    public AttributeEditor name(String name) {
        this.name = name;
        return this;
    }

    public AttributeEditor value(Object value) {
        if (value == null) {
            this.value = null;
        } else if (value instanceof String) {
            this.value = (String) value;
        } else if (value instanceof Integer) {
            this.value = Integer.toString((Integer) value);
        } else if (value instanceof Long) {
            this.value = Long.toString((Long) value);
        } else if (value instanceof Boolean) {
            this.value = Boolean.toString((Boolean) value);
        } else if (value instanceof BigInteger) {
            this.value = value.toString();
        } else if (value instanceof BigDecimal) {
            this.value = value.toString();
        } else if (value instanceof Float) {
            this.value = Float.toString((Float) value);
        } else {
            if (!(value instanceof Double)) {
                throw new DebeziumException(value.getClass().getName() + " cannot be used for attribute values");
            }

            this.value = Double.toString((Double) value);
        }

        return this;
    }

    public Attribute create() {
        return new AttributeImpl(this.name, this.value);
    }

    public String toString() {
        return this.create().toString();
    }
}
