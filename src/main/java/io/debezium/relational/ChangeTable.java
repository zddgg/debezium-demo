package io.debezium.relational;

public class ChangeTable {
    private final String captureInstance;
    private final TableId sourceTableId;
    private final TableId changeTableId;
    private final int changeTableObjectId;
    private Table sourceTable;

    public ChangeTable(String captureInstance, TableId sourceTableId, TableId changeTableId, int changeTableObjectId) {
        this.captureInstance = captureInstance;
        this.sourceTableId = sourceTableId;
        this.changeTableId = changeTableId;
        this.changeTableObjectId = changeTableObjectId;
    }

    public String getCaptureInstance() {
        return this.captureInstance;
    }

    public TableId getSourceTableId() {
        return this.sourceTableId;
    }

    public TableId getChangeTableId() {
        return this.changeTableId;
    }

    public int getChangeTableObjectId() {
        return this.changeTableObjectId;
    }

    public Table getSourceTable() {
        return this.sourceTable;
    }

    public void setSourceTable(Table sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String toString() {
        return "ChangeTable{captureInstance='" + this.captureInstance + "', sourceTableId=" + this.sourceTableId + ", changeTableId=" + this.changeTableId + ", changeTableObjectId=" + this.changeTableObjectId + "}";
    }
}
