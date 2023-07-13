package io.debezium.transforms.partitions;

import io.debezium.DebeziumException;

/**
 * @deprecated
 */
@Deprecated
public class ComputePartitionException extends DebeziumException {
    private static final long serialVersionUID = -1317267303694502915L;

    public ComputePartitionException() {
    }

    public ComputePartitionException(String message) {
        super(message);
    }

    public ComputePartitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComputePartitionException(Throwable cause) {
        super(cause);
    }

    protected ComputePartitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
