package io.debezium.pipeline.notification.channels.jmx;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.JmxUtils;
import io.debezium.pipeline.notification.Notification;
import io.debezium.pipeline.notification.channels.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class JmxNotificationChannel extends NotificationBroadcasterSupport implements NotificationChannel, JmxNotificationChannelMXBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JmxNotificationChannel.class);
    private static final String CHANNEL_NAME = "jmx";
    private static final String DEBEZIUM_NOTIFICATION_TYPE = "debezium.notification";
    private static final List<Notification> NOTIFICATIONS = new ArrayList();
    private final AtomicLong notificationSequence = new AtomicLong(0L);
    private CommonConnectorConfig connectorConfig;

    public String name() {
        return "jmx";
    }

    public void init(CommonConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
        JmxUtils.registerMXBean(this, connectorConfig, "management", "notifications");
        LOGGER.info("Registration for Notification MXBean with the platform server is successfully");
    }

    public void send(Notification notification) {
        NOTIFICATIONS.add(notification);
        this.sendNotification(this.buildJmxNotification(notification));
    }

    private javax.management.Notification buildJmxNotification(Notification notification) {
        javax.management.Notification n = new javax.management.Notification("debezium.notification", this, this.notificationSequence.getAndIncrement(), System.currentTimeMillis(), this.composeMessage(notification));
        n.setUserData(notification.toString());
        return n;
    }

    private String composeMessage(Notification notification) {
        return String.format("%s generated a notification", notification.getAggregateType());
    }

    public void close() {
        JmxUtils.unregisterMXBean(this.connectorConfig, "management", "notifications");
    }

    public List<Notification> getNotifications() {
        return NOTIFICATIONS;
    }

    public void reset() {
        NOTIFICATIONS.clear();
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{"debezium.notification"};
        String name = Notification.class.getName();
        String description = "Notification emitted by Debezium about its status";
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }
}
