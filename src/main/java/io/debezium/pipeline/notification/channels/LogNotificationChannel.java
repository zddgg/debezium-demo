package io.debezium.pipeline.notification.channels;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogNotificationChannel implements NotificationChannel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogNotificationChannel.class);
    private static final String LOG_PREFIX = "[Notification Service] ";
    public static final String CHANNEL_NAME = "log";

    public void init(CommonConnectorConfig config) {
    }

    public String name() {
        return "log";
    }

    public void send(Notification notification) {
        LOGGER.info("{} {}", "[Notification Service] ", notification);
    }

    public void close() {
    }
}
