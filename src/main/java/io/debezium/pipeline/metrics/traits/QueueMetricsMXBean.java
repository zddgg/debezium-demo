package io.debezium.pipeline.metrics.traits;

public interface QueueMetricsMXBean {
    int getQueueTotalCapacity();

    int getQueueRemainingCapacity();

    long getMaxQueueSizeInBytes();

    long getCurrentQueueSizeInBytes();
}
