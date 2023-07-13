package io.debezium.connector.mysql;

import io.debezium.connector.base.ChangeEventQueue;
import io.debezium.pipeline.ErrorHandler;
import io.debezium.util.Collect;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class MySqlErrorHandler extends ErrorHandler {
    public MySqlErrorHandler(MySqlConnectorConfig connectorConfig, ChangeEventQueue<?> queue) {
        super(MySqlConnector.class, connectorConfig, queue);
    }

    protected Set<Class<? extends Exception>> communicationExceptions() {
        return Collect.unmodifiableSet(new Class[]{IOException.class, SQLException.class});
    }
}
