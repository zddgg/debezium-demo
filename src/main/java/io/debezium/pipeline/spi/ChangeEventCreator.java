package io.debezium.pipeline.spi;

import io.debezium.pipeline.DataChangeEvent;
import org.apache.kafka.connect.source.SourceRecord;

@FunctionalInterface
public interface ChangeEventCreator {
    DataChangeEvent createDataChangeEvent(SourceRecord var1);
}
