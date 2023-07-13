package io.debezium.pipeline.notification;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.function.BlockingConsumer;
import io.debezium.function.Predicates;
import io.debezium.pipeline.notification.channels.ConnectChannel;
import io.debezium.pipeline.notification.channels.NotificationChannel;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
import java.util.function.Predicate;

public class NotificationService<P extends Partition, O extends OffsetContext> {
    private final List<NotificationChannel> notificationChannels;
    private final List<String> enabledChannels;
    private final IncrementalSnapshotNotificationService<P, O> incrementalSnapshotNotificationService;

    public NotificationService(List<NotificationChannel> notificationChannels, CommonConnectorConfig config, SchemaFactory schemaFactory, BlockingConsumer<SourceRecord> consumer) {
        this.notificationChannels = notificationChannels;
        this.enabledChannels = config.getEnabledNotificationChannels();
        this.notificationChannels.stream().filter(this.isEnabled()).forEach((channel) -> {
            channel.init(config);
        });
        this.notificationChannels.stream().filter(this.isConnectChannel()).forEach((channel) -> {
            ((ConnectChannel) channel).initConnectChannel(schemaFactory, consumer);
        });
        this.incrementalSnapshotNotificationService = new IncrementalSnapshotNotificationService(this);
    }

    public void notify(Notification notification) {
        this.notificationChannels.stream().filter(this.isEnabled()).forEach((channel) -> {
            channel.send(notification);
        });
    }

    public void notify(Notification notification, Offsets<P, ? extends OffsetContext> offsets) {
        this.notificationChannels.stream().filter(this.isEnabled()).filter(Predicates.not(this.isConnectChannel())).forEach((channel) -> {
            channel.send(notification);
        });
        this.notificationChannels.stream().filter(this.isEnabled()).filter(this.isConnectChannel()).forEach((channel) -> {
            ((ConnectChannel) channel).send(notification, offsets);
        });
    }

    public IncrementalSnapshotNotificationService<P, O> incrementalSnapshotNotificationService() {
        return this.incrementalSnapshotNotificationService;
    }

    private Predicate<? super NotificationChannel> isEnabled() {
        return (channel) -> {
            return this.enabledChannels.contains(channel.name());
        };
    }

    private Predicate<? super NotificationChannel> isConnectChannel() {
        return (channel) -> {
            return channel instanceof ConnectChannel;
        };
    }

    public void stop() {
        this.notificationChannels.stream().filter(this.isEnabled()).forEach(NotificationChannel::close);
    }
}
