package io.debezium.relational;

import io.debezium.annotation.Immutable;

import java.math.BigDecimal;
import java.math.BigInteger;

@Immutable
public interface Attribute {
    static AttributeEditor editor() {
        return new AttributeEditorImpl();
    }

    String name();

    String value();

    String asString();

    Integer asInteger();

    Long asLong();

    Boolean asBoolean();

    BigInteger asBigInteger();

    BigDecimal asBigDecimal();

    Float asFloat();

    Double asDouble();

    AttributeEditor edit();
}
