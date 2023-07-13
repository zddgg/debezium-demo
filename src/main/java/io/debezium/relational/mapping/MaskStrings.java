package io.debezium.relational.mapping;

import io.debezium.annotation.Immutable;
import io.debezium.relational.Column;
import io.debezium.relational.ValueConverter;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.function.Function;

public class MaskStrings implements ColumnMapper {
    private final Function<Column, ValueConverter> converterFromColumn;

    public MaskStrings(String maskValue) {
        Objects.requireNonNull(maskValue);
        this.converterFromColumn = (ignored) -> {
            return new MaskingValueConverter(maskValue);
        };
    }

    public MaskStrings(byte[] salt, String hashAlgorithm, HashingByteArrayStrategy hashingByteArrayStrategy) {
        Objects.requireNonNull(salt);
        Objects.requireNonNull(hashAlgorithm);
        this.converterFromColumn = (column) -> {
            HashValueConverter hashValueConverter = new HashValueConverter(salt, hashAlgorithm, hashingByteArrayStrategy);
            return (ValueConverter) (column.length() > 0 ? hashValueConverter.and(new TruncateStrings.TruncatingValueConverter(column.length())) : hashValueConverter);
        };
    }

    public ValueConverter create(Column column) {
        switch (column.jdbcType()) {
            case -16:
            case -15:
            case -9:
            case -1:
            case 1:
            case 12:
            case 70:
            case 2005:
            case 2011:
                return (ValueConverter) this.converterFromColumn.apply(column);
            default:
                return ValueConverter.passthrough();
        }
    }

    public void alterFieldSchema(Column column, SchemaBuilder schemaBuilder) {
        schemaBuilder.parameter("masked", "true");
    }

    public static enum HashingByteArrayStrategy {
        V1 {
            byte[] toByteArray(Serializable value) throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos);
                out.writeObject(value);
                return bos.toByteArray();
            }
        },
        V2 {
            byte[] toByteArray(Serializable value) {
                return value.toString().getBytes();
            }
        };

        abstract byte[] toByteArray(Serializable var1) throws IOException;

        // $FF: synthetic method
        private static HashingByteArrayStrategy[] $values() {
            return new HashingByteArrayStrategy[]{V1, V2};
        }
    }

    @Immutable
    protected static final class HashValueConverter implements ValueConverter {
        private static final Logger LOGGER = LoggerFactory.getLogger(HashValueConverter.class);
        private final byte[] salt;
        private final MessageDigest hashAlgorithm;
        private final HashingByteArrayStrategy hashingByteArrayStrategy;

        public HashValueConverter(byte[] salt, String hashAlgorithm, HashingByteArrayStrategy hashingByteArrayStrategy) {
            this.salt = salt;
            this.hashingByteArrayStrategy = hashingByteArrayStrategy;

            try {
                this.hashAlgorithm = MessageDigest.getInstance(hashAlgorithm);
            } catch (NoSuchAlgorithmException var5) {
                throw new IllegalArgumentException(var5);
            }
        }

        public Object convert(Object value) {
            if (value instanceof Serializable) {
                try {
                    return this.toHash((Serializable) value);
                } catch (IOException var3) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("can't calculate hash", var3);
                    }
                }
            }

            return null;
        }

        private String toHash(Serializable value) throws IOException {
            this.hashAlgorithm.reset();
            this.hashAlgorithm.update(this.salt);
            byte[] valueToByteArray = this.hashingByteArrayStrategy.toByteArray(value);
            return this.convertToHexadecimalFormat(this.hashAlgorithm.digest(valueToByteArray));
        }

        private String convertToHexadecimalFormat(byte[] bytes) {
            StringBuilder hashString = new StringBuilder();
            byte[] var3 = bytes;
            int var4 = bytes.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                byte b = var3[var5];
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString();
        }
    }

    @Immutable
    protected static final class MaskingValueConverter implements ValueConverter {
        protected final String maskValue;

        public MaskingValueConverter(String maskValue) {
            this.maskValue = maskValue;

            assert this.maskValue != null;

        }

        public Object convert(Object value) {
            return this.maskValue;
        }
    }
}
