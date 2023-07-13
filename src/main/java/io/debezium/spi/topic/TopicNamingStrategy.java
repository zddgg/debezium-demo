package io.debezium.spi.topic;

import io.debezium.common.annotation.Incubating;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Properties;

@Incubating
public interface TopicNamingStrategy<I extends DataCollectionId> {
    int MAX_NAME_LENGTH = 249;
    TopicSchemaAugment NO_SCHEMA_OP = (schemaBuilder) -> {
        return false;
    };
    TopicValueAugment NO_VALUE_OP = (id, schema, struct) -> {
        return false;
    };

    void configure(Properties var1);

    String dataChangeTopic(I var1);

    String schemaChangeTopic();

    String heartbeatTopic();

    String transactionTopic();

    String sanitizedTopicName(String var1);

    default TopicSchemaAugment keySchemaAugment() {
        return NO_SCHEMA_OP;
    }

    default TopicValueAugment keyValueAugment() {
        return NO_VALUE_OP;
    }

    public interface TopicSchemaAugment<S> {
        boolean augment(S var1);
    }

    public interface TopicValueAugment<I extends DataCollectionId, S, R> {
        boolean augment(I var1, S var2, R var3);
    }
}
