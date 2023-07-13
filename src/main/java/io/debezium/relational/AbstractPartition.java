package io.debezium.relational;

import io.debezium.pipeline.spi.Partition;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractPartition implements Partition {
    protected final String databaseName;

    public AbstractPartition(String databaseName) {
        this.databaseName = databaseName;
    }

    public Map<String, String> getLoggingContext() {
        return Collections.singletonMap("dbz.databaseName", this.databaseName);
    }
}
