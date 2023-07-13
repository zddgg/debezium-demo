package io.debezium.connector.mysql;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.connector.common.BaseSourceInfo;
import io.debezium.document.Document;
import io.debezium.relational.TableId;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@NotThreadSafe
public final class SourceInfo extends BaseSourceInfo {
    public static final String SERVER_ID_KEY = "server_id";
    public static final String GTID_KEY = "gtid";
    public static final String BINLOG_FILENAME_OFFSET_KEY = "file";
    public static final String BINLOG_POSITION_OFFSET_KEY = "pos";
    public static final String BINLOG_ROW_IN_EVENT_OFFSET_KEY = "row";
    public static final String THREAD_KEY = "thread";
    public static final String QUERY_KEY = "query";
    private String currentGtid;
    private String currentBinlogFilename;
    private long currentBinlogPosition = 0L;
    private int currentRowNumber = 0;
    private long serverId = 0L;
    private Instant sourceTime = null;
    private long threadId = -1L;
    private String currentQuery = null;
    private Set<TableId> tableIds = new HashSet();
    private String databaseName;

    public SourceInfo(MySqlConnectorConfig connectorConfig) {
        super(connectorConfig);
    }

    public void setQuery(String query) {
        this.currentQuery = query;
    }

    public String getQuery() {
        return this.currentQuery;
    }

    public void setBinlogPosition(String binlogFilename, long positionOfFirstEvent) {
        if (binlogFilename != null) {
            this.currentBinlogFilename = binlogFilename;
        }

        assert positionOfFirstEvent >= 0L;

        this.currentBinlogPosition = positionOfFirstEvent;
        this.currentRowNumber = 0;
    }

    public void setEventPosition(long positionOfCurrentEvent) {
        this.currentBinlogPosition = positionOfCurrentEvent;
    }

    public void setRowNumber(int eventRowNumber) {
        this.currentRowNumber = eventRowNumber;
    }

    public void databaseEvent(String databaseName) {
        this.databaseName = databaseName;
    }

    public void tableEvent(Set<TableId> tableIds) {
        this.tableIds = new HashSet(tableIds);
    }

    public void tableEvent(TableId tableId) {
        this.tableIds = Collections.singleton(tableId);
    }

    public void startGtid(String gtid) {
        this.currentGtid = gtid;
    }

    public void setBinlogServerId(long serverId) {
        this.serverId = serverId;
    }

    public void setBinlogTimestampSeconds(long timestampInSeconds) {
        this.sourceTime = Instant.ofEpochSecond(timestampInSeconds);
    }

    public void setSourceTime(Instant timestamp) {
        this.sourceTime = timestamp;
    }

    public void setBinlogThread(long threadId) {
        this.threadId = threadId;
    }

    public String binlogFilename() {
        return this.currentBinlogFilename;
    }

    public long binlogPosition() {
        return this.currentBinlogPosition;
    }

    long getServerId() {
        return this.serverId;
    }

    long getThreadId() {
        return this.threadId;
    }

    String table() {
        return this.tableIds.isEmpty() ? null : (String) this.tableIds.stream().filter(Objects::nonNull).map(TableId::table).collect(Collectors.joining(","));
    }

    String getCurrentGtid() {
        return this.currentGtid;
    }

    String getCurrentBinlogFilename() {
        return this.currentBinlogFilename;
    }

    long getCurrentBinlogPosition() {
        return this.currentBinlogPosition;
    }

    long getBinlogTimestampSeconds() {
        return this.sourceTime == null ? 0L : this.sourceTime.getEpochSecond();
    }

    int getCurrentRowNumber() {
        return this.currentRowNumber;
    }

    public String toString() {
        return "SourceInfo [currentGtid=" + this.currentGtid + ", currentBinlogFilename=" + this.currentBinlogFilename + ", currentBinlogPosition=" + this.currentBinlogPosition + ", currentRowNumber=" + this.currentRowNumber + ", serverId=" + this.serverId + ", sourceTime=" + this.sourceTime + ", threadId=" + this.threadId + ", currentQuery=" + this.currentQuery + ", tableIds=" + this.tableIds + ", databaseName=" + this.databaseName + "]";
    }

    public static Document createDocumentFromOffset(Map<String, ?> offset) {
        Document offsetDocument = Document.create();
        Iterator var2 = offset.entrySet().iterator();

        while (var2.hasNext()) {
            Map.Entry<String, ?> entry = (Map.Entry) var2.next();
            offsetDocument.set((CharSequence) entry.getKey(), entry.getValue());
        }

        return offsetDocument;
    }

    protected Instant timestamp() {
        return this.sourceTime;
    }

    protected String database() {
        if (this.tableIds != null && !this.tableIds.isEmpty()) {
            TableId tableId = (TableId) this.tableIds.iterator().next();
            return tableId == null ? this.databaseName : tableId.catalog();
        } else {
            return this.databaseName;
        }
    }
}
