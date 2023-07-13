package io.debezium.relational;

import io.debezium.config.ConfigDefinition;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.function.Predicates;
import io.debezium.relational.history.HistoryRecordComparator;
import io.debezium.relational.history.SchemaHistory;
import io.debezium.relational.history.SchemaHistoryMetrics;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.function.Predicate;

public abstract class HistorizedRelationalDatabaseConnectorConfig extends RelationalDatabaseConnectorConfig {
    protected static final int DEFAULT_SNAPSHOT_FETCH_SIZE = 2000;
    private static final String DEFAULT_SCHEMA_HISTORY = "io.debezium.storage.kafka.history.KafkaSchemaHistory";
    private final boolean useCatalogBeforeSchema;
    private final Class<? extends SourceConnector> connectorClass;
    private final boolean multiPartitionMode;
    private final Predicate<String> ddlFilter;
    protected boolean skipUnparseableDDL;
    protected boolean storeOnlyCapturedTablesDdl;
    protected boolean storeOnlyCapturedDatabasesDdl;
    public static final Field SCHEMA_HISTORY;
    public static final Field SKIP_UNPARSEABLE_DDL_STATEMENTS;
    public static final Field STORE_ONLY_CAPTURED_TABLES_DDL;
    public static final Field STORE_ONLY_CAPTURED_DATABASES_DDL;
    protected static final ConfigDefinition CONFIG_DEFINITION;

    protected HistorizedRelationalDatabaseConnectorConfig(Class<? extends SourceConnector> connectorClass, Configuration config, Tables.TableFilter systemTablesFilter, boolean useCatalogBeforeSchema, int defaultSnapshotFetchSize, ColumnFilterMode columnFilterMode, boolean multiPartitionMode) {
        this(connectorClass, config, systemTablesFilter, TableId::toString, useCatalogBeforeSchema, defaultSnapshotFetchSize, columnFilterMode, multiPartitionMode);
    }

    protected HistorizedRelationalDatabaseConnectorConfig(Class<? extends SourceConnector> connectorClass, Configuration config, Tables.TableFilter systemTablesFilter, Selectors.TableIdToStringMapper tableIdMapper, boolean useCatalogBeforeSchema, ColumnFilterMode columnFilterMode, boolean multiPartitionMode) {
        this(connectorClass, config, systemTablesFilter, tableIdMapper, useCatalogBeforeSchema, 2000, columnFilterMode, multiPartitionMode);
    }

    protected HistorizedRelationalDatabaseConnectorConfig(Class<? extends SourceConnector> connectorClass, Configuration config, Tables.TableFilter systemTablesFilter, Selectors.TableIdToStringMapper tableIdMapper, boolean useCatalogBeforeSchema, int defaultSnapshotFetchSize, ColumnFilterMode columnFilterMode, boolean multiPartitionMode) {
        super(config, systemTablesFilter, tableIdMapper, defaultSnapshotFetchSize, columnFilterMode, useCatalogBeforeSchema);
        this.useCatalogBeforeSchema = useCatalogBeforeSchema;
        this.connectorClass = connectorClass;
        this.multiPartitionMode = multiPartitionMode;
        this.ddlFilter = this.createDdlFilter(config);
        this.skipUnparseableDDL = config.getBoolean(SKIP_UNPARSEABLE_DDL_STATEMENTS);
        this.storeOnlyCapturedTablesDdl = config.getBoolean(STORE_ONLY_CAPTURED_TABLES_DDL);
        this.storeOnlyCapturedDatabasesDdl = config.getBoolean(STORE_ONLY_CAPTURED_DATABASES_DDL);
    }

    public SchemaHistory getSchemaHistory() {
        Configuration config = this.getConfig();
        SchemaHistory schemaHistory = (SchemaHistory) config.getInstance(SCHEMA_HISTORY, SchemaHistory.class);
        if (schemaHistory == null) {
            throw new ConnectException("Unable to instantiate the database schema history class " + config.getString(SCHEMA_HISTORY));
        } else {
            Configuration schemaHistoryConfig = ((Configuration.Builder) ((Configuration.Builder) ((Configuration.Builder) ((Configuration.Builder) config.subset("schema.history.internal.", false).edit().with(config.subset("internal.schema.history.internal.", false))).withDefault((Field) SchemaHistory.NAME, this.getLogicalName() + "-schemahistory")).withDefault((Field) SchemaHistory.INTERNAL_CONNECTOR_CLASS, this.connectorClass.getName())).withDefault((Field) SchemaHistory.INTERNAL_CONNECTOR_ID, this.logicalName)).build();
            HistoryRecordComparator historyComparator = this.getHistoryRecordComparator();
            schemaHistory.configure(schemaHistoryConfig, historyComparator, new SchemaHistoryMetrics(this, this.multiPartitionMode()), this.useCatalogBeforeSchema());
            return schemaHistory;
        }
    }

    public boolean useCatalogBeforeSchema() {
        return this.useCatalogBeforeSchema;
    }

    public boolean multiPartitionMode() {
        return this.multiPartitionMode;
    }

    private Predicate<String> createDdlFilter(Configuration config) {
        String ddlFilter = config.getString(SchemaHistory.DDL_FILTER);
        return ddlFilter != null ? Predicates.includes(ddlFilter, 34) : (x) -> {
            return false;
        };
    }

    public Predicate<String> ddlFilter() {
        return this.ddlFilter;
    }

    public boolean skipUnparseableDdlStatements() {
        return this.skipUnparseableDDL;
    }

    public boolean storeOnlyCapturedTables() {
        return this.storeOnlyCapturedTablesDdl;
    }

    public boolean storeOnlyCapturedDatabases() {
        return this.storeOnlyCapturedDatabasesDdl;
    }

    protected abstract HistoryRecordComparator getHistoryRecordComparator();

    static {
        SCHEMA_HISTORY = Field.create("schema.history.internal").withDisplayName("Database schema history class").withType(Type.CLASS).withWidth(Width.LONG).withImportance(Importance.LOW).withInvisibleRecommender().withDescription("The name of the SchemaHistory class that should be used to store and recover database schema changes. The configuration properties for the history are prefixed with the 'schema.history.internal.' string.").withDefault("io.debezium.storage.kafka.history.KafkaSchemaHistory");
        SKIP_UNPARSEABLE_DDL_STATEMENTS = SchemaHistory.SKIP_UNPARSEABLE_DDL_STATEMENTS;
        STORE_ONLY_CAPTURED_TABLES_DDL = SchemaHistory.STORE_ONLY_CAPTURED_TABLES_DDL;
        STORE_ONLY_CAPTURED_DATABASES_DDL = SchemaHistory.STORE_ONLY_CAPTURED_DATABASES_DDL;
        CONFIG_DEFINITION = RelationalDatabaseConnectorConfig.CONFIG_DEFINITION.edit().history(SCHEMA_HISTORY, SKIP_UNPARSEABLE_DDL_STATEMENTS, STORE_ONLY_CAPTURED_TABLES_DDL, STORE_ONLY_CAPTURED_DATABASES_DDL).create();
    }
}
