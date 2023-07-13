package io.debezium.heartbeat;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.schema.SchemaNameAdjuster;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.spi.topic.TopicNamingStrategy;

public class HeartbeatFactory<T extends DataCollectionId> {
    private final CommonConnectorConfig connectorConfig;
    private final TopicNamingStrategy<T> topicNamingStrategy;
    private final SchemaNameAdjuster schemaNameAdjuster;
    private final HeartbeatConnectionProvider connectionProvider;
    private final HeartbeatErrorHandler errorHandler;

    public HeartbeatFactory(CommonConnectorConfig connectorConfig, TopicNamingStrategy<T> topicNamingStrategy, SchemaNameAdjuster schemaNameAdjuster) {
        this(connectorConfig, topicNamingStrategy, schemaNameAdjuster, (HeartbeatConnectionProvider) null, (HeartbeatErrorHandler) null);
    }

    public HeartbeatFactory(CommonConnectorConfig connectorConfig, TopicNamingStrategy<T> topicNamingStrategy, SchemaNameAdjuster schemaNameAdjuster, HeartbeatConnectionProvider connectionProvider, HeartbeatErrorHandler errorHandler) {
        this.connectorConfig = connectorConfig;
        this.topicNamingStrategy = topicNamingStrategy;
        this.schemaNameAdjuster = schemaNameAdjuster;
        this.connectionProvider = connectionProvider;
        this.errorHandler = errorHandler;
    }

    public Heartbeat createHeartbeat() {
        if (this.connectorConfig.getHeartbeatInterval().isZero()) {
            return Heartbeat.DEFAULT_NOOP_HEARTBEAT;
        } else {
            if (this.connectorConfig instanceof RelationalDatabaseConnectorConfig) {
                RelationalDatabaseConnectorConfig relConfig = (RelationalDatabaseConnectorConfig) this.connectorConfig;
                if (relConfig.getHeartbeatActionQuery() != null) {
                    return new DatabaseHeartbeatImpl(this.connectorConfig.getHeartbeatInterval(), this.topicNamingStrategy.heartbeatTopic(), this.connectorConfig.getLogicalName(), this.connectionProvider.get(), relConfig.getHeartbeatActionQuery(), this.errorHandler, this.schemaNameAdjuster);
                }
            }

            return new HeartbeatImpl(this.connectorConfig.getHeartbeatInterval(), this.topicNamingStrategy.heartbeatTopic(), this.connectorConfig.getLogicalName(), this.schemaNameAdjuster);
        }
    }
}
