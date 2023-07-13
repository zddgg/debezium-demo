package io.debezium;

public class DebeziumException extends RuntimeException {
    private static final long serialVersionUID = -829914184849944524L;

    public DebeziumException() {
    }

    public DebeziumException(String message) {
        super(message);
    }

    public DebeziumException(Throwable cause) {
        super(cause);
    }

    public DebeziumException(String message, Throwable cause) {
        super(message, cause);
    }

    public DebeziumException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
