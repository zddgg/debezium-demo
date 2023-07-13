package io.debezium.crdt;

public final class CRDT {
    public static GCounter newGCounter() {
        return new StateBasedGCounter();
    }

    public static GCounter newGCounter(long adds) {
        return new StateBasedGCounter(adds);
    }

    public static PNCounter newPNCounter() {
        return new StateBasedPNCounter();
    }

    public static PNCounter newPNCounter(long adds, long removes) {
        return new StateBasedPNCounter(adds, removes);
    }

    public static DeltaCounter newDeltaCounter() {
        return new StateBasedPNDeltaCounter();
    }

    public static DeltaCounter newDeltaCounter(long totalAdds, long totalRemoves, long recentAdds, long recentRemoves) {
        return new StateBasedPNDeltaCounter(totalAdds, totalRemoves, recentAdds, recentRemoves);
    }

    public static DeltaCounter newDeltaCounter(DeltaCount count) {
        return count == null ? new StateBasedPNDeltaCounter() : new StateBasedPNDeltaCounter(count.getIncrement(), count.getDecrement(), count.getChanges().getIncrement(), count.getChanges().getDecrement());
    }

    private CRDT() {
    }
}
