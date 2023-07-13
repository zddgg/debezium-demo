package io.debezium.pipeline.metrics.traits;

public interface CommonEventMetricsMXBean {
    String getLastEvent();

    long getMilliSecondsSinceLastEvent();

    long getTotalNumberOfEventsSeen();

    long getTotalNumberOfCreateEventsSeen();

    long getTotalNumberOfUpdateEventsSeen();

    long getTotalNumberOfDeleteEventsSeen();

    long getNumberOfEventsFiltered();

    long getNumberOfErroneousEvents();
}
