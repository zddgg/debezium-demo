package io.debezium.connector.mysql.metadata;

import io.debezium.metadata.ConnectorMetadata;
import io.debezium.metadata.ConnectorMetadataProvider;

public class MySqlConnectorMetadataProvider implements ConnectorMetadataProvider {
    public ConnectorMetadata getConnectorMetadata() {
        return new MySqlConnectorMetadata();
    }
}
