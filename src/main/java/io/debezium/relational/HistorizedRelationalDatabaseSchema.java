package io.debezium.relational;

import io.debezium.DebeziumException;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.relational.ddl.DdlParser;
import io.debezium.relational.history.SchemaHistory;
import io.debezium.relational.history.TableChanges;
import io.debezium.schema.HistorizedDatabaseSchema;
import io.debezium.schema.SchemaChangeEvent;
import io.debezium.spi.topic.TopicNamingStrategy;
import io.debezium.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class HistorizedRelationalDatabaseSchema extends RelationalDatabaseSchema implements HistorizedDatabaseSchema<TableId> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistorizedRelationalDatabaseSchema.class);
    protected final SchemaHistory schemaHistory;
    private final HistorizedRelationalDatabaseConnectorConfig historizedConnectorConfig;
    private boolean recoveredTables;

    protected HistorizedRelationalDatabaseSchema(HistorizedRelationalDatabaseConnectorConfig config, TopicNamingStrategy<TableId> topicNamingStrategy, Tables.TableFilter tableFilter, Tables.ColumnNameFilter columnFilter, TableSchemaBuilder schemaBuilder, boolean tableIdCaseInsensitive, Key.KeyMapper customKeysMapper) {
        super(config, topicNamingStrategy, tableFilter, columnFilter, schemaBuilder, tableIdCaseInsensitive, customKeysMapper);
        this.schemaHistory = config.getSchemaHistory();
        this.schemaHistory.start();
        this.historizedConnectorConfig = config;
    }

    public void recover(Offsets<?, ?> offsets) {
        boolean hasNonNullOffsets = offsets.getOffsets().values().stream().anyMatch(Objects::nonNull);
        if (hasNonNullOffsets) {
            if (!this.schemaHistory.exists()) {
                String msg = "The db history topic or its content is fully or partially missing. Please check database schema history topic configuration and re-execute the snapshot.";
                throw new DebeziumException(msg);
            } else {
                this.schemaHistory.recover(offsets, this.tables(), this.getDdlParser());
                this.recoveredTables = !this.tableIds().isEmpty();
                Iterator var3 = this.tableIds().iterator();

                while (var3.hasNext()) {
                    TableId tableId = (TableId) var3.next();
                    this.buildAndRegisterSchema(this.tableFor(tableId));
                }

            }
        }
    }

    public void close() {
        this.schemaHistory.stop();
    }

    public void initializeStorage() {
        if (!this.schemaHistory.storageExists()) {
            this.schemaHistory.initializeStorage();
        }

    }

    protected abstract DdlParser getDdlParser();

    protected void record(SchemaChangeEvent schemaChange, TableChanges tableChanges) {
        this.schemaHistory.record(schemaChange.getPartition(), schemaChange.getOffset(), schemaChange.getDatabase(), schemaChange.getSchema(), schemaChange.getDdl(), tableChanges, schemaChange.getTimestamp());
    }

    public boolean tableInformationComplete() {
        return this.recoveredTables;
    }

    public boolean storeOnlyCapturedTables() {
        return this.historizedConnectorConfig.storeOnlyCapturedTables();
    }

    public boolean storeOnlyCapturedDatabases() {
        return this.historizedConnectorConfig.storeOnlyCapturedDatabases();
    }

    public boolean skipUnparseableDdlStatements() {
        return this.historizedConnectorConfig.skipUnparseableDdlStatements();
    }

    public Predicate<String> ddlFilter() {
        return this.historizedConnectorConfig.ddlFilter();
    }

    public boolean isHistorized() {
        return true;
    }

    public boolean skipSchemaChangeEvent(SchemaChangeEvent event) {
        if (this.storeOnlyCapturedDatabases() && !Strings.isNullOrEmpty(event.getSchema()) && !this.historizedConnectorConfig.getTableFilters().schemaFilter().test(event.getSchema())) {
            LOGGER.debug("Skipping schema event as it belongs to a non-captured schema: '{}'", event);
            return true;
        } else {
            return false;
        }
    }
}
