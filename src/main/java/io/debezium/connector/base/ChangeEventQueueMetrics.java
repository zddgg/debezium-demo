package io.debezium.connector.base;

public interface ChangeEventQueueMetrics {
    int totalCapacity();

    int remainingCapacity();

    long maxQueueSizeInBytes();

    long currentQueueSizeInBytes();
}
