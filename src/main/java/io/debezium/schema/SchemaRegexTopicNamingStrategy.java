package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Properties;

@Incubating
public class SchemaRegexTopicNamingStrategy extends AbstractRegexTopicNamingStrategy {
    public SchemaRegexTopicNamingStrategy(Properties props) {
        super(props);
    }

    public String getOriginTopic(DataCollectionId id) {
        return this.getSchemaPartsTopicName(id);
    }
}
