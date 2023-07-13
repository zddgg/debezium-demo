package io.debezium.pipeline.metrics;

import io.debezium.pipeline.source.spi.StreamingProgressListener;
import io.debezium.pipeline.spi.Partition;

public interface StreamingChangeEventSourceMetrics<P extends Partition> extends ChangeEventSourceMetrics<P>, StreamingProgressListener {
}
