package io.debezium.converters;

import io.debezium.config.Configuration;
import io.debezium.config.Instantiator;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.errors.DataException;
import org.apache.kafka.connect.storage.Converter;
import org.apache.kafka.connect.storage.ConverterConfig;
import org.apache.kafka.connect.storage.HeaderConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ByteArrayConverter implements Converter, HeaderConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ByteArrayConverter.class);
    private static final ConfigDef CONFIG_DEF = ConverterConfig.newConfigDef();
    protected static final String DELEGATE_CONVERTER_TYPE = "delegate.converter.type";
    private Converter delegateConverter;

    public ConfigDef config() {
        return CONFIG_DEF;
    }

    public void configure(Map<String, ?> configs) {
    }

    public void configure(Map<String, ?> configs, boolean isKey) {
        String converterTypeName = (String) configs.get("delegate.converter.type");
        if (converterTypeName != null) {
            this.delegateConverter = (Converter) Instantiator.getInstance(converterTypeName);
            this.delegateConverter.configure(Configuration.from(configs).subset("delegate.converter.type", true).asMap(), isKey);
        }

    }

    public byte[] fromConnectData(String topic, Schema schema, Object value) {
        if (schema != null && schema.type() != Type.BYTES) {
            this.assertDelegateProvided(topic, value);
            LOGGER.debug("Value is not of Schema.Type.BYTES, delegating to " + this.delegateConverter.getClass().getName());
            return this.delegateConverter.fromConnectData(topic, schema, value);
        } else if (value != null && !(value instanceof byte[])) {
            this.assertDelegateProvided(topic, value);
            LOGGER.debug("Value is not of Schema.Type.BYTES, delegating to " + this.delegateConverter.getClass().getName());
            return this.delegateConverter.fromConnectData(topic, schema, value);
        } else {
            return (byte[]) value;
        }
    }

    public SchemaAndValue toConnectData(String topic, byte[] value) {
        return new SchemaAndValue(Schema.OPTIONAL_BYTES_SCHEMA, value);
    }

    public byte[] fromConnectHeader(String topic, String headerKey, Schema schema, Object value) {
        return this.fromConnectData(topic, schema, value);
    }

    public SchemaAndValue toConnectHeader(String topic, String headerKey, byte[] value) {
        return this.toConnectData(topic, value);
    }

    public void close() throws IOException {
    }

    private void assertDelegateProvided(String name, Object type) {
        if (this.delegateConverter == null) {
            throw new DataException("A " + name + " of type '" + type + "' requires a delegate.converter.type to be configured");
        }
    }

    static {
        CONFIG_DEF.define("delegate.converter.type", ConfigDef.Type.STRING, (Object) null, Importance.LOW, "Specifies the delegate converter class");
    }
}
