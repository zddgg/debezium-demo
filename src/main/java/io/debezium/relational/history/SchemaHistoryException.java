package io.debezium.relational.history;

public class SchemaHistoryException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SchemaHistoryException(String message) {
        super(message);
    }

    public SchemaHistoryException(Throwable cause) {
        super(cause);
    }

    public SchemaHistoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaHistoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
