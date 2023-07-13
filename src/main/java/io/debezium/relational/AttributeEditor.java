package io.debezium.relational;

import io.debezium.annotation.NotThreadSafe;

import java.math.BigDecimal;
import java.math.BigInteger;

@NotThreadSafe
public interface AttributeEditor {
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

    AttributeEditor name(String var1);

    AttributeEditor value(Object var1);

    Attribute create();
}
