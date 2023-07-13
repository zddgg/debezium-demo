package io.debezium.connector.mysql.converters;

import io.debezium.function.Predicates;
import io.debezium.spi.converter.CustomConverter;
import io.debezium.spi.converter.RelationalColumn;
import io.debezium.util.Strings;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.function.Predicate;

public class JdbcSinkDataTypesConverter implements CustomConverter<SchemaBuilder, RelationalColumn> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcSinkDataTypesConverter.class);
    private static final Short INT16_FALLBACK = Short.valueOf((short) 0);
    private static final Float FLOAT32_FALLBACK = 0.0F;
    private static final Double FLOAT64_FALLBACK = 0.0;
    public static final String SELECTOR_BOOLEAN_PROPERTY = "selector.boolean";
    public static final String SELECTOR_REAL_PROPERTY = "selector.real";
    public static final String SELECTOR_STRING_PROPERTY = "selector.string";
    public static final String TREAT_REAL_AS_DOUBLE = "treat.real.as.double";
    private Predicate<RelationalColumn> selectorBoolean = (x) -> {
        return false;
    };
    private Predicate<RelationalColumn> selectorReal = (x) -> {
        return false;
    };
    private Predicate<RelationalColumn> selectorString = (x) -> {
        return false;
    };
    private boolean treatRealAsDouble = true;

    public void configure(Properties props) {
        String booleanSelectorConfig = props.getProperty("selector.boolean");
        if (!Strings.isNullOrBlank(booleanSelectorConfig)) {
            this.selectorBoolean = Predicates.includes(booleanSelectorConfig.trim(), (x) -> {
                String var10000 = x.dataCollection();
                return var10000 + "." + x.name();
            });
        }

        String realSelectorConfig = props.getProperty("selector.real");
        if (!Strings.isNullOrBlank(realSelectorConfig)) {
            this.selectorReal = Predicates.includes(realSelectorConfig.trim(), (x) -> {
                String var10000 = x.dataCollection();
                return var10000 + "." + x.name();
            });
        }

        String stringSelectorConfig = props.getProperty("selector.string");
        if (!Strings.isNullOrBlank(stringSelectorConfig)) {
            this.selectorString = Predicates.includes(stringSelectorConfig.trim(), (x) -> {
                String var10000 = x.dataCollection();
                return var10000 + "." + x.name();
            });
        }

        String realAsDouble = props.getProperty("treat.real.as.double");
        if (!Strings.isNullOrEmpty(realAsDouble)) {
            this.treatRealAsDouble = Boolean.parseBoolean(realAsDouble);
        }

    }

    public void converterFor(RelationalColumn field, ConverterRegistration<SchemaBuilder> registration) {
        if (this.selectorBoolean.test(field)) {
            registration.register(SchemaBuilder.int16(), this.getBooleanConverter(field));
        } else if (this.selectorReal.test(field)) {
            if (this.treatRealAsDouble) {
                registration.register(SchemaBuilder.float64(), this.getRealConverterDouble(field));
            } else {
                registration.register(SchemaBuilder.float32(), this.getRealConverterFloat(field));
            }
        } else if (this.selectorString.test(field)) {
            SchemaBuilder schemaBuilder = SchemaBuilder.string();
            schemaBuilder.parameter("__debezium.source.column.character_set", field.charsetName());
            registration.register(schemaBuilder, this.getStringConverter(field));
        }

    }

    private Converter getBooleanConverter(RelationalColumn field) {
        return (value) -> {
            if (value == null) {
                if (field.isOptional()) {
                    return null;
                } else {
                    return field.hasDefaultValue() ? toTinyInt((Boolean) field.defaultValue()) : INT16_FALLBACK;
                }
            } else if (value instanceof Boolean) {
                return toTinyInt((Boolean) value);
            } else if (value instanceof Number) {
                return toTinyInt(((Number) value).intValue() > 0);
            } else if (value instanceof String) {
                try {
                    return toTinyInt(Integer.parseInt((String) value) > 0);
                } catch (NumberFormatException var3) {
                    return toTinyInt(Boolean.parseBoolean((String) value));
                }
            } else {
                LOGGER.warn("Cannot convert '{}' to INT16", value.getClass());
                return INT16_FALLBACK;
            }
        };
    }

    private Converter getRealConverterDouble(RelationalColumn field) {
        return (value) -> {
            if (value == null) {
                if (field.isOptional()) {
                    return null;
                } else {
                    return field.hasDefaultValue() ? (Double) field.defaultValue() : FLOAT64_FALLBACK;
                }
            } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                return Double.parseDouble((String) value);
            } else {
                LOGGER.warn("Cannot convert '{}' to FLOAT64.", value.getClass());
                return FLOAT64_FALLBACK;
            }
        };
    }

    private Converter getRealConverterFloat(RelationalColumn field) {
        return (value) -> {
            if (value == null) {
                if (field.isOptional()) {
                    return null;
                } else {
                    return field.hasDefaultValue() ? (Float) field.defaultValue() : FLOAT32_FALLBACK;
                }
            } else if (value instanceof Number) {
                return ((Number) value).floatValue();
            } else if (value instanceof String) {
                return Float.parseFloat((String) value);
            } else {
                LOGGER.warn("Cannot convert '{}' to FLOAT32.", value.getClass());
                return FLOAT32_FALLBACK;
            }
        };
    }

    private Converter getStringConverter(RelationalColumn field) {
        return (value) -> {
            if (value == null) {
                if (field.isOptional()) {
                    return null;
                } else {
                    return field.hasDefaultValue() ? (String) field.defaultValue() : "";
                }
            } else if (value instanceof byte[]) {
                return new String((byte[]) value, StandardCharsets.UTF_8);
            } else if (value instanceof Number) {
                return ((Number) value).toString();
            } else if (value instanceof String) {
                return (String) value;
            } else {
                LOGGER.warn("Cannot convert '{}' to STRING", value.getClass());
                return "";
            }
        };
    }

    private static short toTinyInt(Boolean value) {
        return (short) (value ? 1 : 0);
    }
}
