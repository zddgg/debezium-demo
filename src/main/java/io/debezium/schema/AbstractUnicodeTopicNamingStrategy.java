package io.debezium.schema;

import io.debezium.common.annotation.Incubating;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Properties;

@Incubating
public abstract class AbstractUnicodeTopicNamingStrategy extends AbstractTopicNamingStrategy<DataCollectionId> {
    public AbstractUnicodeTopicNamingStrategy(Properties props) {
        super(props);
        this.replacement = new UnicodeReplacementFunction();
    }

    public boolean isValidCharacter(char c) {
        return c == '.' || c == '-' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9';
    }
}
