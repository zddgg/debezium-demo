package io.debezium.transforms.outbox;

import io.debezium.config.Configuration;
import io.debezium.config.EnumeratedValue;
import io.debezium.config.Field;
import io.debezium.transforms.tracing.ActivateTracingSpan;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EventRouterConfigDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRouterConfigDefinition.class);
    public static final Field OPERATION_INVALID_BEHAVIOR;
    public static final Field FIELD_EVENT_ID;
    public static final Field FIELD_EVENT_KEY;
    public static final Field FIELD_EVENT_TYPE;
    public static final Field FIELD_EVENT_TIMESTAMP;
    public static final Field FIELD_PAYLOAD;
    public static final Field FIELDS_ADDITIONAL_PLACEMENT;
    public static final Field FIELD_SCHEMA_VERSION;
    public static final Field ROUTE_BY_FIELD;
    public static final Field ROUTE_TOPIC_REGEX;
    public static final Field ROUTE_TOPIC_REPLACEMENT;
    public static final Field ROUTE_TOMBSTONE_ON_EMPTY_PAYLOAD;
    public static final Field EXPAND_JSON_PAYLOAD;
    public static final Field TABLE_JSON_PAYLOAD_NULL_BEHAVIOR;
    static final Field[] CONFIG_FIELDS;

    public static ConfigDef configDef() {
        ConfigDef config = new ConfigDef();
        Field.group(config, "Table", FIELD_EVENT_ID, FIELD_EVENT_KEY, FIELD_EVENT_TYPE, FIELD_PAYLOAD, FIELD_EVENT_TIMESTAMP, FIELDS_ADDITIONAL_PLACEMENT, FIELD_SCHEMA_VERSION, OPERATION_INVALID_BEHAVIOR, EXPAND_JSON_PAYLOAD, TABLE_JSON_PAYLOAD_NULL_BEHAVIOR);
        Field.group(config, "Router", ROUTE_BY_FIELD, ROUTE_TOPIC_REGEX, ROUTE_TOPIC_REPLACEMENT, ROUTE_TOMBSTONE_ON_EMPTY_PAYLOAD);
        Field.group(config, "Tracing", ActivateTracingSpan.TRACING_SPAN_CONTEXT_FIELD, ActivateTracingSpan.TRACING_OPERATION_NAME, ActivateTracingSpan.TRACING_CONTEXT_FIELD_REQUIRED);
        return config;
    }

    static List<AdditionalField> parseAdditionalFieldsConfig(Configuration config) {
        String extraFieldsMapping = config.getString(FIELDS_ADDITIONAL_PLACEMENT);
        List<AdditionalField> additionalFields = new ArrayList();
        if (extraFieldsMapping != null) {
            String[] var3 = extraFieldsMapping.split(",");
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String field = var3[var5];
                String[] parts = field.split(":");
                String fieldName = parts[0];
                AdditionalFieldPlacement placement = AdditionalFieldPlacement.parse(parts[1]);
                AdditionalField addField = new AdditionalField(placement, fieldName, parts.length == 3 ? parts[2] : fieldName);
                additionalFields.add(addField);
            }
        }

        return additionalFields;
    }

    static {
        OPERATION_INVALID_BEHAVIOR = Field.create("table.op.invalid.behavior").withDisplayName("Behavior when capturing an unexpected outbox event").withEnum(InvalidOperationBehavior.class, InvalidOperationBehavior.SKIP_AND_WARN).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("While Debezium is capturing changes from the outbox table, it is expecting only to process 'create' or 'delete' row events; in case something else is processed this transform can log it as warning, error or stop the process.");
        FIELD_EVENT_ID = Field.create("table.field.event.id").withDisplayName("Event ID Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("id").withDescription("The column which contains the event ID within the outbox table");
        FIELD_EVENT_KEY = Field.create("table.field.event.key").withDisplayName("Event Key Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("aggregateid").withDescription("The column which contains the event key within the outbox table");
        FIELD_EVENT_TYPE = Field.create("table.field.event.type").withDisplayName("Event Type Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("type").withDescription("The column which contains the event type within the outbox table");
        FIELD_EVENT_TIMESTAMP = Field.create("table.field.event.timestamp").withDisplayName("Event Timestamp Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Optionally you can override the Kafka message timestamp with a value from a chosen column, otherwise it'll be the Debezium event processed timestamp.");
        FIELD_PAYLOAD = Field.create("table.field.event.payload").withDisplayName("Event Payload Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("payload").withDescription("The column which contains the event payload within the outbox table");
        FIELDS_ADDITIONAL_PLACEMENT = Field.create("table.fields.additional.placement").withDisplayName("Settings for each additional column in the outbox table").withType(Type.LIST).withValidation(AdditionalFieldsValidator::isListOfStringPairs).withWidth(Width.MEDIUM).withImportance(Importance.HIGH).withDescription("Extra fields can be added as part of the event envelope or a message header, format is a list of colon-delimited pairs or trios when you desire to have aliases, e.g. <code>id:header,field_name:envelope:alias,field_name:partition</code> ");
        FIELD_SCHEMA_VERSION = Field.create("table.field.event.schema.version").withDisplayName("Event Schema Version Field").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("The column which contains the event schema version within the outbox table");
        ROUTE_BY_FIELD = Field.create("route.by.field").withDisplayName("Field to route events by").withType(Type.STRING).withDefault("aggregatetype").withWidth(Width.MEDIUM).withImportance(Importance.HIGH).withDescription("The column which determines how the events will be routed, the value will become part of the topic name");
        ROUTE_TOPIC_REGEX = Field.create("route.topic.regex").withDisplayName("The name of the routed topic").withType(Type.STRING).withValidation(Field::isRegex).withDefault("(?<routedByValue>.*)").withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("The default regex to use within the RegexRouter, the default capture will allow to replace the routed field into a new topic name defined in 'route.topic.replacement'");
        ROUTE_TOPIC_REPLACEMENT = Field.create("route.topic.replacement").withDisplayName("The name of the routed topic").withType(Type.STRING).withDefault("outbox.event.${routedByValue}").withWidth(Width.MEDIUM).withImportance(Importance.HIGH).withDescription("The name of the topic in which the events will be routed, a replacement '${routedByValue}' is available which is the value of The column configured via 'route.by.field'");
        ROUTE_TOMBSTONE_ON_EMPTY_PAYLOAD = Field.create("route.tombstone.on.empty.payload").withDisplayName("Empty payloads cause a tombstone message").withType(Type.BOOLEAN).withDefault(false).withWidth(Width.MEDIUM).withImportance(Importance.HIGH).withDescription("Whether or not an empty payload should cause a tombstone event.");
        EXPAND_JSON_PAYLOAD = Field.create("table.expand.json.payload").withDisplayName("Expand Payload escaped string as real JSON").withType(Type.BOOLEAN).withDefault(false).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Whether or not to try unescaping a JSON string and make it real JSON. It will infer schema information from payload and update the record schema accordingly. If content is not JSON, it just produces a warning and emits the record unchanged");
        TABLE_JSON_PAYLOAD_NULL_BEHAVIOR = Field.create("table.json.payload.null.behavior").withDisplayName("Behavior when json payload including null value").withEnum(JsonPayloadNullFieldBehavior.class, JsonPayloadNullFieldBehavior.IGNORE).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Behavior when json payload including null value, the default will ignore null, optional_bytes will keep the null value, and treat null as optional bytes of connect.");
        CONFIG_FIELDS = new Field[]{FIELD_EVENT_ID, FIELD_EVENT_KEY, FIELD_EVENT_TYPE, FIELD_PAYLOAD, FIELD_EVENT_TIMESTAMP, FIELDS_ADDITIONAL_PLACEMENT, FIELD_SCHEMA_VERSION, ROUTE_BY_FIELD, ROUTE_TOPIC_REGEX, ROUTE_TOPIC_REPLACEMENT, ROUTE_TOMBSTONE_ON_EMPTY_PAYLOAD, OPERATION_INVALID_BEHAVIOR, EXPAND_JSON_PAYLOAD, TABLE_JSON_PAYLOAD_NULL_BEHAVIOR};
    }

    public static enum AdditionalFieldPlacement implements EnumeratedValue {
        HEADER("header"),
        ENVELOPE("envelope"),
        PARTITION("partition");

        private final String value;

        private AdditionalFieldPlacement(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static AdditionalFieldPlacement parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                AdditionalFieldPlacement[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    AdditionalFieldPlacement option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        // $FF: synthetic method
        private static AdditionalFieldPlacement[] $values() {
            return new AdditionalFieldPlacement[]{HEADER, ENVELOPE, PARTITION};
        }
    }

    public static class AdditionalField {
        private final AdditionalFieldPlacement placement;
        private final String field;
        private final String alias;

        AdditionalField(AdditionalFieldPlacement placement, String field, String alias) {
            this.placement = placement;
            this.field = field;
            this.alias = alias;
        }

        public AdditionalFieldPlacement getPlacement() {
            return this.placement;
        }

        public String getField() {
            return this.field;
        }

        public String getAlias() {
            return this.alias;
        }
    }

    public static enum InvalidOperationBehavior implements EnumeratedValue {
        SKIP_AND_WARN("warn"),
        SKIP_AND_ERROR("error"),
        FATAL("fatal");

        private final String value;

        private InvalidOperationBehavior(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static InvalidOperationBehavior parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                InvalidOperationBehavior[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    InvalidOperationBehavior option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        // $FF: synthetic method
        private static InvalidOperationBehavior[] $values() {
            return new InvalidOperationBehavior[]{SKIP_AND_WARN, SKIP_AND_ERROR, FATAL};
        }
    }

    public static enum JsonPayloadNullFieldBehavior implements EnumeratedValue {
        IGNORE("ignore"),
        OPTIONAL_BYTES("optional_bytes");

        private final String value;

        private JsonPayloadNullFieldBehavior(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static JsonPayloadNullFieldBehavior parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                JsonPayloadNullFieldBehavior[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    JsonPayloadNullFieldBehavior option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        // $FF: synthetic method
        private static JsonPayloadNullFieldBehavior[] $values() {
            return new JsonPayloadNullFieldBehavior[]{IGNORE, OPTIONAL_BYTES};
        }
    }
}
