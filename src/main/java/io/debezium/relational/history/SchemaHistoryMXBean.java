package io.debezium.relational.history;

public interface SchemaHistoryMXBean {
    String getStatus();

    long getRecoveryStartTime();

    long getChangesRecovered();

    long getChangesApplied();

    long getMilliSecondsSinceLastAppliedChange();

    long getMilliSecondsSinceLastRecoveredChange();

    String getLastAppliedChange();

    String getLastRecoveredChange();
}
