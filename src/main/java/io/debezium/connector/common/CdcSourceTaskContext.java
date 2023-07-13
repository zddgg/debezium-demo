package io.debezium.connector.common;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Clock;
import io.debezium.util.LoggingContext;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

public class CdcSourceTaskContext {
    private final String connectorType;
    private final String connectorName;
    private final String taskId;
    private final Clock clock;
    private final Supplier<Collection<? extends DataCollectionId>> collectionsSupplier;

    public CdcSourceTaskContext(String connectorType, String connectorName, String taskId, Supplier<Collection<? extends DataCollectionId>> collectionsSupplier) {
        this.connectorType = connectorType;
        this.connectorName = connectorName;
        this.taskId = taskId;
        this.collectionsSupplier = collectionsSupplier != null ? collectionsSupplier : Collections::emptyList;
        this.clock = Clock.system();
    }

    public CdcSourceTaskContext(String connectorType, String connectorName, Supplier<Collection<? extends DataCollectionId>> collectionsSupplier) {
        this(connectorType, connectorName, "0", collectionsSupplier);
    }

    public LoggingContext.PreviousContext configureLoggingContext(String contextName) {
        return LoggingContext.forConnector(this.connectorType, this.connectorName, contextName);
    }

    public LoggingContext.PreviousContext configureLoggingContext(String contextName, Partition partition) {
        return LoggingContext.forConnector(this.connectorType, this.connectorName, this.taskId, contextName, partition);
    }

    public void temporaryLoggingContext(CommonConnectorConfig connectorConfig, String contextName, Runnable operation) {
        LoggingContext.temporarilyForConnector("MySQL", connectorConfig.getLogicalName(), contextName, operation);
    }

    public Clock getClock() {
        return this.clock;
    }

    public String[] capturedDataCollections() {
        return (String[]) ((Collection) this.collectionsSupplier.get()).stream().map(Object::toString).toArray((x$0) -> {
            return new String[x$0];
        });
    }

    public String getConnectorType() {
        return this.connectorType;
    }

    public String getConnectorName() {
        return this.connectorName;
    }

    public String getTaskId() {
        return this.taskId;
    }
}
