package io.debezium.serde.json;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

import java.util.Map;

@Incubating
public class JsonSerdeConfig extends AbstractConfig {
    public static final Field FROM_FIELD;
    public static final Field UNKNOWN_PROPERTIES_IGNORED;
    private static final ConfigDef CONFIG;
    private String sourceField;
    private boolean unknownPropertiesIgnored;

    private static int isEnvelopeFieldName(Configuration config, Field field, Field.ValidationOutput problems) {
        String fieldName = config.getString(field);
        if (fieldName == null) {
            return 0;
        } else if (!"after".equals(fieldName) && !"before".equals(fieldName)) {
            problems.accept(field, fieldName, "Allowed values are 'before' or 'after'");
            return 1;
        } else {
            return 0;
        }
    }

    public static ConfigDef configDef() {
        return CONFIG;
    }

    public JsonSerdeConfig(Map<String, ?> props) {
        super(CONFIG, props);
        this.sourceField = this.getString(FROM_FIELD.name());
        this.unknownPropertiesIgnored = this.getBoolean(UNKNOWN_PROPERTIES_IGNORED.name());
    }

    public String sourceField() {
        return this.sourceField;
    }

    public boolean asEnvelope() {
        return this.sourceField == null;
    }

    public boolean isUnknownPropertiesIgnored() {
        return this.unknownPropertiesIgnored;
    }

    static {
        FROM_FIELD = Field.create("from.field").withDisplayName("What Envelope field should be deserialized (before/after)").withType(Type.STRING).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Enables user to choose which of Envelope provided fields should be deserialized as the payload.If not set then the envelope is provided as is.").withValidation(JsonSerdeConfig::isEnvelopeFieldName);
        UNKNOWN_PROPERTIES_IGNORED = Field.create("unknown.properties.ignored").withDisplayName("Unknown properties ignored").withType(Type.BOOLEAN).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Controls whether unknown properties will be ignored or cause a JsonMappingException when encountered.").withDefault(false);
        CONFIG = new ConfigDef();
        Field.group(CONFIG, "Source", FROM_FIELD, UNKNOWN_PROPERTIES_IGNORED);
    }
}
