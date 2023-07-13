package io.debezium.pipeline.metrics;

import io.debezium.pipeline.metrics.traits.ConnectionMetricsMXBean;
import io.debezium.pipeline.metrics.traits.StreamingMetricsMXBean;

public interface StreamingChangeEventSourceMetricsMXBean extends ChangeEventSourceMetricsMXBean, ConnectionMetricsMXBean, StreamingMetricsMXBean {
}
