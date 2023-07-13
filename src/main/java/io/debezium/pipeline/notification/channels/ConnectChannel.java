package io.debezium.pipeline.notification.channels;

import io.debezium.function.BlockingConsumer;
import io.debezium.pipeline.notification.Notification;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.source.SourceRecord;

public interface ConnectChannel {
    void initConnectChannel(SchemaFactory var1, BlockingConsumer<SourceRecord> var2);

    <P extends Partition, O extends OffsetContext> void send(Notification var1, Offsets<P, O> var2);
}
