package io.debezium.pipeline.notification.channels;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.notification.Notification;

public interface NotificationChannel {
    void init(CommonConnectorConfig var1);

    String name();

    void send(Notification var1);

    void close();
}
