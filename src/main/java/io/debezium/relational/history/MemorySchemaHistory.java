package io.debezium.relational.history;

import io.debezium.annotation.ThreadSafe;
import io.debezium.util.FunctionalReadWriteLock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@ThreadSafe
public final class MemorySchemaHistory extends AbstractSchemaHistory {
    private final List<HistoryRecord> records = new ArrayList();
    private final FunctionalReadWriteLock lock = FunctionalReadWriteLock.reentrant();

    protected void storeRecord(HistoryRecord record) {
        this.lock.write(() -> {
            return this.records.add(record);
        });
    }

    protected void recoverRecords(Consumer<HistoryRecord> records) {
        this.lock.write(() -> {
            this.records.forEach(records);
        });
    }

    public boolean storageExists() {
        return true;
    }

    public boolean exists() {
        return !this.records.isEmpty();
    }

    public String toString() {
        return "memory";
    }
}
