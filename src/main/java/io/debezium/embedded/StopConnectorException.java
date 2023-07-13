package io.debezium.embedded;

import io.debezium.DebeziumException;

/**
 * @deprecated
 */
@Deprecated
public class StopConnectorException extends DebeziumException {
    private static final long serialVersionUID = 1L;

    public StopConnectorException(String msg) {
        super(msg);
    }
}
