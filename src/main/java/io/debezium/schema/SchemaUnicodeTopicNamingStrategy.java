package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Properties;

@Incubating
public class SchemaUnicodeTopicNamingStrategy extends AbstractUnicodeTopicNamingStrategy {
    public SchemaUnicodeTopicNamingStrategy(Properties props) {
        super(props);
    }

    public static SchemaUnicodeTopicNamingStrategy create(CommonConnectorConfig config) {
        return new SchemaUnicodeTopicNamingStrategy(config.getConfig().asProperties());
    }

    public String dataChangeTopic(DataCollectionId id) {
        return (String) this.topicNames.computeIfAbsent(id, (t) -> {
            return this.sanitizedTopicName(this.getSchemaPartsTopicName(id));
        });
    }
}
