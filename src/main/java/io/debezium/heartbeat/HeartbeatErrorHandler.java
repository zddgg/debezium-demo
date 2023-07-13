package io.debezium.heartbeat;

import java.sql.SQLException;

public interface HeartbeatErrorHandler {
    HeartbeatErrorHandler DEFAULT_NOOP_ERRORHANDLER = (exception) -> {
    };

    void onError(SQLException var1) throws RuntimeException;
}
