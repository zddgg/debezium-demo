package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Collect;

import java.util.List;
import java.util.Properties;

@Incubating
public class DefaultUnicodeTopicNamingStrategy extends AbstractUnicodeTopicNamingStrategy {
    public DefaultUnicodeTopicNamingStrategy(Properties props) {
        super(props);
    }

    public static DefaultUnicodeTopicNamingStrategy create(CommonConnectorConfig config) {
        return new DefaultUnicodeTopicNamingStrategy(config.getConfig().asProperties());
    }

    public String dataChangeTopic(DataCollectionId id) {
        String topicName = this.mkString(Collect.arrayListOf(this.prefix, (List) id.databaseParts()), this.delimiter);
        return (String) this.topicNames.computeIfAbsent(id, (t) -> {
            return this.sanitizedTopicName(topicName);
        });
    }
}
