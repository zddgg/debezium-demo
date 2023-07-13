package io.debezium.pipeline.signal.actions;

import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.spi.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log<P extends Partition> implements SignalAction<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log.class);
    private static final String FIELD_MESSAGE = "message";
    public static final String NAME = "log";

    public boolean arrived(SignalPayload<P> signalPayload) {
        String message = signalPayload.data.getString("message");
        if (message != null && !message.isEmpty()) {
            LOGGER.info(message, signalPayload.offsetContext != null ? signalPayload.offsetContext.getOffset() : "<none>");
            return true;
        } else {
            LOGGER.warn("Logging signal '{}' has arrived but the requested field '{}' is missing from data", signalPayload, "message");
            return false;
        }
    }
}
