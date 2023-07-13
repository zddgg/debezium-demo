package io.debezium.heartbeat;

import io.debezium.jdbc.JdbcConnection;

public interface HeartbeatConnectionProvider {
    JdbcConnection get();
}
