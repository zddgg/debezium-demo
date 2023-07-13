package io.debezium.schema;

import io.debezium.DebeziumException;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.Table;
import io.debezium.relational.TableId;
import io.debezium.relational.history.TableChanges;
import io.debezium.util.Clock;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class SchemaChangeEvent {
    private final String database;
    private final String schema;
    private final String ddl;
    private final Set<Table> tables;
    private final SchemaChangeEventType type;
    private final Map<String, ?> partition;
    private final Map<String, ?> offset;
    private final Struct source;
    private final boolean isFromSnapshot;
    private final Instant timestamp;
    private TableChanges tableChanges;

    private SchemaChangeEvent(Map<String, ?> partition, Map<String, ?> offset, Struct source, String database, String schema, String ddl, Table table, SchemaChangeEventType type, boolean isFromSnapshot, TableId previousTableId) {
        this(partition, offset, source, database, schema, ddl, table != null ? Collections.singleton(table) : Collections.emptySet(), type, isFromSnapshot, Clock.SYSTEM.currentTimeAsInstant(), previousTableId);
    }

    private SchemaChangeEvent(Map<String, ?> partition, Map<String, ?> offset, Struct source, String database, String schema, String ddl, Set<Table> tables, SchemaChangeEventType type, boolean isFromSnapshot, Instant timestamp, TableId previousTableId) {
        this.tableChanges = new TableChanges();
        this.partition = (Map) Objects.requireNonNull(partition, "partition must not be null");
        this.offset = (Map) Objects.requireNonNull(offset, "offset must not be null");
        this.source = (Struct) Objects.requireNonNull(source, "source must not be null");
        this.database = (String) Objects.requireNonNull(database, "database must not be null");
        this.schema = schema;
        this.ddl = ddl;
        this.tables = (Set) Objects.requireNonNull(tables, "tables must not be null");
        this.type = (SchemaChangeEventType) Objects.requireNonNull(type, "type must not be null");
        this.isFromSnapshot = isFromSnapshot;
        this.timestamp = timestamp;
        TableChanges var10001;
        switch (type) {
            case CREATE:
                var10001 = this.tableChanges;
                Objects.requireNonNull(var10001);
                tables.forEach(var10001::create);
                break;
            case ALTER:
                if (previousTableId == null) {
                    var10001 = this.tableChanges;
                    Objects.requireNonNull(var10001);
                    tables.forEach(var10001::alter);
                } else {
                    tables.forEach((t) -> {
                        this.tableChanges.rename(t, previousTableId);
                    });
                }
                break;
            case DROP:
                var10001 = this.tableChanges;
                Objects.requireNonNull(var10001);
                tables.forEach(var10001::drop);
            case DATABASE:
        }

    }

    public Map<String, ?> getPartition() {
        return this.partition;
    }

    public Map<String, ?> getOffset() {
        return this.offset;
    }

    public Struct getSource() {
        return this.source;
    }

    public String getDatabase() {
        return this.database;
    }

    public String getSchema() {
        return this.schema;
    }

    public String getDdl() {
        return this.ddl;
    }

    public Set<Table> getTables() {
        return this.tables;
    }

    public SchemaChangeEventType getType() {
        return this.type;
    }

    public boolean isFromSnapshot() {
        return this.isFromSnapshot;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public TableChanges getTableChanges() {
        return this.tableChanges;
    }

    public String toString() {
        String var10000 = this.database;
        return "SchemaChangeEvent [database=" + var10000 + ", schema=" + this.schema + ", ddl=" + this.ddl + ", tables=" + this.tables + ", type=" + this.type + ", ts_ms=" + this.timestamp.toEpochMilli() + "]";
    }

    public static SchemaChangeEvent of(SchemaChangeEventType type, Partition partition, OffsetContext offsetContext, String databaseName, String schemaName, String ddl, Table table, boolean isFromSnapshot) {
        return new SchemaChangeEvent(partition.getSourcePartition(), offsetContext.getOffset(), offsetContext.getSourceInfo(), databaseName, schemaName, ddl, table, type, isFromSnapshot, (TableId) null);
    }

    public static SchemaChangeEvent ofTableChange(TableChanges.TableChange change, Map<String, ?> partition, Map<String, ?> offset, Struct source, String databaseName, String schemaName) {
        return new SchemaChangeEvent(partition, offset, source, databaseName, schemaName, (String) null, change.getTable(), toSchemaChangeEventType(change.getType()), false, change.getPreviousId());
    }

    public static SchemaChangeEvent ofDatabase(Partition partition, OffsetContext offsetContext, String databaseName, String ddl, boolean isFromSnapshot) {
        return of(SchemaChangeEventType.DATABASE, partition, offsetContext, databaseName, (String) null, ddl, (Table) null, isFromSnapshot);
    }

    public static SchemaChangeEvent ofSnapshotCreate(Partition partition, OffsetContext offsetContext, String databaseName, Table table) {
        return ofCreate(partition, offsetContext, databaseName, table.id().schema(), (String) null, table, true);
    }

    public static SchemaChangeEvent ofCreate(Partition partition, OffsetContext offsetContext, String databaseName, String schemaName, String ddl, Table table, boolean isFromSnapshot) {
        return of(SchemaChangeEventType.CREATE, partition, offsetContext, databaseName, schemaName, ddl, table, isFromSnapshot);
    }

    public static SchemaChangeEvent ofAlter(Partition partition, OffsetContext offsetContext, String databaseName, String schemaName, String ddl, Table table) {
        return of(SchemaChangeEventType.ALTER, partition, offsetContext, databaseName, schemaName, ddl, table, false);
    }

    public static SchemaChangeEvent ofRename(Partition partition, OffsetContext offsetContext, String databaseName, String schemaName, String ddl, Table table, TableId previousTableId) {
        return new SchemaChangeEvent(partition.getSourcePartition(), offsetContext.getOffset(), offsetContext.getSourceInfo(), databaseName, schemaName, ddl, table, SchemaChangeEventType.ALTER, false, previousTableId);
    }

    public static SchemaChangeEvent ofDrop(Partition partition, OffsetContext offsetContext, String databaseName, String schemaName, String ddl, Table table) {
        return of(SchemaChangeEventType.DROP, partition, offsetContext, databaseName, schemaName, ddl, table, false);
    }

    private static SchemaChangeEventType toSchemaChangeEventType(TableChanges.TableChangeType type) {
        switch (type) {
            case CREATE:
                return SchemaChangeEventType.CREATE;
            case ALTER:
                return SchemaChangeEventType.ALTER;
            case DROP:
                return SchemaChangeEventType.DROP;
            default:
                throw new DebeziumException("Unknown table change event type " + type);
        }
    }

    public static enum SchemaChangeEventType {
        CREATE,
        ALTER,
        DROP,
        TRUNCATE,
        DATABASE;

        // $FF: synthetic method
        private static SchemaChangeEventType[] $values() {
            return new SchemaChangeEventType[]{CREATE, ALTER, DROP, TRUNCATE, DATABASE};
        }
    }
}
