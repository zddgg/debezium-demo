package io.debezium.pipeline;

import io.debezium.util.ApproximateStructSizeCalculator;
import org.apache.kafka.connect.source.SourceRecord;

public class DataChangeEvent implements Sizeable {
    private final SourceRecord record;

    public DataChangeEvent(SourceRecord record) {
        this.record = record;
    }

    public SourceRecord getRecord() {
        return this.record;
    }

    public String toString() {
        return "DataChangeEvent [record=" + this.record + "]";
    }

    public long objectSize() {
        return ApproximateStructSizeCalculator.getApproximateRecordSize(this.record);
    }
}
