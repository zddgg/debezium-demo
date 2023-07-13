package io.debezium.relational;

import io.debezium.annotation.Immutable;
import io.debezium.util.Strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Immutable
public final class ColumnId implements Comparable<ColumnId> {
    private static final Pattern IDENTIFIER_SEPARATOR_PATTERN = Pattern.compile("\\.");
    private final TableId tableId;
    private final String columnName;
    private final String id;

    public static Map<TableId, Predicate<Column>> filter(String columnExcludeList) {
        Set<ColumnId> columnExclusions = columnExcludeList == null ? null : Strings.setOf(columnExcludeList, ColumnId::parse);
        Map<TableId, Set<String>> excludedColumnNamesByTable = new HashMap();
        if (null != columnExclusions) {
            columnExclusions.forEach((columnId) -> {
                excludedColumnNamesByTable.compute(columnId.tableId(), (tableId, columns) -> {
                    if (columns == null) {
                        columns = new HashSet();
                    }

                    ((Set) columns).add(columnId.columnName().toLowerCase());
                    return (Set) columns;
                });
            });
        }

        Map<TableId, Predicate<Column>> exclusionFilterByTable = new HashMap();
        excludedColumnNamesByTable.forEach((tableId, excludedColumnNames) -> {
            exclusionFilterByTable.put(tableId, (col) -> {
                return !excludedColumnNames.contains(col.name().toLowerCase());
            });
        });
        return exclusionFilterByTable;
    }

    public static ColumnId parse(String str) {
        return parse(str, true);
    }

    private static ColumnId parse(String str, boolean useCatalogBeforeSchema) {
        String[] parts = IDENTIFIER_SEPARATOR_PATTERN.split(str);
        if (parts.length < 2) {
            return null;
        } else {
            TableId tableId = TableId.parse(parts, parts.length - 1, useCatalogBeforeSchema);
            return tableId == null ? null : new ColumnId(tableId, parts[parts.length - 1]);
        }
    }

    public ColumnId(TableId tableId, String columnName) {
        this.tableId = tableId;
        this.columnName = columnName;

        assert this.tableId != null;

        assert this.columnName != null;

        this.id = columnId(this.tableId, this.columnName);
    }

    public ColumnId(String catalogName, String schemaName, String tableName, String columnName) {
        this(new TableId(catalogName, schemaName, tableName), columnName);
    }

    public TableId tableId() {
        return this.tableId;
    }

    public String catalog() {
        return this.tableId.catalog();
    }

    public String schema() {
        return this.tableId.schema();
    }

    public String table() {
        return this.tableId.table();
    }

    public String columnName() {
        return this.columnName;
    }

    public int compareTo(ColumnId that) {
        return this == that ? 0 : this.id.compareTo(that.id);
    }

    public int compareToIgnoreCase(ColumnId that) {
        return this == that ? 0 : this.id.compareToIgnoreCase(that.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ColumnId) {
            return this.compareTo((ColumnId) obj) == 0;
        } else {
            return false;
        }
    }

    public String toString() {
        return this.id;
    }

    private static String columnId(TableId tableId, String columnName) {
        String var10000 = tableId.toString();
        return var10000 + "." + columnName;
    }
}
