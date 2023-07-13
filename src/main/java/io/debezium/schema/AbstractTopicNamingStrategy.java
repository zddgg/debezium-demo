package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.spi.common.ReplacementFunction;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.spi.topic.TopicNamingStrategy;
import io.debezium.util.BoundedConcurrentHashMap;
import io.debezium.util.Collect;
import io.debezium.util.Strings;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Incubating
public abstract class AbstractTopicNamingStrategy<I extends DataCollectionId> implements TopicNamingStrategy<I> {
    public static final String DEFAULT_HEARTBEAT_TOPIC_PREFIX = "__debezium-heartbeat";
    public static final String DEFAULT_TRANSACTION_TOPIC = "transaction";
    public static final Field TOPIC_DELIMITER;
    public static final Field TOPIC_CACHE_SIZE;
    public static final Field TOPIC_HEARTBEAT_PREFIX;
    public static final Field TOPIC_TRANSACTION;
    private static final Logger LOGGER;
    protected BoundedConcurrentHashMap<I, String> topicNames;
    protected String delimiter;
    protected String prefix;
    protected String transaction;
    protected String heartbeatPrefix;
    protected boolean multiPartitionMode;
    protected ReplacementFunction replacement;

    public AbstractTopicNamingStrategy(Properties props) {
        this.configure(props);
    }

    public void configure(Properties props) {
        Configuration config = Configuration.from(props);
        Field.Set configFields = Field.setOf(CommonConnectorConfig.TOPIC_PREFIX, TOPIC_DELIMITER, TOPIC_CACHE_SIZE, TOPIC_TRANSACTION, TOPIC_HEARTBEAT_PREFIX);
        Logger var10002 = LOGGER;
        Objects.requireNonNull(var10002);
        if (!config.validateAndRecord(configFields, var10002::error)) {
            throw new ConnectException("Unable to validate config.");
        } else {
            this.topicNames = new BoundedConcurrentHashMap(config.getInteger(TOPIC_CACHE_SIZE), 10, BoundedConcurrentHashMap.Eviction.LRU);
            this.delimiter = config.getString(TOPIC_DELIMITER);
            this.heartbeatPrefix = config.getString(TOPIC_HEARTBEAT_PREFIX);
            this.transaction = config.getString(TOPIC_TRANSACTION);
            this.prefix = config.getString(CommonConnectorConfig.TOPIC_PREFIX);

            assert this.prefix != null;

            this.multiPartitionMode = props.get("multi.partition.mode") == null ? false : Boolean.parseBoolean(props.get("multi.partition.mode").toString());
            this.replacement = ReplacementFunction.UNDERSCORE_REPLACEMENT;
        }
    }

    public abstract String dataChangeTopic(I var1);

    public String schemaChangeTopic() {
        return this.prefix;
    }

    public String heartbeatTopic() {
        return String.join(this.delimiter, this.heartbeatPrefix, this.prefix);
    }

    public String transactionTopic() {
        return String.join(this.delimiter, this.prefix, this.transaction);
    }

    public String sanitizedTopicName(String topicName) {
        StringBuilder sanitizedNameBuilder = new StringBuilder(topicName.length());
        boolean changed = false;

        for (int i = 0; i < topicName.length(); ++i) {
            char c = topicName.charAt(i);
            if (this.isValidCharacter(c)) {
                sanitizedNameBuilder.append(c);
            } else {
                sanitizedNameBuilder.append(this.replacement.replace(c));
                changed = true;
            }
        }

        String sanitizedName = sanitizedNameBuilder.toString();
        if (sanitizedName.length() > 249) {
            sanitizedName = sanitizedName.substring(0, 249);
            changed = true;
        } else if (sanitizedName.equals(".")) {
            sanitizedName = this.replacement.replace('.');
            changed = true;
        } else if (sanitizedName.equals("..")) {
            String replace = this.replacement.replace('.');
            sanitizedName = String.format("%s%s", replace, replace);
            changed = true;
        }

        if (changed) {
            LOGGER.warn("Topic '{}' name isn't a valid topic name, replacing it with '{}'.", topicName, sanitizedName);
            return sanitizedName;
        } else {
            return topicName;
        }
    }

    protected boolean isValidCharacter(char c) {
        return c == '.' || c == '_' || c == '-' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
    }

    protected String mkString(List<String> data, String delimiter) {
        return (String) data.stream().filter((f) -> {
            return !Strings.isNullOrBlank(f);
        }).collect(Collectors.joining(delimiter));
    }

    protected String getSchemaPartsTopicName(DataCollectionId id) {
        String topicName;
        if (this.multiPartitionMode) {
            topicName = this.mkString(Collect.arrayListOf(this.prefix, (List) id.parts()), this.delimiter);
        } else {
            topicName = this.mkString(Collect.arrayListOf(this.prefix, (List) id.schemaParts()), this.delimiter);
        }

        return topicName;
    }

    static {
        TOPIC_DELIMITER = Field.create("topic.delimiter").withDisplayName("Topic delimiter").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault(".").withValidation(CommonConnectorConfig::validateTopicName).withDescription("Specify the delimiter for topic name.");
        TOPIC_CACHE_SIZE = Field.create("topic.cache.size").withDisplayName("Topic cache size").withType(Type.INT).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault(10000).withDescription("The size used for holding the topic names in bounded concurrent hash map. The cache will help to determine the topic name corresponding to a given data collection");
        TOPIC_HEARTBEAT_PREFIX = Field.create("topic.heartbeat.prefix").withDisplayName("Prefix name of heartbeat topic").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("__debezium-heartbeat").withValidation(CommonConnectorConfig::validateTopicName).withDescription("Specify the heartbeat topic name. Defaults to __debezium-heartbeat.${topic.prefix}");
        TOPIC_TRANSACTION = Field.create("topic.transaction").withDisplayName("Transaction topic name").withType(Type.STRING).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDefault("transaction").withValidation(CommonConnectorConfig::validateTopicName).withDescription("Specify the transaction topic name. Defaults to ${topic.prefix}.transaction");
        LOGGER = LoggerFactory.getLogger(AbstractTopicNamingStrategy.class);
    }
}
