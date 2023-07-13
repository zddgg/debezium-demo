package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.BoundedConcurrentHashMap;
import io.debezium.util.Strings;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Incubating
public abstract class AbstractRegexTopicNamingStrategy extends AbstractTopicNamingStrategy<DataCollectionId> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegexTopicNamingStrategy.class);
    public static final Field TOPIC_REGEX;
    public static final Field TOPIC_REPLACEMENT;
    public static final Field TOPIC_KEY_ENFORCE_UNIQUENESS;
    public static final Field TOPIC_KEY_FIELD_NAME;
    public static final Field TOPIC_KEY_FIELD_REGEX;
    public static final Field TOPIC_KEY_FIELD_REPLACEMENT;
    private Pattern topicRegex;
    private String topicReplacement;
    private boolean keyEnforceUniqueness;
    private String keyFieldName;
    private Pattern keyFieldRegex;
    private String keyFieldReplacement;
    private BoundedConcurrentHashMap<String, String> keyRegexReplaceCache;

    private static int validateTopicReplacement(Configuration config, Field field, Field.ValidationOutput problems) {
        String topicRegex = config.getString(TOPIC_REGEX);
        if (topicRegex != null) {
            topicRegex = topicRegex.trim();
        }

        String topicReplacement = config.getString(TOPIC_REPLACEMENT);
        if (topicReplacement != null) {
            topicReplacement = topicReplacement.trim();
        }

        if (!Strings.isNullOrEmpty(topicRegex) && Strings.isNullOrEmpty(topicReplacement)) {
            problems.accept(TOPIC_REPLACEMENT, (Object) null, String.format("%s must be non-empty if %s is set.", TOPIC_REPLACEMENT.name(), TOPIC_REGEX.name()));
            return 1;
        } else {
            return 0;
        }
    }

    private static int validateKeyFieldReplacement(Configuration config, Field field, Field.ValidationOutput problems) {
        String keyFieldRegex = config.getString(TOPIC_KEY_FIELD_REGEX);
        if (keyFieldRegex != null) {
            keyFieldRegex = keyFieldRegex.trim();
        }

        String keyFieldReplacement = config.getString(TOPIC_KEY_FIELD_REPLACEMENT);
        if (keyFieldReplacement != null) {
            keyFieldReplacement = keyFieldReplacement.trim();
        }

        if (!Strings.isNullOrEmpty(keyFieldRegex) && Strings.isNullOrEmpty(keyFieldReplacement)) {
            problems.accept(TOPIC_KEY_FIELD_REPLACEMENT, (Object) null, String.format("%s must be non-empty if %s is set.", TOPIC_KEY_FIELD_REPLACEMENT.name(), TOPIC_KEY_FIELD_REGEX.name()));
            return 1;
        } else {
            return 0;
        }
    }

    public AbstractRegexTopicNamingStrategy(Properties props) {
        super(props);
    }

    public void configure(Properties props) {
        super.configure(props);
        Configuration config = Configuration.from(props);
        Field.Set regexConfigFields = Field.setOf(TOPIC_REGEX, TOPIC_REPLACEMENT, TOPIC_KEY_ENFORCE_UNIQUENESS, TOPIC_KEY_FIELD_NAME, TOPIC_KEY_FIELD_REGEX, TOPIC_KEY_FIELD_REPLACEMENT);
        Logger var10002 = LOGGER;
        Objects.requireNonNull(var10002);
        if (!config.validateAndRecord(regexConfigFields, var10002::error)) {
            throw new ConnectException("Unable to validate config.");
        } else {
            this.topicRegex = Pattern.compile(config.getString(TOPIC_REGEX));
            this.topicReplacement = config.getString(TOPIC_REPLACEMENT);
            this.keyEnforceUniqueness = config.getBoolean(TOPIC_KEY_ENFORCE_UNIQUENESS);
            this.keyFieldName = config.getString(TOPIC_KEY_FIELD_NAME);
            String keyFieldRegexString = config.getString(TOPIC_KEY_FIELD_REGEX);
            if (keyFieldRegexString != null) {
                keyFieldRegexString = keyFieldRegexString.trim();
            }

            if (!Strings.isNullOrBlank(keyFieldRegexString)) {
                this.keyFieldRegex = Pattern.compile(config.getString(TOPIC_KEY_FIELD_REGEX));
                this.keyFieldReplacement = config.getString(TOPIC_KEY_FIELD_REPLACEMENT);
            }

            this.keyRegexReplaceCache = new BoundedConcurrentHashMap(config.getInteger(TOPIC_CACHE_SIZE), 10, BoundedConcurrentHashMap.Eviction.LRU);
        }
    }

    public String dataChangeTopic(DataCollectionId id) {
        String oldTopic = this.getOriginTopic(id);
        return this.determineNewTopic(id, this.sanitizedTopicName(oldTopic));
    }

    public abstract String getOriginTopic(DataCollectionId var1);

    protected String determineNewTopic(DataCollectionId tableId, String oldTopic) {
        String newTopic = (String) this.topicNames.get(tableId);
        if (newTopic == null) {
            newTopic = oldTopic;
            Matcher matcher = this.topicRegex.matcher(oldTopic);
            if (matcher.matches()) {
                newTopic = matcher.replaceFirst(this.topicReplacement);
                if (newTopic.isEmpty()) {
                    LOGGER.warn("Routing regex returned an empty topic name, propagating original topic");
                    newTopic = oldTopic;
                }
            }

            this.topicNames.put(tableId, newTopic);
        }

        return newTopic;
    }

    public TopicSchemaAugment<SchemaBuilder> keySchemaAugment() {
        return (schemaBuilder) -> {
            if (this.keyEnforceUniqueness) {
                schemaBuilder.field(this.keyFieldName, Schema.STRING_SCHEMA);
                return true;
            } else {
                return false;
            }
        };
    }

    public TopicValueAugment<DataCollectionId, Schema, Struct> keyValueAugment() {
        return (id, schema, struct) -> {
            if (this.keyEnforceUniqueness) {
                String oldTopic = this.getOriginTopic(id);
                String physicalTableIdentifier = oldTopic;
                if (this.keyFieldRegex != null) {
                    physicalTableIdentifier = (String) this.keyRegexReplaceCache.get(oldTopic);
                    if (physicalTableIdentifier == null) {
                        Matcher matcher = this.keyFieldRegex.matcher(oldTopic);
                        if (matcher.matches()) {
                            physicalTableIdentifier = matcher.replaceFirst(this.keyFieldReplacement);
                        } else {
                            physicalTableIdentifier = oldTopic;
                        }

                        this.keyRegexReplaceCache.put(oldTopic, physicalTableIdentifier);
                    }
                }

                struct.put(schema.field(this.keyFieldName), physicalTableIdentifier);
                return true;
            } else {
                return false;
            }
        };
    }

    static {
        TOPIC_REGEX = Field.create("topic.regex").withDisplayName("Topic regex").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).required().withValidation(Field::isRegex).withDescription("The regex used for extracting the name of the logical table from the original topic name.");
        TOPIC_REPLACEMENT = Field.create("topic.replacement").withDisplayName("Topic replacement").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).required().withValidation(AbstractRegexTopicNamingStrategy::validateTopicReplacement).withDescription("The replacement string used in conjunction with " + TOPIC_REGEX.name() + ". This will be used to create the new topic name.");
        TOPIC_KEY_ENFORCE_UNIQUENESS = Field.create("topic.key.enforce.uniqueness").withDisplayName("Add source topic name into key").withType(Type.BOOLEAN).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault(true).withDescription("Augment each record's key with a field denoting the source topic. This field distinguishes records coming from different physical tables which may otherwise have primary/unique key conflicts. If the source tables are guaranteed to have globally unique keys then this may be set to false to disable key rewriting.");
        TOPIC_KEY_FIELD_NAME = Field.create("topic.key.field.name").withDisplayName("Key field name").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withDefault("__dbz__physicalTableIdentifier").withDescription("Each record's key schema will be augmented with this field name. The purpose of this field is to distinguish the different physical tables that can now share a single topic. Make sure not to configure a field name that is at risk of conflict with existing key schema field names.");
        TOPIC_KEY_FIELD_REGEX = Field.create("topic.key.field.regex").withDisplayName("Key field regex").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withValidation(Field::isRegex).withDescription("The regex used for extracting the physical table identifier from the original topic name. Now that multiple physical tables can share a topic, the event's key may need to be augmented to include fields other than just those for the record's primary/unique key, since these are not guaranteed to be unique across tables. We need some identifier added to the key that distinguishes the different physical tables.");
        TOPIC_KEY_FIELD_REPLACEMENT = Field.create("topic.key.field.replacement").withDisplayName("Key field replacement").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withValidation(AbstractRegexTopicNamingStrategy::validateKeyFieldReplacement).withDescription("The replacement string used in conjunction with " + TOPIC_KEY_FIELD_REGEX.name() + ". This will be used to create the physical table identifier in the record's key.");
    }
}
