package io.debezium.metadata;

import io.debezium.config.Field;

public interface ConnectorMetadata {
    ConnectorDescriptor getConnectorDescriptor();

    Field.Set getConnectorFields();
}
