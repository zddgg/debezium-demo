package io.debezium.relational.ddl;

import io.debezium.annotation.Immutable;
import io.debezium.relational.TableId;

import java.util.Optional;

@FunctionalInterface
public interface DdlParserListener {
    void handle(Event var1);

    @Immutable
    public static class SetVariableEvent extends Event {
        private final String variableName;
        private final String value;
        private final String databaseName;
        private final int order;

        public SetVariableEvent(String variableName, String value, String currentDatabaseName, int order, String ddlStatement) {
            super(EventType.SET_VARIABLE, ddlStatement);
            this.variableName = variableName;
            this.value = value;
            this.databaseName = currentDatabaseName;
            this.order = order;
        }

        public String variableName() {
            return this.variableName;
        }

        public String variableValue() {
            return this.value;
        }

        public int order() {
            return this.order;
        }

        public Optional<String> databaseName() {
            return Optional.ofNullable(this.databaseName);
        }

        public String toString() {
            return this.statement();
        }
    }

    @Immutable
    public static class DatabaseSwitchedEvent extends DatabaseEvent {
        public DatabaseSwitchedEvent(String databaseName, String ddlStatement) {
            super(EventType.USE_DATABASE, databaseName, ddlStatement);
        }
    }

    @Immutable
    public static class DatabaseDroppedEvent extends DatabaseEvent {
        public DatabaseDroppedEvent(String databaseName, String ddlStatement) {
            super(EventType.DROP_DATABASE, databaseName, ddlStatement);
        }
    }

    @Immutable
    public static class DatabaseAlteredEvent extends DatabaseEvent {
        private final String previousDatabaseName;

        public DatabaseAlteredEvent(String databaseName, String previousDatabaseName, String ddlStatement) {
            super(EventType.ALTER_DATABASE, databaseName, ddlStatement);
            this.previousDatabaseName = previousDatabaseName;
        }

        public String previousDatabaseName() {
            return this.previousDatabaseName;
        }

        public String toString() {
            String var10000;
            if (this.previousDatabaseName != null) {
                var10000 = this.databaseName();
                return var10000 + " (was " + this.previousDatabaseName() + ") => " + this.statement();
            } else {
                var10000 = this.databaseName();
                return var10000 + " => " + this.statement();
            }
        }
    }

    @Immutable
    public static class DatabaseCreatedEvent extends DatabaseEvent {
        public DatabaseCreatedEvent(String databaseName, String ddlStatement) {
            super(EventType.CREATE_DATABASE, databaseName, ddlStatement);
        }
    }

    @Immutable
    public abstract static class DatabaseEvent extends Event {
        private final String databaseName;

        public DatabaseEvent(EventType type, String databaseName, String ddlStatement) {
            super(type, ddlStatement);
            this.databaseName = databaseName;
        }

        public String databaseName() {
            return this.databaseName;
        }

        public String toString() {
            String var10000 = this.databaseName();
            return var10000 + " => " + this.statement();
        }
    }

    @Immutable
    public static class TableIndexDroppedEvent extends TableIndexEvent {
        public TableIndexDroppedEvent(String indexName, TableId tableId, String ddlStatement) {
            super(EventType.DROP_INDEX, indexName, tableId, ddlStatement);
        }
    }

    @Immutable
    public static class TableIndexCreatedEvent extends TableIndexEvent {
        public TableIndexCreatedEvent(String indexName, TableId tableId, String ddlStatement) {
            super(EventType.CREATE_INDEX, indexName, tableId, ddlStatement);
        }
    }

    @Immutable
    public abstract static class TableIndexEvent extends Event {
        private final TableId tableId;
        private final String indexName;

        public TableIndexEvent(EventType type, String indexName, TableId tableId, String ddlStatement) {
            super(type, ddlStatement);
            this.tableId = tableId;
            this.indexName = indexName;
        }

        public TableId tableId() {
            return this.tableId;
        }

        public String indexName() {
            return this.indexName;
        }

        public String toString() {
            String var10000;
            if (this.tableId == null) {
                var10000 = this.indexName();
                return var10000 + " => " + this.statement();
            } else {
                var10000 = this.indexName();
                return var10000 + " on " + this.tableId() + " => " + this.statement();
            }
        }
    }

    @Immutable
    public static class TableTruncatedEvent extends TableEvent {
        public TableTruncatedEvent(TableId tableId, String ddlStatement, boolean isView) {
            super(EventType.TRUNCATE_TABLE, tableId, ddlStatement, isView);
        }
    }

    @Immutable
    public static class TableDroppedEvent extends TableEvent {
        public TableDroppedEvent(TableId tableId, String ddlStatement, boolean isView) {
            super(EventType.DROP_TABLE, tableId, ddlStatement, isView);
        }
    }

    @Immutable
    public static class TableAlteredEvent extends TableEvent {
        private final TableId previousTableId;

        public TableAlteredEvent(TableId tableId, TableId previousTableId, String ddlStatement, boolean isView) {
            super(EventType.ALTER_TABLE, tableId, ddlStatement, isView);
            this.previousTableId = previousTableId;
        }

        public TableId previousTableId() {
            return this.previousTableId;
        }

        public String toString() {
            TableId var10000;
            if (this.previousTableId != null) {
                var10000 = this.tableId();
                return "" + var10000 + " (was " + this.previousTableId() + ") => " + this.statement();
            } else {
                var10000 = this.tableId();
                return "" + var10000 + " => " + this.statement();
            }
        }
    }

    @Immutable
    public static class TableCreatedEvent extends TableEvent {
        public TableCreatedEvent(TableId tableId, String ddlStatement, boolean isView) {
            super(EventType.CREATE_TABLE, tableId, ddlStatement, isView);
        }
    }

    @Immutable
    public abstract static class TableEvent extends Event {
        private final TableId tableId;
        private final boolean isView;

        public TableEvent(EventType type, TableId tableId, String ddlStatement, boolean isView) {
            super(type, ddlStatement);
            this.tableId = tableId;
            this.isView = isView;
        }

        public TableId tableId() {
            return this.tableId;
        }

        public boolean isView() {
            return this.isView;
        }

        public String toString() {
            TableId var10000 = this.tableId();
            return "" + var10000 + " => " + this.statement();
        }
    }

    @Immutable
    public abstract static class Event {
        private final String statement;
        private final EventType type;

        public Event(EventType type, String ddlStatement) {
            this.type = type;
            this.statement = ddlStatement;
        }

        public EventType type() {
            return this.type;
        }

        public String statement() {
            return this.statement;
        }
    }

    public static enum EventType {
        CREATE_TABLE,
        ALTER_TABLE,
        DROP_TABLE,
        TRUNCATE_TABLE,
        CREATE_INDEX,
        DROP_INDEX,
        CREATE_DATABASE,
        ALTER_DATABASE,
        DROP_DATABASE,
        USE_DATABASE,
        SET_VARIABLE;

        // $FF: synthetic method
        private static EventType[] $values() {
            return new EventType[]{CREATE_TABLE, ALTER_TABLE, DROP_TABLE, TRUNCATE_TABLE, CREATE_INDEX, DROP_INDEX, CREATE_DATABASE, ALTER_DATABASE, DROP_DATABASE, USE_DATABASE, SET_VARIABLE};
        }
    }
}
