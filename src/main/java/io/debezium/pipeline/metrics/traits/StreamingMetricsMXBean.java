package io.debezium.pipeline.metrics.traits;

import java.util.Map;

public interface StreamingMetricsMXBean extends SchemaMetricsMXBean {
    Map<String, String> getSourceEventPosition();

    long getMilliSecondsBehindSource();

    long getNumberOfCommittedTransactions();

    String getLastTransactionId();
}
