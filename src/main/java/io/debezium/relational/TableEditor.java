package io.debezium.relational;

import io.debezium.annotation.NotThreadSafe;

import java.util.List;
import java.util.stream.Collectors;

@NotThreadSafe
public interface TableEditor {
    static TableEditor noOp(TableId id) {
        return (new NoOpTableEditorImpl()).tableId(id);
    }

    TableId tableId();

    TableEditor tableId(TableId var1);

    List<Column> columns();

    default List<String> columnNames() {
        return (List) this.columns().stream().map(Column::name).collect(Collectors.toList());
    }

    Column columnWithName(String var1);

    List<String> primaryKeyColumnNames();

    default boolean hasPrimaryKey() {
        return !this.primaryKeyColumnNames().isEmpty();
    }

    default TableEditor addColumn(Column column) {
        return this.addColumns(column);
    }

    TableEditor addColumns(Column... var1);

    TableEditor addColumns(Iterable<Column> var1);

    TableEditor setColumns(Column... var1);

    TableEditor setColumns(Iterable<Column> var1);

    TableEditor removeColumn(String var1);

    TableEditor updateColumn(Column var1);

    TableEditor reorderColumn(String var1, String var2);

    TableEditor renameColumn(String var1, String var2);

    TableEditor setPrimaryKeyNames(String... var1);

    TableEditor setPrimaryKeyNames(List<String> var1);

    TableEditor setUniqueValues();

    TableEditor setDefaultCharsetName(String var1);

    TableEditor setComment(String var1);

    boolean hasDefaultCharsetName();

    boolean hasComment();

    boolean hasUniqueValues();

    List<Attribute> attributes();

    Attribute attributeWithName(String var1);

    TableEditor addAttribute(Attribute var1);

    TableEditor addAttributes(List<Attribute> var1);

    TableEditor removeAttribute(String var1);

    Table create();
}
