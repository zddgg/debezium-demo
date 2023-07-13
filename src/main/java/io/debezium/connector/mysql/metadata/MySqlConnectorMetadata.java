package io.debezium.connector.mysql.metadata;

import io.debezium.config.Field;
import io.debezium.connector.mysql.Module;
import io.debezium.connector.mysql.MySqlConnector;
import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.metadata.ConnectorDescriptor;
import io.debezium.metadata.ConnectorMetadata;

public class MySqlConnectorMetadata implements ConnectorMetadata {
    public ConnectorDescriptor getConnectorDescriptor() {
        return new ConnectorDescriptor("mysql", "Debezium MySQL Connector", MySqlConnector.class.getName(), Module.version());
    }

    public Field.Set getConnectorFields() {
        return MySqlConnectorConfig.ALL_FIELDS;
    }
}
