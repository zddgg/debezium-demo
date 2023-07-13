package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Properties;

@Incubating
public class SchemaTopicNamingStrategy extends AbstractTopicNamingStrategy<DataCollectionId> {
    public SchemaTopicNamingStrategy(Properties props) {
        super(props);
    }

    public SchemaTopicNamingStrategy(Properties props, boolean multiPartitionMode) {
        super(props);
        this.multiPartitionMode = multiPartitionMode;
    }

    public static SchemaTopicNamingStrategy create(CommonConnectorConfig config) {
        return create(config, false);
    }

    public static SchemaTopicNamingStrategy create(CommonConnectorConfig config, boolean multiPartitionMode) {
        return new SchemaTopicNamingStrategy(config.getConfig().asProperties(), multiPartitionMode);
    }

    public String dataChangeTopic(DataCollectionId id) {
        return (String) this.topicNames.computeIfAbsent(id, (t) -> {
            return this.sanitizedTopicName(this.getSchemaPartsTopicName(id));
        });
    }
}
