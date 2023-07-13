package io.debezium.relational;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class NoOpTableEditorImpl implements TableEditor {
    private TableId id;
    private boolean uniqueValues = false;
    private String defaultCharsetName;
    private String comment;

    protected NoOpTableEditorImpl() {
    }

    public TableId tableId() {
        return this.id;
    }

    public TableEditor tableId(TableId id) {
        this.id = id;
        return this;
    }

    public List<Column> columns() {
        return Collections.emptyList();
    }

    public Column columnWithName(String name) {
        return null;
    }

    protected boolean hasColumnWithName(String name) {
        return false;
    }

    public List<String> primaryKeyColumnNames() {
        return Collections.emptyList();
    }

    public TableEditor addColumns(Column... columns) {
        return this;
    }

    public TableEditor addColumns(Iterable<Column> columns) {
        return this;
    }

    public TableEditor setColumns(Column... columns) {
        return this;
    }

    public TableEditor setColumns(Iterable<Column> columns) {
        return this;
    }

    public TableEditor setPrimaryKeyNames(String... pkColumnNames) {
        return this;
    }

    public TableEditor setPrimaryKeyNames(List<String> pkColumnNames) {
        return this;
    }

    public TableEditor setUniqueValues() {
        this.uniqueValues = true;
        return this;
    }

    public boolean hasUniqueValues() {
        return this.uniqueValues;
    }

    public TableEditor setDefaultCharsetName(String charsetName) {
        this.defaultCharsetName = charsetName;
        return this;
    }

    public TableEditor setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean hasDefaultCharsetName() {
        return this.defaultCharsetName != null && !this.defaultCharsetName.trim().isEmpty();
    }

    public boolean hasComment() {
        return this.comment != null && !this.comment.trim().isEmpty();
    }

    public TableEditor removeColumn(String columnName) {
        return this;
    }

    public TableEditor updateColumn(Column column) {
        return this;
    }

    public TableEditor reorderColumn(String columnName, String afterColumnName) {
        return this;
    }

    public TableEditor renameColumn(String existingName, String newName) {
        return this;
    }

    public List<Attribute> attributes() {
        return Collections.emptyList();
    }

    public Attribute attributeWithName(String attributeName) {
        return null;
    }

    public TableEditor addAttribute(Attribute attribute) {
        return this;
    }

    public TableEditor addAttributes(List<Attribute> attributes) {
        return this;
    }

    public TableEditor removeAttribute(String attributeName) {
        return this;
    }

    public String toString() {
        return this.create().toString();
    }

    public Table create() {
        if (this.id == null) {
            throw new IllegalStateException("Unable to create a table from an editor that has no table ID");
        } else {
            List<Column> columns = new ArrayList();
            List<Attribute> attributes = new ArrayList();
            return new TableImpl(this.id, columns, this.primaryKeyColumnNames(), this.defaultCharsetName, this.comment, attributes);
        }
    }
}
