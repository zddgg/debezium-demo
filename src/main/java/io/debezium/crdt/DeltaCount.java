package io.debezium.crdt;

public interface DeltaCount extends PNCount {
    PNCount getChanges();

    default boolean hasChanges() {
        PNCount changes = this.getChanges();
        return changes.getIncrement() != 0L || changes.getDecrement() != 0L;
    }

    Count getPriorCount();
}
