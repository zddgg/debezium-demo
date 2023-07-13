package io.debezium.converters;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.converters.spi.SerializerType;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.storage.ConverterConfig;

import java.util.Map;

public class CloudEventsConverterConfig extends ConverterConfig {
    public static final String CLOUDEVENTS_SERIALIZER_TYPE_CONFIG = "serializer.type";
    public static final String CLOUDEVENTS_SERIALIZER_TYPE_DEFAULT = "json";
    private static final String CLOUDEVENTS_SERIALIZER_TYPE_DOC = "Specify a serializer to serialize CloudEvents values";
    public static final String CLOUDEVENTS_DATA_SERIALIZER_TYPE_CONFIG = "data.serializer.type";
    public static final String CLOUDEVENTS_DATA_SERIALIZER_TYPE_DEFAULT = "json";
    private static final String CLOUDEVENTS_DATA_SERIALIZER_TYPE_DOC = "Specify a serializer to serialize the data field of CloudEvents values";
    public static final String CLOUDEVENTS_SCHEMA_NAME_ADJUSTMENT_MODE_CONFIG = "schema.name.adjustment.mode";
    public static final String CLOUDEVENTS_SCHEMA_NAME_ADJUSTMENT_MODE_DEFAULT = "avro";
    private static final String CLOUDEVENTS_SCHEMA_NAME_ADJUSTMENT_MODE_DOC = "Specify how schema names should be adjusted for compatibility with the message converter used by the connector, including:'avro' replaces the characters that cannot be used in the Avro type name with underscore (default)'none' does not apply any adjustment";
    private static final ConfigDef CONFIG = ConverterConfig.newConfigDef();

    public static ConfigDef configDef() {
        return CONFIG;
    }

    public CloudEventsConverterConfig(Map<String, ?> props) {
        super(CONFIG, props);
    }

    public SerializerType cloudeventsSerializerType() {
        return SerializerType.withName(this.getString("serializer.type"));
    }

    public SerializerType cloudeventsDataSerializerTypeConfig() {
        return SerializerType.withName(this.getString("data.serializer.type"));
    }

    public CommonConnectorConfig.SchemaNameAdjustmentMode schemaNameAdjustmentMode() {
        return CommonConnectorConfig.SchemaNameAdjustmentMode.parse(this.getString("schema.name.adjustment.mode"));
    }

    static {
        CONFIG.define("serializer.type", Type.STRING, "json", Importance.HIGH, "Specify a serializer to serialize CloudEvents values");
        CONFIG.define("data.serializer.type", Type.STRING, "json", Importance.HIGH, "Specify a serializer to serialize the data field of CloudEvents values");
        CONFIG.define("schema.name.adjustment.mode", Type.STRING, "avro", Importance.LOW, "Specify how schema names should be adjusted for compatibility with the message converter used by the connector, including:'avro' replaces the characters that cannot be used in the Avro type name with underscore (default)'none' does not apply any adjustment");
    }
}
