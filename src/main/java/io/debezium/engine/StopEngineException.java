package io.debezium.engine;

import io.debezium.DebeziumException;

public class StopEngineException extends DebeziumException {
    private static final long serialVersionUID = 1L;

    public StopEngineException(String msg) {
        super(msg);
    }
}
