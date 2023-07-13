package io.debezium.spi.converter;

import io.debezium.common.annotation.Incubating;

@Incubating
public interface ConvertedField {
    String name();

    String dataCollection();
}
