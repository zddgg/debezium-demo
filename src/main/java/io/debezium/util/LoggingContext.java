package io.debezium.util;

import io.debezium.pipeline.spi.Partition;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class LoggingContext {
    public static final String CONNECTOR_TYPE = "dbz.connectorType";
    public static final String CONNECTOR_NAME = "dbz.connectorName";
    public static final String CONNECTOR_CONTEXT = "dbz.connectorContext";
    public static final String TASK_ID = "dbz.taskId";
    public static final String DATABASE_NAME = "dbz.databaseName";

    private LoggingContext() {
    }

    public static PreviousContext forConnector(String connectorType, String connectorName, String contextName) {
        return forConnector(connectorType, connectorName, (String) null, contextName, (Partition) null);
    }

    public static PreviousContext forConnector(String connectorType, String connectorName, String taskId, String contextName, Partition partition) {
        Objects.requireNonNull(connectorType, "The MDC value for the connector type may not be null");
        Objects.requireNonNull(connectorName, "The MDC value for the connector name may not be null");
        Objects.requireNonNull(contextName, "The MDC value for the connector context may not be null");
        PreviousContext previous = new PreviousContext();
        if (taskId != null) {
            MDC.put("dbz.taskId", taskId);
        }

        if (partition != null && partition.getLoggingContext() != null) {
            partition.getLoggingContext().forEach((k, v) -> {
                if (k != null && v != null) {
                    MDC.put(k, v);
                }

            });
        }

        MDC.put("dbz.connectorType", connectorType);
        MDC.put("dbz.connectorName", connectorName);
        MDC.put("dbz.connectorContext", contextName);
        return previous;
    }

    public static void temporarilyForConnector(String connectorType, String connectorName, String contextName, Runnable operation) {
        Objects.requireNonNull(connectorType, "The MDC value for the connector type may not be null");
        Objects.requireNonNull(connectorName, "The MDC value for the connector name may not be null");
        Objects.requireNonNull(contextName, "The MDC value for the connector context may not be null");
        Objects.requireNonNull(operation, "The operation may not be null");
        PreviousContext previous = new PreviousContext();

        try {
            forConnector(connectorType, connectorName, contextName);
            operation.run();
        } finally {
            previous.restore();
        }

    }

    public static final class PreviousContext {
        private static final Map<String, String> EMPTY_CONTEXT = Collections.emptyMap();
        private final Map<String, String> context;

        protected PreviousContext() {
            Map<String, String> context = MDC.getCopyOfContextMap();
            this.context = context != null ? context : EMPTY_CONTEXT;
        }

        public void restore() {
            MDC.setContextMap(this.context);
        }
    }
}
