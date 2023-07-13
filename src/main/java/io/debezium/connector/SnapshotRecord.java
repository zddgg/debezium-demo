package io.debezium.connector;

import org.apache.kafka.connect.data.Struct;

public enum SnapshotRecord {
    TRUE,
    FIRST,
    FIRST_IN_DATA_COLLECTION,
    LAST_IN_DATA_COLLECTION,
    LAST,
    FALSE,
    INCREMENTAL;

    public static SnapshotRecord fromSource(Struct source) {
        if (source.schema().field("snapshot") != null && "io.debezium.data.Enum".equals(source.schema().field("snapshot").schema().name())) {
            String snapshotString = source.getString("snapshot");
            if (snapshotString != null) {
                return valueOf(snapshotString.toUpperCase());
            }
        }

        return null;
    }

    public void toSource(Struct source) {
        if (this != FALSE) {
            source.put("snapshot", this.name().toLowerCase());
        }

    }

    // $FF: synthetic method
    private static SnapshotRecord[] $values() {
        return new SnapshotRecord[]{TRUE, FIRST, FIRST_IN_DATA_COLLECTION, LAST_IN_DATA_COLLECTION, LAST, FALSE, INCREMENTAL};
    }
}
