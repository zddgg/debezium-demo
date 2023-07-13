package io.debezium.spi.converter;

import io.debezium.common.annotation.Incubating;

import java.util.Properties;

@Incubating
public interface CustomConverter<S, F extends ConvertedField> {
    void configure(Properties var1);

    void converterFor(F var1, ConverterRegistration<S> var2);

    public interface ConverterRegistration<S> {
        void register(S var1, Converter var2);
    }

    @FunctionalInterface
    public interface Converter {
        Object convert(Object var1);
    }
}
