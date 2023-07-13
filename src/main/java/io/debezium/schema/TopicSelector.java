package io.debezium.schema;

import io.debezium.annotation.ThreadSafe;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.BoundedConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated
 */
@Deprecated
public class TopicSelector<I extends DataCollectionId> {
    private final String prefix;
    private final String heartbeatPrefix;
    private final String delimiter;
    private final DataCollectionTopicNamer<I> dataCollectionTopicNamer;

    private TopicSelector(String prefix, String heartbeatPrefix, String delimiter, DataCollectionTopicNamer<I> dataCollectionTopicNamer) {
        this.prefix = prefix;
        this.heartbeatPrefix = heartbeatPrefix;
        this.delimiter = delimiter;
        this.dataCollectionTopicNamer = new TopicNameCache(new TopicNameSanitizer(dataCollectionTopicNamer));
    }

    public static <I extends DataCollectionId> TopicSelector<I> defaultSelector(String prefix, String heartbeatPrefix, String delimiter, DataCollectionTopicNamer<I> dataCollectionTopicNamer) {
        return new TopicSelector(prefix, heartbeatPrefix, delimiter, dataCollectionTopicNamer);
    }

    public static <I extends DataCollectionId> TopicSelector<I> defaultSelector(CommonConnectorConfig connectorConfig, DataCollectionTopicNamer<I> dataCollectionTopicNamer) {
        String prefix = connectorConfig.getLogicalName();
        String heartbeatTopicsPrefix = connectorConfig.getHeartbeatTopicsPrefix();
        String delimiter = ".";
        return defaultSelector(prefix, heartbeatTopicsPrefix, delimiter, dataCollectionTopicNamer);
    }

    public String topicNameFor(I id) {
        return this.dataCollectionTopicNamer.topicNameFor(id, this.prefix, this.delimiter);
    }

    public String getPrimaryTopic() {
        return this.prefix;
    }

    public String getHeartbeatTopic() {
        return String.join(this.delimiter, this.heartbeatPrefix, this.prefix);
    }

    @ThreadSafe
    private static class TopicNameCache<I extends DataCollectionId> implements DataCollectionTopicNamer<I> {
        private final BoundedConcurrentHashMap<I, String> topicNames;
        private final DataCollectionTopicNamer<I> delegate;

        TopicNameCache(DataCollectionTopicNamer<I> delegate) {
            this.topicNames = new BoundedConcurrentHashMap(10000, 10, BoundedConcurrentHashMap.Eviction.LRU);
            this.delegate = delegate;
        }

        public String topicNameFor(I id, String prefix, String delimiter) {
            return (String) this.topicNames.computeIfAbsent(id, (i) -> {
                return this.delegate.topicNameFor(i, prefix, delimiter);
            });
        }
    }

    private static class TopicNameSanitizer<I extends DataCollectionId> implements DataCollectionTopicNamer<I> {
        private static final Logger LOGGER = LoggerFactory.getLogger(TopicNameSanitizer.class);
        private static final String REPLACEMENT_CHAR = "_";
        private final DataCollectionTopicNamer<I> delegate;

        TopicNameSanitizer(DataCollectionTopicNamer<I> delegate) {
            this.delegate = delegate;
        }

        public String topicNameFor(I id, String prefix, String delimiter) {
            String topicName = this.delegate.topicNameFor(id, prefix, delimiter);
            StringBuilder sanitizedNameBuilder = new StringBuilder(topicName.length());
            boolean changed = false;

            for (int i = 0; i < topicName.length(); ++i) {
                char c = topicName.charAt(i);
                if (this.isValidTopicNameCharacter(c)) {
                    sanitizedNameBuilder.append(c);
                } else {
                    sanitizedNameBuilder.append("_");
                    changed = true;
                }
            }

            if (changed) {
                String sanitizedName = sanitizedNameBuilder.toString();
                LOGGER.warn("Topic '{}' name isn't a valid topic name, replacing it with '{}'.", topicName, sanitizedName);
                return sanitizedName;
            } else {
                return topicName;
            }
        }

        private boolean isValidTopicNameCharacter(char c) {
            return c == '.' || c == '_' || c == '-' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
        }
    }

    @FunctionalInterface
    public interface DataCollectionTopicNamer<I extends DataCollectionId> {
        String topicNameFor(I var1, String var2, String var3);
    }
}
