package io.debezium.relational;

import io.debezium.config.*;
import io.debezium.heartbeat.DatabaseHeartbeatImpl;
import io.debezium.heartbeat.Heartbeat;
import io.debezium.heartbeat.HeartbeatConnectionProvider;
import io.debezium.heartbeat.HeartbeatErrorHandler;
import io.debezium.jdbc.JdbcConfiguration;
import io.debezium.jdbc.JdbcValueConverters;
import io.debezium.jdbc.TemporalPrecisionMode;
import io.debezium.schema.FieldNameSelector;
import io.debezium.schema.SchemaNameAdjuster;
import io.debezium.spi.topic.TopicNamingStrategy;
import io.debezium.util.Strings;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class RelationalDatabaseConnectorConfig extends CommonConnectorConfig {
    protected static final String SCHEMA_INCLUDE_LIST_NAME = "schema.include.list";
    protected static final String SCHEMA_EXCLUDE_LIST_NAME = "schema.exclude.list";
    protected static final String DATABASE_INCLUDE_LIST_NAME = "database.include.list";
    protected static final String DATABASE_EXCLUDE_LIST_NAME = "database.exclude.list";
    protected static final String TABLE_EXCLUDE_LIST_NAME = "table.exclude.list";
    protected static final String TABLE_INCLUDE_LIST_NAME = "table.include.list";
    public static final String TABLE_INCLUDE_LIST_ALREADY_SPECIFIED_ERROR_MSG = "\"table.include.list\" is already specified";
    public static final String COLUMN_INCLUDE_LIST_ALREADY_SPECIFIED_ERROR_MSG = "\"column.include.list\" is already specified";
    public static final String SCHEMA_INCLUDE_LIST_ALREADY_SPECIFIED_ERROR_MSG = "\"schema.include.list\" is already specified";
    public static final String DATABASE_INCLUDE_LIST_ALREADY_SPECIFIED_ERROR_MSG = "\"database.include.list\" is already specified";
    public static final long DEFAULT_SNAPSHOT_LOCK_TIMEOUT_MILLIS;
    public static final String DEFAULT_UNAVAILABLE_VALUE_PLACEHOLDER = "__debezium_unavailable_value";
    public static final Pattern HOSTNAME_PATTERN;
    public static final Field HOSTNAME;
    public static final Field PORT;
    public static final Field USER;
    public static final Field PASSWORD;
    public static final Field DATABASE_NAME;
    public static final Field TABLE_INCLUDE_LIST;
    public static final Field TABLE_EXCLUDE_LIST;
    public static final Field TABLE_IGNORE_BUILTIN;
    public static final Field COLUMN_EXCLUDE_LIST;
    public static final Field COLUMN_INCLUDE_LIST;
    public static final Field MSG_KEY_COLUMNS;
    public static final Field DECIMAL_HANDLING_MODE;
    public static final Field SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE;
    public static final Field SCHEMA_INCLUDE_LIST;
    public static final Field SCHEMA_EXCLUDE_LIST;
    public static final Field DATABASE_INCLUDE_LIST;
    public static final Field DATABASE_EXCLUDE_LIST;
    public static final Field TIME_PRECISION_MODE;
    public static final Field SNAPSHOT_LOCK_TIMEOUT_MS;
    public static final Field INCLUDE_SCHEMA_CHANGES;
    public static final Field INCLUDE_SCHEMA_COMMENTS;
    public static final Field MASK_COLUMN_WITH_HASH;
    public static final Field MASK_COLUMN;
    public static final Field TRUNCATE_COLUMN;
    public static final Field PROPAGATE_COLUMN_SOURCE_TYPE;
    public static final Field PROPAGATE_DATATYPE_SOURCE_TYPE;
    public static final Field SNAPSHOT_FULL_COLUMN_SCAN_FORCE;
    public static final Field UNAVAILABLE_VALUE_PLACEHOLDER;
    public static final Field SNAPSHOT_TABLES_ORDER_BY_ROW_COUNT;
    protected static final ConfigDefinition CONFIG_DEFINITION;
    private final RelationalTableFilters tableFilters;
    private final Tables.ColumnNameFilter columnFilter;
    private final boolean columnsFiltered;
    private final TemporalPrecisionMode temporalPrecisionMode;
    private final Key.KeyMapper keyMapper;
    private final Selectors.TableIdToStringMapper tableIdMapper;
    private final JdbcConfiguration jdbcConfig;
    private final String heartbeatActionQuery;
    private final FieldNameSelector.FieldNamer<Column> fieldNamer;
    private final SnapshotTablesRowCountOrder snapshotOrderByRowCount;

    protected RelationalDatabaseConnectorConfig(Configuration config, Tables.TableFilter systemTablesFilter, Selectors.TableIdToStringMapper tableIdMapper, int defaultSnapshotFetchSize, ColumnFilterMode columnFilterMode, boolean useCatalogBeforeSchema) {
        super(config, defaultSnapshotFetchSize);
        this.temporalPrecisionMode = TemporalPrecisionMode.parse(config.getString(TIME_PRECISION_MODE));
        this.keyMapper = Key.CustomKeyMapper.getInstance(config.getString(MSG_KEY_COLUMNS), tableIdMapper);
        this.tableIdMapper = tableIdMapper;
        this.jdbcConfig = JdbcConfiguration.adapt(config.subset("database.", true).merge(config.subset("driver.", true)));
        if (systemTablesFilter != null && tableIdMapper != null) {
            this.tableFilters = new RelationalTableFilters(config, systemTablesFilter, tableIdMapper, useCatalogBeforeSchema);
        } else {
            this.tableFilters = null;
        }

        String columnExcludeList = config.getString(COLUMN_EXCLUDE_LIST);
        String columnIncludeList = config.getString(COLUMN_INCLUDE_LIST);
        this.columnsFiltered = !Strings.isNullOrEmpty(columnExcludeList) || !Strings.isNullOrEmpty(columnIncludeList);
        if (columnIncludeList != null) {
            this.columnFilter = Tables.ColumnNameFilterFactory.createIncludeListFilter(columnIncludeList, columnFilterMode);
        } else {
            this.columnFilter = Tables.ColumnNameFilterFactory.createExcludeListFilter(columnExcludeList, columnFilterMode);
        }

        this.heartbeatActionQuery = config.getString("heartbeat.action.query", "");
        this.fieldNamer = FieldNameSelector.defaultSelector(this.fieldNameAdjuster());
        this.snapshotOrderByRowCount = SnapshotTablesRowCountOrder.parse(config.getString(SNAPSHOT_TABLES_ORDER_BY_ROW_COUNT));
    }

    public RelationalTableFilters getTableFilters() {
        return this.tableFilters;
    }

    public JdbcValueConverters.DecimalMode getDecimalMode() {
        return DecimalHandlingMode.parse(this.getConfig().getString(DECIMAL_HANDLING_MODE)).asDecimalMode();
    }

    public TemporalPrecisionMode getTemporalPrecisionMode() {
        return this.temporalPrecisionMode;
    }

    public Key.KeyMapper getKeyMapper() {
        return this.keyMapper;
    }

    public JdbcConfiguration getJdbcConfig() {
        return this.jdbcConfig;
    }

    public String getHeartbeatActionQuery() {
        return this.heartbeatActionQuery;
    }

    public byte[] getUnavailableValuePlaceholder() {
        return this.getConfig().getString(UNAVAILABLE_VALUE_PLACEHOLDER).getBytes();
    }

    public Duration snapshotLockTimeout() {
        return Duration.ofMillis(this.getConfig().getLong(SNAPSHOT_LOCK_TIMEOUT_MS));
    }

    public String schemaExcludeList() {
        return this.getConfig().getString(SCHEMA_EXCLUDE_LIST);
    }

    public String schemaIncludeList() {
        return this.getConfig().getString(SCHEMA_INCLUDE_LIST);
    }

    public String tableExcludeList() {
        return this.getConfig().getString(TABLE_EXCLUDE_LIST);
    }

    public String tableIncludeList() {
        return this.getConfig().getString(TABLE_INCLUDE_LIST);
    }

    public Tables.ColumnNameFilter getColumnFilter() {
        return this.columnFilter;
    }

    public boolean isColumnsFiltered() {
        return this.columnsFiltered;
    }

    public Boolean isFullColumnScanRequired() {
        return this.getConfig().getBoolean(SNAPSHOT_FULL_COLUMN_SCAN_FORCE);
    }

    public SnapshotTablesRowCountOrder snapshotOrderByRowCount() {
        return this.snapshotOrderByRowCount;
    }

    private static int validateColumnExcludeList(Configuration config, Field field, Field.ValidationOutput problems) {
        String includeList = config.getString(COLUMN_INCLUDE_LIST);
        String excludeList = config.getString(COLUMN_EXCLUDE_LIST);
        if (includeList != null && excludeList != null) {
            problems.accept(COLUMN_EXCLUDE_LIST, excludeList, "\"column.include.list\" is already specified");
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isSchemaChangesHistoryEnabled() {
        return this.getConfig().getBoolean(INCLUDE_SCHEMA_CHANGES);
    }

    public boolean isSchemaCommentsHistoryEnabled() {
        return this.getConfig().getBoolean(INCLUDE_SCHEMA_COMMENTS);
    }

    public Selectors.TableIdToStringMapper getTableIdMapper() {
        return this.tableIdMapper;
    }

    private static int validateTableBlacklist(Configuration config, Field field, Field.ValidationOutput problems) {
        String includeList = config.getString(TABLE_INCLUDE_LIST);
        String excludeList = config.getString(TABLE_EXCLUDE_LIST);
        if (includeList != null && excludeList != null) {
            problems.accept(TABLE_EXCLUDE_LIST, excludeList, "\"table.include.list\" is already specified");
            return 1;
        } else {
            return 0;
        }
    }

    private static int validateTableExcludeList(Configuration config, Field field, Field.ValidationOutput problems) {
        String includeList = config.getString(TABLE_INCLUDE_LIST);
        String excludeList = config.getString(TABLE_EXCLUDE_LIST);
        if (includeList != null && excludeList != null) {
            problems.accept(TABLE_EXCLUDE_LIST, excludeList, "\"table.include.list\" is already specified");
            return 1;
        } else {
            return 0;
        }
    }

    public Map<TableId, String> getSnapshotSelectOverridesByTable() {
        List<String> tableValues = this.getConfig().getTrimmedStrings(SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE, ",");
        if (tableValues == null) {
            return Collections.emptyMap();
        } else {
            Map<TableId, String> snapshotSelectOverridesByTable = new HashMap();
            Iterator var3 = tableValues.iterator();

            while (var3.hasNext()) {
                String table = (String) var3.next();
                snapshotSelectOverridesByTable.put(TableId.parse(table), this.getConfig().getString(SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE + "." + table));
            }

            return Collections.unmodifiableMap(snapshotSelectOverridesByTable);
        }
    }

    public Heartbeat createHeartbeat(TopicNamingStrategy topicNamingStrategy, SchemaNameAdjuster schemaNameAdjuster, HeartbeatConnectionProvider connectionProvider, HeartbeatErrorHandler errorHandler) {
        return (Heartbeat) (!Strings.isNullOrBlank(this.getHeartbeatActionQuery()) && !this.getHeartbeatInterval().isZero() ? new DatabaseHeartbeatImpl(this.getHeartbeatInterval(), topicNamingStrategy.heartbeatTopic(), this.getLogicalName(), connectionProvider.get(), this.getHeartbeatActionQuery(), errorHandler, schemaNameAdjuster) : super.createHeartbeat(topicNamingStrategy, schemaNameAdjuster, connectionProvider, errorHandler));
    }

    private static int validateSchemaExcludeList(Configuration config, Field field, Field.ValidationOutput problems) {
        String includeList = config.getString(SCHEMA_INCLUDE_LIST);
        String excludeList = config.getString(SCHEMA_EXCLUDE_LIST);
        if (includeList != null && excludeList != null) {
            problems.accept(SCHEMA_EXCLUDE_LIST, excludeList, "\"schema.include.list\" is already specified");
            return 1;
        } else {
            return 0;
        }
    }

    private static int validateDatabaseExcludeList(Configuration config, Field field, Field.ValidationOutput problems) {
        String includeList = config.getString(DATABASE_INCLUDE_LIST);
        String excludeList = config.getString(DATABASE_EXCLUDE_LIST);
        if (includeList != null && excludeList != null) {
            problems.accept(DATABASE_EXCLUDE_LIST, excludeList, "\"database.include.list\" is already specified");
            return 1;
        } else {
            return 0;
        }
    }

    private static int validateMessageKeyColumnsField(Configuration config, Field field, Field.ValidationOutput problems) {
        String msgKeyColumns = config.getString(MSG_KEY_COLUMNS);
        int problemCount = 0;
        if (msgKeyColumns != null) {
            if (msgKeyColumns.isEmpty()) {
                problems.accept(MSG_KEY_COLUMNS, "", "Must not be empty");
            }

            String[] var5 = Key.CustomKeyMapper.PATTERN_SPLIT.split(msgKeyColumns);
            int var6 = var5.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                String substring = var5[var7];
                if (!Key.CustomKeyMapper.MSG_KEY_COLUMNS_PATTERN.asPredicate().test(substring)) {
                    problems.accept(MSG_KEY_COLUMNS, substring, substring + " has an invalid format (expecting '" + Key.CustomKeyMapper.MSG_KEY_COLUMNS_PATTERN.pattern() + "')");
                    ++problemCount;
                }
            }
        }

        return problemCount;
    }

    private static int validateHostname(Configuration config, Field field, Field.ValidationOutput problems) {
        String hostName = config.getString(field);
        if (!Strings.isNullOrBlank(hostName) && !HOSTNAME_PATTERN.asPredicate().test(hostName)) {
            problems.accept(field, hostName, hostName + " has invalid format (only the underscore, hyphen, dot and alphanumeric characters are allowed)");
            return 1;
        } else {
            return 0;
        }
    }

    public FieldNameSelector.FieldNamer<Column> getFieldNamer() {
        return this.fieldNamer;
    }

    static {
        DEFAULT_SNAPSHOT_LOCK_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10L);
        HOSTNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-_.]+$");
        HOSTNAME = Field.create("database." + JdbcConfiguration.HOSTNAME).withDisplayName("Hostname").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTION, 2)).withWidth(Width.MEDIUM).withImportance(Importance.HIGH).required().withValidation(RelationalDatabaseConnectorConfig::validateHostname).withDescription("Resolvable hostname or IP address of the database server.");
        PORT = Field.create("database." + JdbcConfiguration.PORT).withDisplayName("Port").withType(Type.INT).withGroup(Field.createGroupEntry(Field.Group.CONNECTION, 3)).withWidth(Width.SHORT).withImportance(Importance.HIGH).withValidation(Field::isInteger).withDescription("Port of the database server.");
        USER = Field.create("database." + JdbcConfiguration.USER).withDisplayName("User").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTION, 4)).withWidth(Width.SHORT).withImportance(Importance.HIGH).required().withDescription("Name of the database user to be used when connecting to the database.");
        PASSWORD = Field.create("database." + JdbcConfiguration.PASSWORD).withDisplayName("Password").withType(Type.PASSWORD).withGroup(Field.createGroupEntry(Field.Group.CONNECTION, 5)).withWidth(Width.SHORT).withImportance(Importance.HIGH).withDescription("Password of the database user to be used when connecting to the database.");
        DATABASE_NAME = Field.create("database." + JdbcConfiguration.DATABASE).withDisplayName("Database").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTION, 6)).withWidth(Width.MEDIUM).withImportance(Importance.HIGH).required().withDescription("The name of the database from which the connector should capture changes");
        TABLE_INCLUDE_LIST = Field.create("table.include.list").withDisplayName("Include Tables").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 2)).withWidth(Width.LONG).withImportance(Importance.HIGH).withValidation(Field::isListOfRegex).withDescription("The tables for which changes are to be captured");
        TABLE_EXCLUDE_LIST = Field.create("table.exclude.list").withDisplayName("Exclude Tables").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 3)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(Field::isListOfRegex, RelationalDatabaseConnectorConfig::validateTableExcludeList).withDescription("A comma-separated list of regular expressions that match the fully-qualified names of tables to be excluded from monitoring");
        TABLE_IGNORE_BUILTIN = Field.create("table.ignore.builtin").withDisplayName("Ignore system databases").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 6)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault(true).withValidation(Field::isBoolean).withDescription("Flag specifying whether built-in tables should be ignored.");
        COLUMN_EXCLUDE_LIST = Field.create("column.exclude.list").withDisplayName("Exclude Columns").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 5)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(Field::isListOfRegex, RelationalDatabaseConnectorConfig::validateColumnExcludeList).withDescription("Regular expressions matching columns to exclude from change events");
        COLUMN_INCLUDE_LIST = Field.create("column.include.list").withDisplayName("Include Columns").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 4)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(Field::isListOfRegex).withDescription("Regular expressions matching columns to include in change events");
        MSG_KEY_COLUMNS = Field.create("message.key.columns").withDisplayName("Columns PK mapping").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 16)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(RelationalDatabaseConnectorConfig::validateMessageKeyColumnsField).withDescription("A semicolon-separated list of expressions that match fully-qualified tables and column(s) to be used as message key. Each expression must match the pattern '<fully-qualified table name>:<key columns>', where the table names could be defined as (DB_NAME.TABLE_NAME) or (SCHEMA_NAME.TABLE_NAME), depending on the specific connector, and the key columns are a comma-separated list of columns representing the custom key. For any table without an explicit key configuration the table's primary key column(s) will be used as message key. Example: dbserver1.inventory.orderlines:orderId,orderLineId;dbserver1.inventory.orders:id");
        DECIMAL_HANDLING_MODE = Field.create("decimal.handling.mode").withDisplayName("Decimal Handling").withGroup(Field.createGroupEntry(Field.Group.CONNECTOR, 2)).withEnum(DecimalHandlingMode.class, DecimalHandlingMode.PRECISE).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Specify how DECIMAL and NUMERIC columns should be represented in change events, including: 'precise' (the default) uses java.math.BigDecimal to represent values, which are encoded in the change events using a binary representation and Kafka Connect's 'org.apache.kafka.connect.data.Decimal' type; 'string' uses string to represent values; 'double' represents values using Java's 'double', which may not offer the precision but will be far easier to use in consumers.");
        SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE = Field.create("snapshot.select.statement.overrides").withDisplayName("List of tables where the default select statement used during snapshotting should be overridden.").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_SNAPSHOT, 8)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withDescription(" This property contains a comma-separated list of fully-qualified tables (DB_NAME.TABLE_NAME) or (SCHEMA_NAME.TABLE_NAME), depending on the specific connectors. Select statements for the individual tables are specified in further configuration properties, one for each table, identified by the id 'snapshot.select.statement.overrides.[DB_NAME].[TABLE_NAME]' or 'snapshot.select.statement.overrides.[SCHEMA_NAME].[TABLE_NAME]', respectively. The value of those properties is the select statement to use when retrieving data from the specific table during snapshotting. A possible use case for large append-only tables is setting a specific point where to start (resume) snapshotting, in case a previous snapshotting was interrupted.");
        SCHEMA_INCLUDE_LIST = Field.create("schema.include.list").withDisplayName("Include Schemas").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 0)).withWidth(Width.LONG).withImportance(Importance.HIGH).withValidation(Field::isListOfRegex).withDependents("table.include.list").withDescription("The schemas for which events should be captured");
        SCHEMA_EXCLUDE_LIST = Field.create("schema.exclude.list").withDisplayName("Exclude Schemas").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 1)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(Field::isListOfRegex, RelationalDatabaseConnectorConfig::validateSchemaExcludeList).withInvisibleRecommender().withDescription("The schemas for which events must not be captured");
        DATABASE_INCLUDE_LIST = Field.create("database.include.list").withDisplayName("Include Databases").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 0)).withWidth(Width.LONG).withImportance(Importance.HIGH).withDependents("table.include.list").withValidation(Field::isListOfRegex).withDescription("The databases for which changes are to be captured");
        DATABASE_EXCLUDE_LIST = Field.create("database.exclude.list").withDisplayName("Exclude Databases").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.FILTERS, 1)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(Field::isListOfRegex, RelationalDatabaseConnectorConfig::validateDatabaseExcludeList).withDescription("A comma-separated list of regular expressions that match database names to be excluded from monitoring");
        TIME_PRECISION_MODE = Field.create("time.precision.mode").withDisplayName("Time Precision").withGroup(Field.createGroupEntry(Field.Group.CONNECTOR, 4)).withEnum(TemporalPrecisionMode.class, TemporalPrecisionMode.ADAPTIVE).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Time, date, and timestamps can be represented with different kinds of precisions, including: 'adaptive' (the default) bases the precision of time, date, and timestamp values on the database column's precision; 'adaptive_time_microseconds' like 'adaptive' mode, but TIME fields always use microseconds precision; 'connect' always represents time, date, and timestamp values using Kafka Connect's built-in representations for Time, Date, and Timestamp, which uses millisecond precision regardless of the database columns' precision.");
        SNAPSHOT_LOCK_TIMEOUT_MS = Field.create("snapshot.lock.timeout.ms").withDisplayName("Snapshot lock timeout (ms)").withType(Type.LONG).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_SNAPSHOT, 6)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withDefault(DEFAULT_SNAPSHOT_LOCK_TIMEOUT_MILLIS).withDescription("The maximum number of millis to wait for table locks at the beginning of a snapshot. If locks cannot be acquired in this time frame, the snapshot will be aborted. Defaults to 10 seconds");
        INCLUDE_SCHEMA_CHANGES = Field.create("include.schema.changes").withDisplayName("Include database schema changes").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR, 0)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Whether the connector should publish changes in the database schema to a Kafka topic with the same name as the database server ID. Each schema change will be recorded using a key that contains the database name and whose value include logical description of the new schema and optionally the DDL statement(s). The default is 'true'. This is independent of how the connector internally records database schema history.").withDefault(true);
        INCLUDE_SCHEMA_COMMENTS = Field.create("include.schema.comments").withDisplayName("Include Table and Column Comments").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR, 5)).withValidation(Field::isBoolean).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Whether the connector parse table and column's comment to metadata object. Note: Enable this option will bring the implications on memory usage. The number and size of ColumnImpl objects is what largely impacts how much memory is consumed by the Debezium connectors, and adding a String to each of them can potentially be quite heavy. The default is 'false'.").withDefault(false);
        MASK_COLUMN_WITH_HASH = Field.create("column.mask.hash.([^.]+).with.salt.(.+)").withDisplayName("Mask Columns Using Hash and Salt").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 13)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withDescription("A comma-separated list of regular expressions matching fully-qualified names of columns that should be masked by hashing the input. Using the specified hash algorithms and salt.");
        MASK_COLUMN = Field.create("column.mask.with.(d+).chars").withDisplayName("Mask Columns With n Asterisks").withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 12)).withValidation(Field::isInteger).withDescription("A comma-separated list of regular expressions matching fully-qualified names of columns that should be masked with configured amount of asterisk ('*') characters.");
        TRUNCATE_COLUMN = Field.create("column.truncate.to.(d+).chars").withDisplayName("Truncate Columns To n Characters").withType(Type.INT).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 11)).withValidation(Field::isInteger).withDescription("A comma-separated list of regular expressions matching fully-qualified names of columns that should be truncated to the configured amount of characters.");
        PROPAGATE_COLUMN_SOURCE_TYPE = Field.create("column.propagate.source.type").withDisplayName("Propagate Source Types by Columns").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 15)).withValidation(Field::isListOfRegex).withDescription("A comma-separated list of regular expressions matching fully-qualified names of columns that adds the column’s original type and original length as parameters to the corresponding field schemas in the emitted change records.");
        PROPAGATE_DATATYPE_SOURCE_TYPE = Field.create("datatype.propagate.source.type").withDisplayName("Propagate Source Types by Data Type").withType(Type.LIST).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 14)).withValidation(Field::isListOfRegex).withDescription("A comma-separated list of regular expressions matching the database-specific data type names that adds the data type's original type and original length as parameters to the corresponding field schemas in the emitted change records.");
        SNAPSHOT_FULL_COLUMN_SCAN_FORCE = Field.createInternal("snapshot.scan.all.columns.force").withDisplayName("Snapshot force scan all columns of all tables").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_SNAPSHOT, 999)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Restore pre 1.5 behaviour and scan all tables to discover columns. If you are excluding one table then turning this on may improve performance. If you are excluding a lot of tables the default behavior should work well.").withDefault(false);
        UNAVAILABLE_VALUE_PLACEHOLDER = Field.create("unavailable.value.placeholder").withDisplayName("Unavailable value placeholder").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_ADVANCED, 20)).withWidth(Width.MEDIUM).withDefault("__debezium_unavailable_value").withImportance(Importance.MEDIUM).withDescription("Specify the constant that will be provided by Debezium to indicate that the original value is unavailable and not provided by the database.");
        SNAPSHOT_TABLES_ORDER_BY_ROW_COUNT = Field.create("snapshot.tables.order.by.row.count").withDisplayName("Initial snapshot tables order by row count").withEnum(SnapshotTablesRowCountOrder.class, SnapshotTablesRowCountOrder.DISABLED).withGroup(Field.createGroupEntry(Field.Group.CONNECTOR_SNAPSHOT, 111)).withDescription("Controls the order in which tables are processed in the initial snapshot. A `descending` value will order the tables by row count descending. A `ascending` value will order the tables by row count ascending. A value of `disabled` (the default) will disable ordering by row count.");
        CONFIG_DEFINITION = CommonConnectorConfig.CONFIG_DEFINITION.edit().type(CommonConnectorConfig.TOPIC_PREFIX).connector(DECIMAL_HANDLING_MODE, TIME_PRECISION_MODE, SNAPSHOT_LOCK_TIMEOUT_MS).events(COLUMN_INCLUDE_LIST, COLUMN_EXCLUDE_LIST, TABLE_INCLUDE_LIST, TABLE_EXCLUDE_LIST, TABLE_IGNORE_BUILTIN, SCHEMA_INCLUDE_LIST, SCHEMA_EXCLUDE_LIST, MSG_KEY_COLUMNS, SNAPSHOT_SELECT_STATEMENT_OVERRIDES_BY_TABLE, MASK_COLUMN_WITH_HASH, MASK_COLUMN, TRUNCATE_COLUMN, INCLUDE_SCHEMA_CHANGES, INCLUDE_SCHEMA_COMMENTS, PROPAGATE_COLUMN_SOURCE_TYPE, PROPAGATE_DATATYPE_SOURCE_TYPE, SNAPSHOT_FULL_COLUMN_SCAN_FORCE, SNAPSHOT_TABLES_ORDER_BY_ROW_COUNT, DatabaseHeartbeatImpl.HEARTBEAT_ACTION_QUERY).create();
    }

    public static enum SnapshotTablesRowCountOrder implements EnumeratedValue {
        ASCENDING("ascending"),
        DESCENDING("descending"),
        DISABLED("disabled");

        private final String value;

        private SnapshotTablesRowCountOrder(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static SnapshotTablesRowCountOrder parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                SnapshotTablesRowCountOrder[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    SnapshotTablesRowCountOrder option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static SnapshotTablesRowCountOrder parse(String value, String defaultValue) {
            SnapshotTablesRowCountOrder mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static SnapshotTablesRowCountOrder[] $values() {
            return new SnapshotTablesRowCountOrder[]{ASCENDING, DESCENDING, DISABLED};
        }
    }

    public static enum DecimalHandlingMode implements EnumeratedValue {
        PRECISE("precise"),
        STRING("string"),
        DOUBLE("double");

        private final String value;

        private DecimalHandlingMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public JdbcValueConverters.DecimalMode asDecimalMode() {
            switch (this) {
                case DOUBLE:
                    return JdbcValueConverters.DecimalMode.DOUBLE;
                case STRING:
                    return JdbcValueConverters.DecimalMode.STRING;
                case PRECISE:
                default:
                    return JdbcValueConverters.DecimalMode.PRECISE;
            }
        }

        public static DecimalHandlingMode parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                DecimalHandlingMode[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    DecimalHandlingMode option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static DecimalHandlingMode parse(String value, String defaultValue) {
            DecimalHandlingMode mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static DecimalHandlingMode[] $values() {
            return new DecimalHandlingMode[]{PRECISE, STRING, DOUBLE};
        }
    }
}
