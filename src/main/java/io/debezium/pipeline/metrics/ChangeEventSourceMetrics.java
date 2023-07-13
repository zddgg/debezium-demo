package io.debezium.pipeline.metrics;

import io.debezium.pipeline.source.spi.DataChangeEventListener;
import io.debezium.pipeline.spi.Partition;

public interface ChangeEventSourceMetrics<P extends Partition> extends DataChangeEventListener<P> {
    void register();

    void unregister();
}
