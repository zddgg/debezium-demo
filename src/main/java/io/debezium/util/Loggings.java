package io.debezium.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Loggings.class);

    public static void logWarningAndTraceRecord(Logger logger, Object record, String message, Object... arguments) {
        logger.warn(message, arguments);
        LOGGER.trace("Source of warning is record '{}'", record);
    }

    public static void logDebugAndTraceRecord(Logger logger, Object record, String message, Object... arguments) {
        logger.debug(message, arguments);
        LOGGER.trace("Source of debug is record '{}'", record);
    }

    public static void logErrorAndTraceRecord(Logger logger, Object record, String message, Object... arguments) {
        logger.error(message, arguments);
        LOGGER.trace("Source of error is record '{}'", record);
    }

    public static void logErrorAndTraceRecord(Logger logger, Object record, String message, Throwable t) {
        logger.error(message, t);
        LOGGER.trace("Source of error is record '{}'", record);
    }
}
