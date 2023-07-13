package io.debezium.pipeline.notification.channels.jmx;

import io.debezium.pipeline.notification.Notification;

import java.util.List;

public interface JmxNotificationChannelMXBean {
    List<Notification> getNotifications();

    void reset();
}
