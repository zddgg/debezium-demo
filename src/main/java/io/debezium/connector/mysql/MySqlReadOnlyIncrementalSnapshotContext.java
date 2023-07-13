package io.debezium.connector.mysql;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.pipeline.source.snapshot.incremental.AbstractIncrementalSnapshotContext;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotContext;
import io.debezium.pipeline.spi.OffsetContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@NotThreadSafe
public class MySqlReadOnlyIncrementalSnapshotContext<T> extends AbstractIncrementalSnapshotContext<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlReadOnlyIncrementalSnapshotContext.class);
    private GtidSet previousLowWatermark;
    private GtidSet previousHighWatermark;
    private GtidSet lowWatermark;
    private GtidSet highWatermark;
    private Long signalOffset;
    public static final String SIGNAL_OFFSET = "incremental_snapshot_signal_offset";

    public MySqlReadOnlyIncrementalSnapshotContext() {
        this(true);
    }

    public MySqlReadOnlyIncrementalSnapshotContext(boolean useCatalogBeforeSchema) {
        super(useCatalogBeforeSchema);
    }

    protected static <U> IncrementalSnapshotContext<U> init(MySqlReadOnlyIncrementalSnapshotContext<U> context, Map<String, ?> offsets) {
        AbstractIncrementalSnapshotContext.init(context, offsets);
        Long signalOffset = (Long) offsets.get("incremental_snapshot_signal_offset");
        context.setSignalOffset(signalOffset);
        return context;
    }

    public static <U> MySqlReadOnlyIncrementalSnapshotContext<U> load(Map<String, ?> offsets) {
        return load(offsets, true);
    }

    public static <U> MySqlReadOnlyIncrementalSnapshotContext<U> load(Map<String, ?> offsets, boolean useCatalogBeforeSchema) {
        MySqlReadOnlyIncrementalSnapshotContext<U> context = new MySqlReadOnlyIncrementalSnapshotContext(useCatalogBeforeSchema);
        init(context, offsets);
        return context;
    }

    public void setLowWatermark(GtidSet lowWatermark) {
        this.lowWatermark = lowWatermark;
    }

    public void setHighWatermark(GtidSet highWatermark) {
        this.highWatermark = highWatermark.subtract(this.lowWatermark);
    }

    public boolean updateWindowState(OffsetContext offsetContext) {
        String currentGtid = this.getCurrentGtid(offsetContext);
        boolean pastHighWatermark;
        if (!this.windowOpened && this.lowWatermark != null) {
            pastHighWatermark = !this.lowWatermark.contains(currentGtid);
            if (pastHighWatermark) {
                LOGGER.debug("Current gtid {}, low watermark {}", currentGtid, this.lowWatermark);
                this.windowOpened = true;
            }
        }

        if (this.windowOpened && this.highWatermark != null) {
            pastHighWatermark = !this.highWatermark.contains(currentGtid);
            if (pastHighWatermark) {
                LOGGER.debug("Current gtid {}, high watermark {}", currentGtid, this.highWatermark);
                this.closeWindow();
                return true;
            }
        }

        return false;
    }

    public boolean reachedHighWatermark(String currentGtid) {
        if (this.highWatermark == null) {
            return false;
        } else if (currentGtid == null) {
            return true;
        } else {
            String[] gtid = GtidSet.GTID_DELIMITER.split(currentGtid);
            GtidSet.UUIDSet uuidSet = this.getUuidSet(gtid[0]);
            if (uuidSet != null) {
                long maxTransactionId = uuidSet.getIntervals().stream().mapToLong(GtidSet.Interval::getEnd).max().getAsLong();
                if (maxTransactionId <= Long.parseLong(gtid[1])) {
                    LOGGER.debug("Gtid {} reached high watermark {}", currentGtid, this.highWatermark);
                    return true;
                }
            }

            return false;
        }
    }

    public String getCurrentGtid(OffsetContext offsetContext) {
        return offsetContext.getSourceInfo().getString("gtid");
    }

    public void closeWindow() {
        this.windowOpened = false;
        this.previousHighWatermark = this.highWatermark;
        this.highWatermark = null;
        this.previousLowWatermark = this.lowWatermark;
        this.lowWatermark = null;
    }

    private GtidSet.UUIDSet getUuidSet(String serverId) {
        return this.highWatermark.getUUIDSets().isEmpty() ? this.lowWatermark.forServerWithId(serverId) : this.highWatermark.forServerWithId(serverId);
    }

    public boolean serverUuidChanged() {
        return this.highWatermark.getUUIDSets().size() > 1;
    }

    public Long getSignalOffset() {
        return this.signalOffset;
    }

    public void setSignalOffset(Long signalOffset) {
        this.signalOffset = signalOffset;
    }

    public Map<String, Object> store(Map<String, Object> offset) {
        Map<String, Object> snapshotOffset = super.store(offset);
        snapshotOffset.put("incremental_snapshot_signal_offset", this.signalOffset);
        return snapshotOffset;
    }

    public boolean watermarksChanged() {
        return !this.previousLowWatermark.equals(this.lowWatermark) || !this.previousHighWatermark.equals(this.highWatermark);
    }
}
