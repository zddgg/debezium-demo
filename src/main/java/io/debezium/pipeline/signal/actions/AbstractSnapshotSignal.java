package io.debezium.pipeline.signal.actions;

import io.debezium.document.Document;
import io.debezium.pipeline.spi.Partition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSnapshotSignal<P extends Partition> implements SignalAction<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSnapshotSignal.class);
    protected static final String FIELD_DATA_COLLECTIONS = "data-collections";
    protected static final String FIELD_TYPE = "type";
    protected static final String FIELD_ADDITIONAL_CONDITION = "additional-condition";
    protected static final String FIELD_SURROGATE_KEY = "surrogate-key";

    public static SnapshotType getSnapshotType(Document data) {
        String typeStr = data.getString("type");
        SnapshotType type = SnapshotType.INCREMENTAL;
        if (typeStr != null) {
            SnapshotType[] var3 = SnapshotType.values();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                SnapshotType option = var3[var5];
                if (option.name().equalsIgnoreCase(typeStr)) {
                    return option;
                }
            }

            LOGGER.warn("Detected an unexpected snapshot type '{}'", typeStr);
            return null;
        } else {
            return type;
        }
    }

    public static enum SnapshotType {
        INCREMENTAL;

        // $FF: synthetic method
        private static SnapshotType[] $values() {
            return new SnapshotType[]{INCREMENTAL};
        }
    }
}
