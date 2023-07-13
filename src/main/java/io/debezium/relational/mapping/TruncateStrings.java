package io.debezium.relational.mapping;

import io.debezium.annotation.Immutable;
import io.debezium.relational.Column;
import io.debezium.relational.ValueConverter;
import org.apache.kafka.connect.data.SchemaBuilder;

public class TruncateStrings implements ColumnMapper {
    private final TruncatingValueConverter converter;

    public TruncateStrings(int maxLength) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("Maximum length must be positive");
        } else {
            this.converter = new TruncatingValueConverter(maxLength);
        }
    }

    public ValueConverter create(Column column) {
        return (ValueConverter) (this.isTruncationPossible(column) ? this.converter : ValueConverter.passthrough());
    }

    public void alterFieldSchema(Column column, SchemaBuilder schemaBuilder) {
        if (this.isTruncationPossible(column)) {
            schemaBuilder.parameter("truncateLength", Integer.toString(this.converter.maxLength));
        }

    }

    protected boolean isTruncationPossible(Column column) {
        return column.length() < 0 || column.length() > this.converter.maxLength;
    }

    @Immutable
    protected static final class TruncatingValueConverter implements ValueConverter {
        protected final int maxLength;

        public TruncatingValueConverter(int maxLength) {
            this.maxLength = maxLength;

            assert this.maxLength > 0;

        }

        public Object convert(Object value) {
            if (value instanceof String) {
                String str = (String) value;
                if (str.length() > this.maxLength) {
                    return str.substring(0, this.maxLength);
                }
            }

            return value;
        }
    }
}
