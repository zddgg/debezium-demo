package io.debezium.relational;

import io.debezium.relational.mapping.ColumnMappers;
import io.debezium.schema.DatabaseSchema;
import io.debezium.spi.topic.TopicNamingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class RelationalDatabaseSchema implements DatabaseSchema<TableId> {
    private static final Logger LOG = LoggerFactory.getLogger(RelationalDatabaseSchema.class);
    private final TopicNamingStrategy<TableId> topicNamingStrategy;
    private final TableSchemaBuilder schemaBuilder;
    private final Tables.TableFilter tableFilter;
    private final Tables.ColumnNameFilter columnFilter;
    private final ColumnMappers columnMappers;
    private final Key.KeyMapper customKeysMapper;
    private final SchemasByTableId schemasByTableId;
    private final Tables tables;

    protected RelationalDatabaseSchema(RelationalDatabaseConnectorConfig config, TopicNamingStrategy<TableId> topicNamingStrategy, Tables.TableFilter tableFilter, Tables.ColumnNameFilter columnFilter, TableSchemaBuilder schemaBuilder, boolean tableIdCaseInsensitive, Key.KeyMapper customKeysMapper) {
        this.topicNamingStrategy = topicNamingStrategy;
        this.schemaBuilder = schemaBuilder;
        this.tableFilter = tableFilter;
        this.columnFilter = columnFilter;
        this.columnMappers = ColumnMappers.create(config);
        this.customKeysMapper = customKeysMapper;
        this.schemasByTableId = new SchemasByTableId(tableIdCaseInsensitive);
        this.tables = new Tables(tableIdCaseInsensitive);
    }

    public void close() {
    }

    public Set<TableId> tableIds() {
        return this.tables.subset(this.tableFilter).tableIds();
    }

    public void assureNonEmptySchema() {
        if (this.tableIds().isEmpty()) {
            LOG.warn("After applying the include/exclude list filters, no changes will be captured. Please check your configuration!");
        }

    }

    public TableSchema schemaFor(TableId id) {
        return this.schemasByTableId.get(id);
    }

    public Table tableFor(TableId id) {
        return this.tableFilter.isIncluded(id) ? this.tables.forTable(id) : null;
    }

    public boolean isHistorized() {
        return false;
    }

    protected Tables tables() {
        return this.tables;
    }

    protected void clearSchemas() {
        this.schemasByTableId.clear();
    }

    protected void buildAndRegisterSchema(Table table) {
        if (this.tableFilter.isIncluded(table.id())) {
            TableSchema schema = this.schemaBuilder.create(this.topicNamingStrategy, table, this.columnFilter, this.columnMappers, this.customKeysMapper);
            this.schemasByTableId.put(table.id(), schema);
        }

    }

    protected void removeSchema(TableId id) {
        this.schemasByTableId.remove(id);
    }

    protected Tables.TableFilter getTableFilter() {
        return this.tableFilter;
    }

    public boolean tableInformationComplete() {
        return false;
    }

    public void refresh(Table table) {
        this.tables().overwriteTable(table);
        this.refreshSchema(table.id());
    }

    protected void refreshSchema(TableId id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshing DB schema for table '{}'", id);
        }

        Table table = this.tableFor(id);
        this.buildAndRegisterSchema(table);
    }

    private static class SchemasByTableId {
        private final boolean tableIdCaseInsensitive;
        private final ConcurrentMap<TableId, TableSchema> values;

        SchemasByTableId(boolean tableIdCaseInsensitive) {
            this.tableIdCaseInsensitive = tableIdCaseInsensitive;
            this.values = new ConcurrentHashMap();
        }

        public void clear() {
            this.values.clear();
        }

        public TableSchema remove(TableId tableId) {
            return (TableSchema) this.values.remove(this.toLowerCaseIfNeeded(tableId));
        }

        public TableSchema get(TableId tableId) {
            return (TableSchema) this.values.get(this.toLowerCaseIfNeeded(tableId));
        }

        public TableSchema put(TableId tableId, TableSchema updated) {
            return (TableSchema) this.values.put(this.toLowerCaseIfNeeded(tableId), updated);
        }

        private TableId toLowerCaseIfNeeded(TableId tableId) {
            return this.tableIdCaseInsensitive ? tableId.toLowercase() : tableId;
        }
    }
}
