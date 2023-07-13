package io.debezium.relational;

import io.debezium.annotation.Immutable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Immutable
public interface Table {
    static TableEditor editor() {
        return new TableEditorImpl();
    }

    TableId id();

    List<String> primaryKeyColumnNames();

    default List<Column> primaryKeyColumns() {
        List<Column> pkColumns = (List) this.primaryKeyColumnNames().stream().map(this::columnWithName).collect(Collectors.toList());
        return Collections.unmodifiableList(pkColumns);
    }

    default List<Column> filterColumns(Predicate<Column> predicate) {
        return (List) this.columns().stream().filter(predicate).collect(Collectors.toList());
    }

    List<String> retrieveColumnNames();

    List<Column> columns();

    Column columnWithName(String var1);

    String defaultCharsetName();

    String comment();

    List<Attribute> attributes();

    Attribute attributeWithName(String var1);

    default boolean isPrimaryKeyColumn(String columnName) {
        Column column = this.columnWithName(columnName);
        return column == null ? false : this.primaryKeyColumnNames().contains(column.name());
    }

    default boolean isAutoIncremented(String columnName) {
        Column column = this.columnWithName(columnName);
        return column == null ? false : column.isAutoIncremented();
    }

    default boolean isGenerated(String columnName) {
        Column column = this.columnWithName(columnName);
        return column == null ? false : column.isGenerated();
    }

    default boolean isOptional(String columnName) {
        Column column = this.columnWithName(columnName);
        return column == null ? false : column.isOptional();
    }

    TableEditor edit();
}
