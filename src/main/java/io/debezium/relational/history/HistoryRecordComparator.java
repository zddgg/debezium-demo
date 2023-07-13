package io.debezium.relational.history;

import io.debezium.document.Document;

public class HistoryRecordComparator {
    public static final HistoryRecordComparator INSTANCE = new HistoryRecordComparator();

    public boolean isAtOrBefore(HistoryRecord record1, HistoryRecord record2) {
        return this.isSameSource(record1.source(), record2.source()) && this.isPositionAtOrBefore(record1.position(), record2.position());
    }

    protected boolean isPositionAtOrBefore(Document position1, Document position2) {
        return position1.compareToUsingSimilarFields(position2) <= 0;
    }

    protected boolean isSameSource(Document source1, Document source2) {
        return source1.equals(source2);
    }
}
