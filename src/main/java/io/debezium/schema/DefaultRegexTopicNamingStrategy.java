package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Collect;

import java.util.List;
import java.util.Properties;

@Incubating
public class DefaultRegexTopicNamingStrategy extends AbstractRegexTopicNamingStrategy {
    public DefaultRegexTopicNamingStrategy(Properties props) {
        super(props);
    }

    public String getOriginTopic(DataCollectionId id) {
        return this.mkString(Collect.arrayListOf(this.prefix, (List) id.databaseParts()), this.delimiter);
    }
}
