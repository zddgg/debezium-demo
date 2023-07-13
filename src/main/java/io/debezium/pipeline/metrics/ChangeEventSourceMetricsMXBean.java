package io.debezium.pipeline.metrics;

import io.debezium.pipeline.metrics.traits.CommonEventMetricsMXBean;
import io.debezium.pipeline.metrics.traits.QueueMetricsMXBean;
import io.debezium.pipeline.metrics.traits.SchemaMetricsMXBean;

public interface ChangeEventSourceMetricsMXBean extends CommonEventMetricsMXBean, QueueMetricsMXBean, SchemaMetricsMXBean {
    void reset();
}
