package io.debezium.transforms;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.data.Envelope;
import io.debezium.schema.SchemaNameAdjuster;
import io.debezium.util.Strings;
import org.apache.kafka.common.cache.Cache;
import org.apache.kafka.common.cache.LRUCache;
import org.apache.kafka.common.cache.SynchronizedCache;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.Requirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByLogicalTableRouter<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Field TOPIC_REGEX;
    private static final Field TOPIC_REPLACEMENT;
    private static final Field KEY_ENFORCE_UNIQUENESS;
    private static final Field KEY_FIELD_REGEX;
    private static final Field KEY_FIELD_NAME;
    private static final Field KEY_FIELD_REPLACEMENT;
    private static final Field SCHEMA_NAME_ADJUSTMENT_MODE;
    private static final Field LOGICAL_TABLE_CACHE_SIZE;
    private static final Logger LOGGER;
    private SchemaNameAdjuster schemaNameAdjuster;
    private Pattern topicRegex;
    private String topicReplacement;
    private Pattern keyFieldRegex;
    private boolean keyEnforceUniqueness;
    private String keyFieldReplacement;
    private String keyFieldName;
    private Cache<Schema, Schema> keySchemaUpdateCache;
    private Cache<Schema, Schema> envelopeSchemaUpdateCache;
    private Cache<String, String> keyRegexReplaceCache;
    private Cache<String, String> topicRegexReplaceCache;
    private SmtManager<R> smtManager;

    private static int validateKeyFieldReplacement(Configuration config, Field field, Field.ValidationOutput problems) {
        String keyFieldRegex = config.getString(KEY_FIELD_REGEX);
        if (keyFieldRegex != null) {
            keyFieldRegex = keyFieldRegex.trim();
        }

        String keyFieldReplacement = config.getString(KEY_FIELD_REPLACEMENT);
        if (keyFieldReplacement != null) {
            keyFieldReplacement = keyFieldReplacement.trim();
        }

        if (!Strings.isNullOrEmpty(keyFieldRegex) && Strings.isNullOrEmpty(keyFieldReplacement)) {
            problems.accept(KEY_FIELD_REPLACEMENT, (Object) null, String.format("%s must be non-empty if %s is set.", KEY_FIELD_REPLACEMENT.name(), KEY_FIELD_REGEX.name()));
            return 1;
        } else {
            return 0;
        }
    }

    public void configure(Map<String, ?> props) {
        Configuration config = Configuration.from(props);
        Field.Set configFields = Field.setOf(TOPIC_REGEX, TOPIC_REPLACEMENT, KEY_ENFORCE_UNIQUENESS, KEY_FIELD_REGEX, KEY_FIELD_REPLACEMENT, SCHEMA_NAME_ADJUSTMENT_MODE, LOGICAL_TABLE_CACHE_SIZE);
        Logger var10002 = LOGGER;
        Objects.requireNonNull(var10002);
        if (!config.validateAndRecord(configFields, var10002::error)) {
            throw new ConnectException("Unable to validate config.");
        } else {
            this.topicRegex = Pattern.compile(config.getString(TOPIC_REGEX));
            this.topicReplacement = config.getString(TOPIC_REPLACEMENT);
            String keyFieldRegexString = config.getString(KEY_FIELD_REGEX);
            if (keyFieldRegexString != null) {
                keyFieldRegexString = keyFieldRegexString.trim();
            }

            if (keyFieldRegexString != null && !keyFieldRegexString.isEmpty()) {
                this.keyFieldRegex = Pattern.compile(config.getString(KEY_FIELD_REGEX));
                this.keyFieldReplacement = config.getString(KEY_FIELD_REPLACEMENT);
            }

            this.keyFieldName = config.getString(KEY_FIELD_NAME);
            this.keyEnforceUniqueness = config.getBoolean(KEY_ENFORCE_UNIQUENESS);
            int cacheSize = config.getInteger(LOGICAL_TABLE_CACHE_SIZE);
            this.keySchemaUpdateCache = new SynchronizedCache(new LRUCache(cacheSize));
            this.envelopeSchemaUpdateCache = new SynchronizedCache(new LRUCache(cacheSize));
            this.keyRegexReplaceCache = new SynchronizedCache(new LRUCache(cacheSize));
            this.topicRegexReplaceCache = new SynchronizedCache(new LRUCache(cacheSize));
            this.smtManager = new SmtManager(config);
            this.schemaNameAdjuster = CommonConnectorConfig.SchemaNameAdjustmentMode.parse(config.getString(SCHEMA_NAME_ADJUSTMENT_MODE)).createAdjuster();
        }
    }

    public R apply(R record) {
        String oldTopic = record.topic();
        String newTopic = this.determineNewTopic(oldTopic);
        if (newTopic == null) {
            return record;
        } else if (newTopic.isEmpty()) {
            LOGGER.warn("Routing regex returned an empty topic name, propagating original record");
            return record;
        } else {
            LOGGER.debug("Applying topic name transformation from {} to {}", oldTopic, newTopic);
            Schema newKeySchema = null;
            Struct newKey = null;
            Struct oldEnvelope;
            if (record.key() != null) {
                oldEnvelope = Requirements.requireStruct(record.key(), "Updating schema");
                newKeySchema = this.updateKeySchema(oldEnvelope.schema(), newTopic);
                newKey = this.updateKey(newKeySchema, oldEnvelope, oldTopic);
            }

            if (record.value() != null && this.smtManager.isValidEnvelope(record)) {
                oldEnvelope = Requirements.requireStruct(record.value(), "Updating schema");
                Schema newEnvelopeSchema = this.updateEnvelopeSchema(oldEnvelope.schema(), newTopic);
                Struct newEnvelope = this.updateEnvelope(newEnvelopeSchema, oldEnvelope);
                return record.newRecord(newTopic, record.kafkaPartition(), newKeySchema, newKey, newEnvelopeSchema, newEnvelope, record.timestamp());
            } else {
                return record.newRecord(newTopic, record.kafkaPartition(), newKeySchema, newKey, record.valueSchema(), record.value(), record.timestamp());
            }
        }
    }

    public void close() {
    }

    public ConfigDef config() {
        ConfigDef config = new ConfigDef();
        Field.group(config, (String) null, TOPIC_REGEX, TOPIC_REPLACEMENT, KEY_ENFORCE_UNIQUENESS, KEY_FIELD_REGEX, KEY_FIELD_REPLACEMENT, LOGICAL_TABLE_CACHE_SIZE);
        return config;
    }

    private String determineNewTopic(String oldTopic) {
        String newTopic = (String) this.topicRegexReplaceCache.get(oldTopic);
        if (newTopic != null) {
            return newTopic;
        } else {
            Matcher matcher = this.topicRegex.matcher(oldTopic);
            if (matcher.matches()) {
                newTopic = matcher.replaceFirst(this.topicReplacement);
                this.topicRegexReplaceCache.put(oldTopic, newTopic);
                return newTopic;
            } else {
                return null;
            }
        }
    }

    private Schema updateKeySchema(Schema oldKeySchema, String newTopicName) {
        Schema newKeySchema = (Schema) this.keySchemaUpdateCache.get(oldKeySchema);
        if (newKeySchema != null) {
            return newKeySchema;
        } else {
            SchemaBuilder builder = this.copySchemaExcludingName(oldKeySchema, SchemaBuilder.struct());
            builder.name(this.schemaNameAdjuster.adjust(newTopicName + ".Key"));
            if (this.keyEnforceUniqueness) {
                builder.field(this.keyFieldName, Schema.STRING_SCHEMA);
            }

            newKeySchema = builder.build();
            this.keySchemaUpdateCache.put(oldKeySchema, newKeySchema);
            return newKeySchema;
        }
    }

    private Struct updateKey(Schema newKeySchema, Struct oldKey, String oldTopic) {
        Struct newKey = new Struct(newKeySchema);
        Iterator var5 = oldKey.schema().fields().iterator();

        while (var5.hasNext()) {
            org.apache.kafka.connect.data.Field field = (org.apache.kafka.connect.data.Field) var5.next();
            newKey.put(field.name(), oldKey.get(field));
        }

        String physicalTableIdentifier = oldTopic;
        if (this.keyEnforceUniqueness) {
            if (this.keyFieldRegex != null) {
                physicalTableIdentifier = (String) this.keyRegexReplaceCache.get(oldTopic);
                if (physicalTableIdentifier == null) {
                    Matcher matcher = this.keyFieldRegex.matcher(oldTopic);
                    if (matcher.matches()) {
                        physicalTableIdentifier = matcher.replaceFirst(this.keyFieldReplacement);
                        this.keyRegexReplaceCache.put(oldTopic, physicalTableIdentifier);
                    } else {
                        physicalTableIdentifier = oldTopic;
                    }
                }
            }

            newKey.put(this.keyFieldName, physicalTableIdentifier);
        }

        return newKey;
    }

    private Schema updateEnvelopeSchema(Schema oldEnvelopeSchema, String newTopicName) {
        Schema newEnvelopeSchema = (Schema) this.envelopeSchemaUpdateCache.get(oldEnvelopeSchema);
        if (newEnvelopeSchema != null) {
            return newEnvelopeSchema;
        } else {
            Schema oldValueSchema = oldEnvelopeSchema.field("before").schema();
            SchemaBuilder valueBuilder = this.copySchemaExcludingName(oldValueSchema, SchemaBuilder.struct());
            valueBuilder.name(this.schemaNameAdjuster.adjust(newTopicName + ".Value"));
            Schema newValueSchema = valueBuilder.build();
            SchemaBuilder envelopeBuilder = this.copySchemaExcludingName(oldEnvelopeSchema, SchemaBuilder.struct(), false);

            String fieldName;
            Schema fieldSchema;
            for (Iterator var8 = oldEnvelopeSchema.fields().iterator(); var8.hasNext(); envelopeBuilder.field(fieldName, fieldSchema)) {
                org.apache.kafka.connect.data.Field field = (org.apache.kafka.connect.data.Field) var8.next();
                fieldName = field.name();
                fieldSchema = field.schema();
                if (Objects.equals(fieldName, "before") || Objects.equals(fieldName, "after")) {
                    fieldSchema = newValueSchema;
                }
            }

            envelopeBuilder.name(this.schemaNameAdjuster.adjust(Envelope.schemaName(newTopicName)));
            newEnvelopeSchema = envelopeBuilder.build();
            this.envelopeSchemaUpdateCache.put(oldEnvelopeSchema, newEnvelopeSchema);
            return newEnvelopeSchema;
        }
    }

    private Struct updateEnvelope(Schema newEnvelopeSchema, Struct oldEnvelope) {
        Struct newEnvelope = new Struct(newEnvelopeSchema);
        Schema newValueSchema = newEnvelopeSchema.field("before").schema();

        String fieldName;
        Object fieldValue;
        for (Iterator var5 = oldEnvelope.schema().fields().iterator(); var5.hasNext(); newEnvelope.put(fieldName, fieldValue)) {
            org.apache.kafka.connect.data.Field field = (org.apache.kafka.connect.data.Field) var5.next();
            fieldName = field.name();
            fieldValue = oldEnvelope.get(field);
            if ((Objects.equals(fieldName, "before") || Objects.equals(fieldName, "after")) && fieldValue != null) {
                fieldValue = this.updateValue(newValueSchema, Requirements.requireStruct(fieldValue, "Updating schema"));
            }
        }

        return newEnvelope;
    }

    private Struct updateValue(Schema newValueSchema, Struct oldValue) {
        Struct newValue = new Struct(newValueSchema);
        Iterator var4 = oldValue.schema().fields().iterator();

        while (var4.hasNext()) {
            org.apache.kafka.connect.data.Field field = (org.apache.kafka.connect.data.Field) var4.next();
            newValue.put(field.name(), oldValue.get(field));
        }

        return newValue;
    }

    private SchemaBuilder copySchemaExcludingName(Schema source, SchemaBuilder builder) {
        return this.copySchemaExcludingName(source, builder, true);
    }

    private SchemaBuilder copySchemaExcludingName(Schema source, SchemaBuilder builder, boolean copyFields) {
        builder.version(source.version());
        builder.doc(source.doc());
        Map<String, String> params = source.parameters();
        if (params != null) {
            builder.parameters(params);
        }

        if (source.isOptional()) {
            builder.optional();
        } else {
            builder.required();
        }

        if (copyFields) {
            Iterator var5 = source.fields().iterator();

            while (var5.hasNext()) {
                org.apache.kafka.connect.data.Field field = (org.apache.kafka.connect.data.Field) var5.next();
                builder.field(field.name(), field.schema());
            }
        }

        return builder;
    }

    static {
        TOPIC_REGEX = Field.create("topic.regex").withDisplayName("Topic regex").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).required().withValidation(Field::isRegex).withDescription("The regex used for extracting the name of the logical table from the original topic name.");
        TOPIC_REPLACEMENT = Field.create("topic.replacement").withDisplayName("Topic replacement").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).required().withDescription("The replacement string used in conjunction with " + TOPIC_REGEX.name() + ". This will be used to create the new topic name.");
        KEY_ENFORCE_UNIQUENESS = Field.create("key.enforce.uniqueness").withDisplayName("Add source topic name into key").withType(Type.BOOLEAN).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault(true).withDescription("Augment each record's key with a field denoting the source topic. This field distinguishes records coming from different physical tables which may otherwise have primary/unique key conflicts. If the source tables are guaranteed to have globally unique keys then this may be set to false to disable key rewriting.");
        KEY_FIELD_REGEX = Field.create("key.field.regex").withDisplayName("Key field regex").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withValidation(Field::isRegex).withDescription("The regex used for extracting the physical table identifier from the original topic name. Now that multiple physical tables can share a topic, the event's key may need to be augmented to include fields other than just those for the record's primary/unique key, since these are not guaranteed to be unique across tables. We need some identifier added to the key that distinguishes the different physical tables.");
        KEY_FIELD_NAME = Field.create("key.field.name").withDisplayName("Key field name").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withDefault("__dbz__physicalTableIdentifier").withDescription("Each record's key schema will be augmented with this field name. The purpose of this field is to distinguish the different physical tables that can now share a single topic. Make sure not to configure a field name that is at risk of conflict with existing key schema field names.");
        KEY_FIELD_REPLACEMENT = Field.create("key.field.replacement").withDisplayName("Key field replacement").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withValidation(ByLogicalTableRouter::validateKeyFieldReplacement).withDescription("The replacement string used in conjunction with " + KEY_FIELD_REGEX.name() + ". This will be used to create the physical table identifier in the record's key.");
        SCHEMA_NAME_ADJUSTMENT_MODE = Field.create("schema.name.adjustment.mode").withDisplayName("Schema Name Adjustment").withEnum(CommonConnectorConfig.SchemaNameAdjustmentMode.class, CommonConnectorConfig.SchemaNameAdjustmentMode.NONE).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("Specify how the message key schema names derived from the resulting topic name should be adjusted for compatibility with the message converter used by the connector, including:'avro' replaces the characters that cannot be used in the Avro type name with underscore (default)'none' does not apply any adjustment");
        LOGICAL_TABLE_CACHE_SIZE = Field.create("logical.table.cache.size").withDisplayName("Logical table cache size").withType(Type.INT).withWidth(Width.LONG).withImportance(Importance.LOW).withDefault(16).withDescription("The size used for holding the max entries in LRUCache. The cache will keep the old/new schema for logical table key and value, also cache the derived key and topic regex result for improving the source record transformation.");
        LOGGER = LoggerFactory.getLogger(ByLogicalTableRouter.class);
    }
}
