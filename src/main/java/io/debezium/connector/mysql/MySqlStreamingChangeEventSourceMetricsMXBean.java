package io.debezium.connector.mysql;

import io.debezium.pipeline.metrics.StreamingChangeEventSourceMetricsMXBean;

public interface MySqlStreamingChangeEventSourceMetricsMXBean extends StreamingChangeEventSourceMetricsMXBean {
    String getBinlogFilename();

    long getBinlogPosition();

    String getGtidSet();

    long getNumberOfSkippedEvents();

    long getNumberOfDisconnects();

    long getNumberOfCommittedTransactions();

    long getNumberOfRolledBackTransactions();

    long getNumberOfNotWellFormedTransactions();

    long getNumberOfLargeTransactions();

    boolean getIsGtidModeEnabled();
}
