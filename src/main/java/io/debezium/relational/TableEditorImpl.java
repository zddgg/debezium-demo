package io.debezium.relational;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class TableEditorImpl implements TableEditor {
    private TableId id;
    private LinkedHashMap<String, Column> sortedColumns = new LinkedHashMap();
    private final List<String> pkColumnNames = new ArrayList();
    private boolean uniqueValues = false;
    private String defaultCharsetName;
    private String comment;
    private LinkedHashMap<String, Attribute> attributes = new LinkedHashMap();

    protected TableEditorImpl() {
    }

    public TableId tableId() {
        return this.id;
    }

    public TableEditor tableId(TableId id) {
        this.id = id;
        return this;
    }

    public List<Column> columns() {
        return Collections.unmodifiableList(new ArrayList(this.sortedColumns.values()));
    }

    public Column columnWithName(String name) {
        return (Column) this.sortedColumns.get(name.toLowerCase());
    }

    protected boolean hasColumnWithName(String name) {
        return this.columnWithName(name) != null;
    }

    public List<String> primaryKeyColumnNames() {
        return this.uniqueValues ? this.columnNames() : Collections.unmodifiableList(this.pkColumnNames);
    }

    public TableEditor addColumns(Column... columns) {
        Column[] var2 = columns;
        int var3 = columns.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Column column = var2[var4];
            this.add(column);
        }

        assert this.positionsAreValid();

        return this;
    }

    public TableEditor addColumns(Iterable<Column> columns) {
        columns.forEach(this::add);

        assert this.positionsAreValid();

        return this;
    }

    protected void add(Column defn) {
        if (defn != null) {
            Column existing = this.columnWithName(defn.name());
            int position = existing != null ? existing.position() : this.sortedColumns.size() + 1;
            this.sortedColumns.put(defn.name().toLowerCase(), defn.edit().position(position).create());
        }

        assert this.positionsAreValid();

    }

    public TableEditor setColumns(Column... columns) {
        this.sortedColumns.clear();
        this.addColumns(columns);

        assert this.positionsAreValid();

        return this;
    }

    public TableEditor setColumns(Iterable<Column> columns) {
        this.sortedColumns.clear();
        this.addColumns(columns);

        assert this.positionsAreValid();

        return this;
    }

    protected void updatePrimaryKeys() {
        if (!this.uniqueValues) {
            this.pkColumnNames.removeIf((pkColumnName) -> {
                boolean pkColumnDoesNotExists = !this.hasColumnWithName(pkColumnName);
                if (pkColumnDoesNotExists) {
                    throw new IllegalArgumentException("The column \"" + pkColumnName + "\" is referenced as PRIMARY KEY, but a matching column is not defined in table \"" + this.tableId() + "\"!");
                } else {
                    return pkColumnDoesNotExists;
                }
            });
        }

    }

    public TableEditor setPrimaryKeyNames(String... pkColumnNames) {
        return this.setPrimaryKeyNames(Arrays.asList(pkColumnNames));
    }

    public TableEditor setPrimaryKeyNames(List<String> pkColumnNames) {
        this.pkColumnNames.clear();
        this.pkColumnNames.addAll(pkColumnNames);
        this.uniqueValues = false;
        return this;
    }

    public TableEditor setUniqueValues() {
        this.pkColumnNames.clear();
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
        Column existing = (Column) this.sortedColumns.remove(columnName.toLowerCase());
        if (existing != null) {
            this.updatePositions();
            columnName = existing.name();
        }

        assert this.positionsAreValid();

        this.pkColumnNames.remove(columnName);
        return this;
    }

    public TableEditor updateColumn(Column newColumn) {
        this.setColumns((Iterable) this.columns().stream().map((c) -> {
            return c.name().equals(newColumn.name()) ? newColumn : c;
        }).collect(Collectors.toList()));
        return this;
    }

    public TableEditor reorderColumn(String columnName, String afterColumnName) {
        Column columnToMove = this.columnWithName(columnName);
        if (columnToMove == null) {
            throw new IllegalArgumentException("No column with name '" + columnName + "'");
        } else {
            Column afterColumn = afterColumnName == null ? null : this.columnWithName(afterColumnName);
            if (afterColumn != null && afterColumn.position() == this.sortedColumns.size()) {
                this.sortedColumns.remove(columnName);
                this.sortedColumns.put(columnName, columnToMove);
            } else {
                LinkedHashMap<String, Column> newColumns = new LinkedHashMap();
                this.sortedColumns.remove(columnName.toLowerCase());
                if (afterColumn == null) {
                    newColumns.put(columnToMove.name().toLowerCase(), columnToMove);
                }

                this.sortedColumns.forEach((key, defn) -> {
                    newColumns.put(key, defn);
                    if (defn == afterColumn) {
                        newColumns.put(columnToMove.name().toLowerCase(), columnToMove);
                    }

                });
                this.sortedColumns = newColumns;
            }

            this.updatePositions();
            return this;
        }
    }

    public TableEditor renameColumn(String existingName, String newName) {
        Column existing = this.columnWithName(existingName);
        if (existing == null) {
            throw new IllegalArgumentException("No column with name '" + existingName + "'");
        } else {
            Column newColumn = existing.edit().name(newName).create();
            List<String> newPkNames = null;
            if (!this.hasUniqueValues() && this.primaryKeyColumnNames().contains(existing.name())) {
                newPkNames = new ArrayList(this.primaryKeyColumnNames());
                newPkNames.replaceAll((name) -> {
                    return existing.name().equals(name) ? newName : name;
                });
            }

            if (!existingName.equalsIgnoreCase(newName)) {
                this.addColumn(newColumn);
                this.reorderColumn(newColumn.name(), existing.name());
                this.removeColumn(existing.name());
            } else {
                this.sortedColumns.replace(existingName.toLowerCase(), existing, newColumn);
            }

            if (newPkNames != null) {
                this.setPrimaryKeyNames((List) newPkNames);
            }

            return this;
        }
    }

    public List<Attribute> attributes() {
        return Collections.unmodifiableList(new ArrayList(this.attributes.values()));
    }

    public Attribute attributeWithName(String attributeName) {
        return (Attribute) this.attributes.get(attributeName.toLowerCase());
    }

    public TableEditor addAttribute(Attribute attribute) {
        if (attribute != null) {
            this.attributes.put(attribute.name().toLowerCase(), attribute);
        }

        return this;
    }

    public TableEditor addAttributes(List<Attribute> attributes) {
        Iterator var2 = attributes.iterator();

        while (var2.hasNext()) {
            Attribute attribute = (Attribute) var2.next();
            this.addAttribute(attribute);
        }

        return this;
    }

    public TableEditor removeAttribute(String attributeName) {
        if (attributeName != null) {
            this.attributes.remove(attributeName.toLowerCase());
        }

        return this;
    }

    protected void updatePositions() {
        AtomicInteger position = new AtomicInteger(1);
        this.sortedColumns.replaceAll((name, defn) -> {
            int nextPosition = position.getAndIncrement();
            return defn.position() != nextPosition ? defn.edit().position(nextPosition).create() : defn;
        });
    }

    protected boolean positionsAreValid() {
        AtomicInteger position = new AtomicInteger(1);
        return this.sortedColumns.values().stream().allMatch((defn) -> {
            return defn.position() >= position.getAndSet(defn.position() + 1);
        });
    }

    public String toString() {
        return this.create().toString();
    }

    public Table create() {
        if (this.id == null) {
            throw new IllegalStateException("Unable to create a table from an editor that has no table ID");
        } else {
            List<Column> columns = new ArrayList();
            this.sortedColumns.values().forEach((column) -> {
                column = column.edit().charsetNameOfTable(this.defaultCharsetName).create();
                columns.add(column);
            });
            this.updatePrimaryKeys();
            List<Attribute> attributes = new ArrayList(this.attributes.values());
            return new TableImpl(this.id, columns, this.primaryKeyColumnNames(), this.defaultCharsetName, this.comment, attributes);
        }
    }
}
