package io.debezium.connector.mysql.converters;

import io.debezium.function.Predicates;
import io.debezium.spi.converter.CustomConverter;
import io.debezium.spi.converter.RelationalColumn;
import io.debezium.util.Collect;
import io.debezium.util.Strings;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

public class TinyIntOneToBooleanConverter implements CustomConverter<SchemaBuilder, RelationalColumn> {
    private static final Boolean FALLBACK;
    public static final String SELECTOR_PROPERTY = "selector";
    public static final String LENGTH_CHECKER = "length.checker";
    private static final List<String> TINYINT_FAMILY;
    private static final Logger LOGGER;
    private Predicate<RelationalColumn> selector = (x) -> {
        return true;
    };
    private boolean lengthChecker = true;

    public void configure(Properties props) {
        String selectorConfig = props.getProperty("selector");
        if (!Strings.isNullOrEmpty(selectorConfig)) {
            this.selector = Predicates.includes(selectorConfig.trim(), (x) -> {
                String var10000 = x.dataCollection();
                return var10000 + "." + x.name();
            });
        }

        String lengthCheckerConfig = props.getProperty("length.checker");
        if (!Strings.isNullOrEmpty(lengthCheckerConfig)) {
            this.lengthChecker = Boolean.parseBoolean(lengthCheckerConfig);
        }

    }

    public void converterFor(RelationalColumn field, ConverterRegistration<SchemaBuilder> registration) {
        if (TINYINT_FAMILY.contains(field.typeName().toUpperCase()) && (!this.lengthChecker || field.length().orElse(-1) == 1) && this.selector.test(field)) {
            registration.register(SchemaBuilder.bool(), (x) -> {
                if (x == null) {
                    if (field.isOptional()) {
                        return null;
                    } else {
                        return field.hasDefaultValue() ? field.defaultValue() : FALLBACK;
                    }
                } else if (x instanceof Boolean) {
                    return x;
                } else if (x instanceof Number) {
                    return ((Number) x).intValue() > 0;
                } else if (x instanceof String) {
                    try {
                        return Integer.parseInt((String) x);
                    } catch (NumberFormatException var3) {
                        return Boolean.parseBoolean((String) x);
                    }
                } else {
                    LOGGER.warn("Cannot convert '{}' to boolean", x.getClass());
                    return FALLBACK;
                }
            });
        }
    }

    static {
        FALLBACK = Boolean.FALSE;
        TINYINT_FAMILY = Collect.arrayListOf("TINYINT", new String[]{"TINYINT UNSIGNED"});
        LOGGER = LoggerFactory.getLogger(TinyIntOneToBooleanConverter.class);
    }
}
