package io.debezium.connector.mysql;

import io.debezium.connector.SnapshotRecord;
import io.debezium.pipeline.CommonOffsetContext;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotContext;
import io.debezium.pipeline.source.snapshot.incremental.SignalBasedIncrementalSnapshotContext;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.txmetadata.TransactionContext;
import io.debezium.relational.TableId;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.errors.ConnectException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MySqlOffsetContext extends CommonOffsetContext<SourceInfo> {
    private static final String SNAPSHOT_COMPLETED_KEY = "snapshot_completed";
    public static final String EVENTS_TO_SKIP_OFFSET_KEY = "event";
    public static final String TIMESTAMP_KEY = "ts_sec";
    public static final String GTID_SET_KEY = "gtids";
    public static final String NON_GTID_TRANSACTION_ID_FORMAT = "file=%s,pos=%s";
    private final Schema sourceInfoSchema;
    private boolean snapshotCompleted;
    private final TransactionContext transactionContext;
    private final IncrementalSnapshotContext<TableId> incrementalSnapshotContext;
    private String restartGtidSet;
    private String currentGtidSet;
    private String restartBinlogFilename;
    private long restartBinlogPosition;
    private int restartRowsToSkip;
    private long restartEventsToSkip;
    private long currentEventLengthInBytes;
    private boolean inTransaction;
    private String transactionId;

    public MySqlOffsetContext(boolean snapshot, boolean snapshotCompleted, TransactionContext transactionContext, IncrementalSnapshotContext<TableId> incrementalSnapshotContext, SourceInfo sourceInfo) {
        super(sourceInfo);
        this.restartBinlogPosition = 0L;
        this.restartRowsToSkip = 0;
        this.restartEventsToSkip = 0L;
        this.currentEventLengthInBytes = 0L;
        this.inTransaction = false;
        this.transactionId = null;
        this.sourceInfoSchema = sourceInfo.schema();
        this.snapshotCompleted = snapshotCompleted;
        if (this.snapshotCompleted) {
            this.postSnapshotCompletion();
        } else {
            sourceInfo.setSnapshot(snapshot ? SnapshotRecord.TRUE : SnapshotRecord.FALSE);
        }

        this.transactionContext = transactionContext;
        this.incrementalSnapshotContext = incrementalSnapshotContext;
    }

    public MySqlOffsetContext(MySqlConnectorConfig connectorConfig, boolean snapshot, boolean snapshotCompleted, SourceInfo sourceInfo) {
        this(snapshot, snapshotCompleted, new TransactionContext(), (IncrementalSnapshotContext) (connectorConfig.isReadOnlyConnection() ? new MySqlReadOnlyIncrementalSnapshotContext() : new SignalBasedIncrementalSnapshotContext()), sourceInfo);
    }

    public Map<String, ?> getOffset() {
        Map<String, Object> offset = this.offsetUsingPosition((long) this.restartRowsToSkip);
        if (((SourceInfo) this.sourceInfo).isSnapshot()) {
            if (!this.snapshotCompleted) {
                offset.put("snapshot", true);
            }

            return offset;
        } else {
            return this.incrementalSnapshotContext.store(this.transactionContext.store(offset));
        }
    }

    private Map<String, Object> offsetUsingPosition(long rowsToSkip) {
        Map<String, Object> map = new HashMap();
        if (((SourceInfo) this.sourceInfo).getServerId() != 0L) {
            map.put("server_id", ((SourceInfo) this.sourceInfo).getServerId());
        }

        if (this.restartGtidSet != null) {
            map.put("gtids", this.restartGtidSet);
        }

        map.put("file", this.restartBinlogFilename);
        map.put("pos", this.restartBinlogPosition);
        if (this.restartEventsToSkip != 0L) {
            map.put("event", this.restartEventsToSkip);
        }

        if (rowsToSkip != 0L) {
            map.put("row", rowsToSkip);
        }

        if (((SourceInfo) this.sourceInfo).timestamp() != null) {
            map.put("ts_sec", ((SourceInfo) this.sourceInfo).timestamp().getEpochSecond());
        }

        return map;
    }

    public Schema getSourceInfoSchema() {
        return this.sourceInfoSchema;
    }

    public boolean isSnapshotRunning() {
        return ((SourceInfo) this.sourceInfo).isSnapshot() && !this.snapshotCompleted;
    }

    public boolean isSnapshotCompleted() {
        return this.snapshotCompleted;
    }

    public void preSnapshotStart() {
        ((SourceInfo) this.sourceInfo).setSnapshot(SnapshotRecord.TRUE);
        this.snapshotCompleted = false;
    }

    public void preSnapshotCompletion() {
        this.snapshotCompleted = true;
    }

    private void setTransactionId() {
        if (((SourceInfo) this.sourceInfo).getCurrentGtid() != null) {
            this.transactionId = ((SourceInfo) this.sourceInfo).getCurrentGtid();
        } else {
            this.transactionId = String.format("file=%s,pos=%s", this.restartBinlogFilename, this.restartBinlogPosition);
        }

    }

    private void resetTransactionId() {
        this.transactionId = null;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setInitialSkips(long restartEventsToSkip, int restartRowsToSkip) {
        this.restartEventsToSkip = restartEventsToSkip;
        this.restartRowsToSkip = restartRowsToSkip;
    }

    public static MySqlOffsetContext initial(MySqlConnectorConfig config) {
        MySqlOffsetContext offset = new MySqlOffsetContext(config, false, false, new SourceInfo(config));
        offset.setBinlogStartPoint("", 0L);
        return offset;
    }

    public void event(DataCollectionId tableId, Instant timestamp) {
        ((SourceInfo) this.sourceInfo).setSourceTime(timestamp);
        ((SourceInfo) this.sourceInfo).tableEvent((TableId) tableId);
    }

    public void databaseEvent(String database, Instant timestamp) {
        ((SourceInfo) this.sourceInfo).setSourceTime(timestamp);
        ((SourceInfo) this.sourceInfo).databaseEvent(database);
        ((SourceInfo) this.sourceInfo).tableEvent((TableId) null);
    }

    public void tableEvent(String database, Set<TableId> tableIds, Instant timestamp) {
        ((SourceInfo) this.sourceInfo).setSourceTime(timestamp);
        ((SourceInfo) this.sourceInfo).databaseEvent(database);
        ((SourceInfo) this.sourceInfo).tableEvent(tableIds);
    }

    public TransactionContext getTransactionContext() {
        return this.transactionContext;
    }

    public IncrementalSnapshotContext<?> getIncrementalSnapshotContext() {
        return this.incrementalSnapshotContext;
    }

    public void setBinlogStartPoint(String binlogFilename, long positionOfFirstEvent) {
        assert positionOfFirstEvent >= 0L;

        if (binlogFilename != null) {
            ((SourceInfo) this.sourceInfo).setBinlogPosition(binlogFilename, positionOfFirstEvent);
            this.restartBinlogFilename = binlogFilename;
        } else {
            ((SourceInfo) this.sourceInfo).setBinlogPosition(((SourceInfo) this.sourceInfo).getCurrentBinlogFilename(), positionOfFirstEvent);
        }

        this.restartBinlogPosition = positionOfFirstEvent;
        this.restartRowsToSkip = 0;
        this.restartEventsToSkip = 0L;
    }

    public void setCompletedGtidSet(String gtidSet) {
        if (gtidSet != null && !gtidSet.trim().isEmpty()) {
            String trimmedGtidSet = gtidSet.replace("\n", "").replace("\r", "");
            this.currentGtidSet = trimmedGtidSet;
            this.restartGtidSet = trimmedGtidSet;
        }

    }

    public String gtidSet() {
        return this.currentGtidSet != null ? this.currentGtidSet : null;
    }

    public void startGtid(String gtid, String gtidSet) {
        ((SourceInfo) this.sourceInfo).startGtid(gtid);
        if (gtidSet != null && !gtidSet.trim().isEmpty()) {
            String trimmedGtidSet = gtidSet.replace("\n", "").replace("\r", "");
            this.restartGtidSet = this.currentGtidSet != null ? this.currentGtidSet : trimmedGtidSet;
            this.currentGtidSet = trimmedGtidSet;
        }

    }

    public SourceInfo getSource() {
        return (SourceInfo) this.sourceInfo;
    }

    public void startNextTransaction() {
        this.restartRowsToSkip = 0;
        this.restartEventsToSkip = 0L;
        this.restartBinlogFilename = ((SourceInfo) this.sourceInfo).binlogFilename();
        this.restartBinlogPosition = ((SourceInfo) this.sourceInfo).binlogPosition();
        this.inTransaction = true;
        this.setTransactionId();
    }

    public void commitTransaction() {
        this.restartGtidSet = this.currentGtidSet;
        this.restartBinlogFilename = ((SourceInfo) this.sourceInfo).binlogFilename();
        this.restartBinlogPosition = ((SourceInfo) this.sourceInfo).binlogPosition() + this.currentEventLengthInBytes;
        this.restartRowsToSkip = 0;
        this.restartEventsToSkip = 0L;
        this.inTransaction = false;
        ((SourceInfo) this.sourceInfo).setQuery((String) null);
        this.resetTransactionId();
    }

    public void completeEvent() {
        ++this.restartEventsToSkip;
    }

    public void setEventPosition(long positionOfCurrentEvent, long eventSizeInBytes) {
        ((SourceInfo) this.sourceInfo).setEventPosition(positionOfCurrentEvent);
        this.currentEventLengthInBytes = eventSizeInBytes;
        if (!this.inTransaction) {
            this.restartBinlogPosition = positionOfCurrentEvent + eventSizeInBytes;
            this.restartRowsToSkip = 0;
            this.restartEventsToSkip = 0L;
        }

    }

    public void setQuery(String query) {
        ((SourceInfo) this.sourceInfo).setQuery(query);
    }

    public void changeEventCompleted() {
        this.restartRowsToSkip = 0;
    }

    public long eventsToSkipUponRestart() {
        return this.restartEventsToSkip;
    }

    public int rowsToSkipUponRestart() {
        return this.restartRowsToSkip;
    }

    public void setRowNumber(int eventRowNumber, int totalNumberOfRows) {
        ((SourceInfo) this.sourceInfo).setRowNumber(eventRowNumber);
        if (eventRowNumber < totalNumberOfRows - 1) {
            this.restartRowsToSkip = eventRowNumber + 1;
        } else {
            this.restartRowsToSkip = totalNumberOfRows;
        }

    }

    public void setBinlogServerId(long serverId) {
        ((SourceInfo) this.sourceInfo).setBinlogServerId(serverId);
    }

    public void setBinlogThread(long threadId) {
        ((SourceInfo) this.sourceInfo).setBinlogThread(threadId);
    }

    public String toString() {
        return "MySqlOffsetContext [sourceInfoSchema=" + this.sourceInfoSchema + ", sourceInfo=" + this.sourceInfo + ", snapshotCompleted=" + this.snapshotCompleted + ", transactionContext=" + this.transactionContext + ", restartGtidSet=" + this.restartGtidSet + ", currentGtidSet=" + this.currentGtidSet + ", restartBinlogFilename=" + this.restartBinlogFilename + ", restartBinlogPosition=" + this.restartBinlogPosition + ", restartRowsToSkip=" + this.restartRowsToSkip + ", restartEventsToSkip=" + this.restartEventsToSkip + ", currentEventLengthInBytes=" + this.currentEventLengthInBytes + ", inTransaction=" + this.inTransaction + ", transactionId=" + this.transactionId + ", incrementalSnapshotContext =" + this.incrementalSnapshotContext + "]";
    }

    public static class Loader implements OffsetContext.Loader<MySqlOffsetContext> {
        private final MySqlConnectorConfig connectorConfig;

        public Loader(MySqlConnectorConfig connectorConfig) {
            this.connectorConfig = connectorConfig;
        }

        public MySqlOffsetContext load(Map<String, ?> offset) {
            boolean snapshot = Boolean.TRUE.equals(offset.get("snapshot")) || "true".equals(offset.get("snapshot"));
            boolean snapshotCompleted = Boolean.TRUE.equals(offset.get("snapshot_completed")) || "true".equals(offset.get("snapshot_completed"));
            String binlogFilename = (String) offset.get("file");
            if (binlogFilename == null) {
                throw new ConnectException("Source offset 'file' parameter is missing");
            } else {
                long binlogPosition = this.longOffsetValue(offset, "pos");
                Object incrementalSnapshotContext;
                if (this.connectorConfig.isReadOnlyConnection()) {
                    incrementalSnapshotContext = MySqlReadOnlyIncrementalSnapshotContext.load(offset);
                } else {
                    incrementalSnapshotContext = SignalBasedIncrementalSnapshotContext.load(offset);
                }

                MySqlOffsetContext offsetContext = new MySqlOffsetContext(snapshot, snapshotCompleted, TransactionContext.load(offset), (IncrementalSnapshotContext) incrementalSnapshotContext, new SourceInfo(this.connectorConfig));
                offsetContext.setBinlogStartPoint(binlogFilename, binlogPosition);
                offsetContext.setInitialSkips(this.longOffsetValue(offset, "event"), (int) this.longOffsetValue(offset, "row"));
                offsetContext.setCompletedGtidSet((String) offset.get("gtids"));
                return offsetContext;
            }
        }

        private long longOffsetValue(Map<String, ?> values, String key) {
            Object obj = values.get(key);
            if (obj == null) {
                return 0L;
            } else if (obj instanceof Number) {
                return ((Number) obj).longValue();
            } else {
                try {
                    return Long.parseLong(obj.toString());
                } catch (NumberFormatException var5) {
                    throw new ConnectException("Source offset '" + key + "' parameter value " + obj + " could not be converted to a long");
                }
            }
        }
    }
}
