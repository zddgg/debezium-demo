package io.debezium.connector.mysql;

import com.mysql.cj.jdbc.Driver;
import io.debezium.config.*;
import io.debezium.config.Field.Group;
import io.debezium.connector.AbstractSourceInfo;
import io.debezium.connector.SourceInfoStructMaker;
import io.debezium.function.Predicates;
import io.debezium.jdbc.JdbcValueConverters.BigIntUnsignedMode;
import io.debezium.jdbc.TemporalPrecisionMode;
import io.debezium.relational.ColumnFilterMode;
import io.debezium.relational.HistorizedRelationalDatabaseConnectorConfig;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables.TableFilter;
import io.debezium.relational.history.HistoryRecordComparator;
import io.debezium.schema.DefaultTopicNamingStrategy;
import io.debezium.util.Collect;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.function.Predicate;

public class MySqlConnectorConfig extends HistorizedRelationalDatabaseConnectorConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlConnectorConfig.class);
    static final String TEST_DISABLE_GLOBAL_LOCKING = "test.disable.global.locking";
    protected static final int DEFAULT_SNAPSHOT_FETCH_SIZE = Integer.MIN_VALUE;
    protected static final int DEFAULT_PORT = 3306;
    private static final int DEFAULT_BINLOG_BUFFER_SIZE = 0;
    public static final Field PORT;
    public static final Field ON_CONNECT_STATEMENTS;
    public static final Field SERVER_ID;
    public static final Field SERVER_ID_OFFSET;
    public static final Field SSL_MODE;
    public static final Field SSL_KEYSTORE;
    public static final Field SSL_KEYSTORE_PASSWORD;
    public static final Field SSL_TRUSTSTORE;
    public static final Field SSL_TRUSTSTORE_PASSWORD;
    public static final Field TABLES_IGNORE_BUILTIN;
    public static final Field JDBC_DRIVER;
    public static final Field GTID_SOURCE_INCLUDES;
    public static final Field GTID_SOURCE_EXCLUDES;
    public static final Field GTID_SOURCE_FILTER_DML_EVENTS;
    public static final Field CONNECTION_TIMEOUT_MS;
    public static final Field KEEP_ALIVE;
    public static final Field KEEP_ALIVE_INTERVAL_MS;
    public static final Field ROW_COUNT_FOR_STREAMING_RESULT_SETS;
    public static final Field BUFFER_SIZE_FOR_BINLOG_READER;
    public static final Field TOPIC_NAMING_STRATEGY;
    public static final Field INCLUDE_SQL_QUERY;
    public static final Field SNAPSHOT_MODE;
    public static final Field SNAPSHOT_LOCKING_MODE;
    public static final Field SNAPSHOT_NEW_TABLES;
    public static final Field TIME_PRECISION_MODE;
    public static final Field BIGINT_UNSIGNED_HANDLING_MODE;
    public static final Field EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE;
    public static final Field INCONSISTENT_SCHEMA_HANDLING_MODE;
    public static final Field ENABLE_TIME_ADJUSTER;
    public static final Field READ_ONLY_CONNECTION;
    public static final Field SOURCE_INFO_STRUCT_MAKER;
    public static final Field STORE_ONLY_CAPTURED_DATABASES_DDL;
    private static final ConfigDefinition CONFIG_DEFINITION;
    public static Field.Set ALL_FIELDS;
    protected static Field.Set EXPOSED_FIELDS;
    protected static final Set<String> BUILT_IN_DB_NAMES;
    private final Configuration config;
    private final SnapshotMode snapshotMode;
    private final SnapshotLockingMode snapshotLockingMode;
    private final SnapshotNewTables snapshotNewTables;
    private final TemporalPrecisionMode temporalPrecisionMode;
    private final Duration connectionTimeout;
    private final Predicate<String> gtidSourceFilter;
    private final EventProcessingFailureHandlingMode inconsistentSchemaFailureHandlingMode;
    private final boolean readOnlyConnection;

    protected static ConfigDef configDef() {
        return CONFIG_DEFINITION.configDef();
    }

    public boolean supportsOperationFiltering() {
        return true;
    }

    protected boolean supportsSchemaChangesDuringIncrementalSnapshot() {
        return true;
    }

    public MySqlConnectorConfig(Configuration config) {
        super(MySqlConnector.class, config, TableFilter.fromPredicate(MySqlConnectorConfig::isNotBuiltInTable), true, Integer.MIN_VALUE, ColumnFilterMode.CATALOG, false);
        this.config = config;
        this.temporalPrecisionMode = TemporalPrecisionMode.parse(config.getString(TIME_PRECISION_MODE));
        this.snapshotMode = SnapshotMode.parse(config.getString(SNAPSHOT_MODE), SNAPSHOT_MODE.defaultValueAsString());
        this.snapshotLockingMode = SnapshotLockingMode.parse(config.getString(SNAPSHOT_LOCKING_MODE), SNAPSHOT_LOCKING_MODE.defaultValueAsString());
        this.readOnlyConnection = config.getBoolean(READ_ONLY_CONNECTION);
        String snapshotNewTables = config.getString(SNAPSHOT_NEW_TABLES);
        this.snapshotNewTables = SnapshotNewTables.parse(snapshotNewTables, SNAPSHOT_NEW_TABLES.defaultValueAsString());
        String inconsistentSchemaFailureHandlingMode = config.getString(INCONSISTENT_SCHEMA_HANDLING_MODE);
        this.inconsistentSchemaFailureHandlingMode = EventProcessingFailureHandlingMode.parse(inconsistentSchemaFailureHandlingMode);
        this.connectionTimeout = Duration.ofMillis(config.getLong(CONNECTION_TIMEOUT_MS));
        String gtidSetIncludes = config.getString(GTID_SOURCE_INCLUDES);
        String gtidSetExcludes = config.getString(GTID_SOURCE_EXCLUDES);
        this.gtidSourceFilter = gtidSetIncludes != null ? Predicates.includesUuids(gtidSetIncludes) : (gtidSetExcludes != null ? Predicates.excludesUuids(gtidSetExcludes) : null);
        this.storeOnlyCapturedDatabasesDdl = config.getBoolean(STORE_ONLY_CAPTURED_DATABASES_DDL);
    }

    public boolean useCursorFetch() {
        return this.getSnapshotFetchSize() > 0;
    }

    public SnapshotLockingMode getSnapshotLockingMode() {
        return this.snapshotLockingMode;
    }

    public SnapshotNewTables getSnapshotNewTables() {
        return this.snapshotNewTables;
    }

    private static int validateEventDeserializationFailureHandlingModeNotSet(Configuration config, Field field, Field.ValidationOutput problems) {
        String modeName = (String) config.asMap().get(EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE.name());
        if (modeName != null) {
            LOGGER.warn("Configuration option '{}' is renamed to '{}'", EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE.name(), EVENT_PROCESSING_FAILURE_HANDLING_MODE.name());
        }

        return 0;
    }

    private static int validateGtidSetExcludes(Configuration config, Field field, Field.ValidationOutput problems) {
        String includes = config.getString(GTID_SOURCE_INCLUDES);
        String excludes = config.getString(GTID_SOURCE_EXCLUDES);
        if (includes != null && excludes != null) {
            problems.accept(GTID_SOURCE_EXCLUDES, excludes, "Included GTID source UUIDs are already specified");
            return 1;
        } else {
            return 0;
        }
    }

    private static int validateSnapshotLockingMode(Configuration config, Field field, Field.ValidationOutput problems) {
        if (config.hasKey(SNAPSHOT_LOCKING_MODE.name())) {
            SnapshotLockingMode lockingModeValue = SnapshotLockingMode.parse(config.getString(SNAPSHOT_LOCKING_MODE));
            if (lockingModeValue == null) {
                problems.accept(SNAPSHOT_LOCKING_MODE, lockingModeValue, "Must be a valid snapshot.locking.mode value");
                return 1;
            }
        }

        return 0;
    }

    private static int validateTimePrecisionMode(Configuration config, Field field, Field.ValidationOutput problems) {
        if (config.hasKey(TIME_PRECISION_MODE.name())) {
            String timePrecisionMode = config.getString(TIME_PRECISION_MODE.name());
            if (TemporalPrecisionMode.ADAPTIVE.getValue().equals(timePrecisionMode)) {
                problems.accept(TIME_PRECISION_MODE, timePrecisionMode, "The 'adaptive' time.precision.mode is no longer supported");
                return 1;
            }
        }

        return 0;
    }

    protected SourceInfoStructMaker<? extends AbstractSourceInfo> getSourceInfoStructMaker(Version version) {
        return this.getSourceInfoStructMaker(SOURCE_INFO_STRUCT_MAKER, Module.name(), Module.version(), this);
    }

    public String getContextName() {
        return Module.contextName();
    }

    public String getConnectorName() {
        return Module.name();
    }

    public TemporalPrecisionMode getTemporalPrecisionMode() {
        return this.temporalPrecisionMode;
    }

    public SnapshotMode getSnapshotMode() {
        return this.snapshotMode;
    }

    public Duration getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public EventProcessingFailureHandlingMode inconsistentSchemaFailureHandlingMode() {
        return this.inconsistentSchemaFailureHandlingMode;
    }

    public String hostname() {
        return this.config.getString(HOSTNAME);
    }

    public int port() {
        return this.config.getInteger(PORT);
    }

    public String username() {
        return this.config.getString(USER);
    }

    public String password() {
        return this.config.getString(PASSWORD);
    }

    public long serverId() {
        return this.config.getLong(SERVER_ID);
    }

    public SecureConnectionMode sslMode() {
        String mode = this.config.getString(SSL_MODE);
        return SecureConnectionMode.parse(mode);
    }

    public boolean sslModeEnabled() {
        return this.sslMode() != SecureConnectionMode.DISABLED;
    }

    public int bufferSizeForStreamingChangeEventSource() {
        return this.config.getInteger(BUFFER_SIZE_FOR_BINLOG_READER);
    }

    public Predicate<String> gtidSourceFilter() {
        return this.gtidSourceFilter;
    }

    public boolean includeSchemaChangeRecords() {
        return this.config.getBoolean(INCLUDE_SCHEMA_CHANGES);
    }

    public boolean includeSqlQuery() {
        return this.config.getBoolean(INCLUDE_SQL_QUERY);
    }

    public long rowCountForLargeTable() {
        return this.config.getLong(ROW_COUNT_FOR_STREAMING_RESULT_SETS);
    }

    protected HistoryRecordComparator getHistoryRecordComparator() {
        return new MySqlHistoryRecordComparator(this.gtidSourceFilter());
    }

    public static boolean isBuiltInDatabase(String databaseName) {
        return databaseName == null ? false : BUILT_IN_DB_NAMES.contains(databaseName.toLowerCase());
    }

    public static boolean isNotBuiltInTable(TableId id) {
        return !isBuiltInDatabase(id.catalog());
    }

    public boolean isReadOnlyConnection() {
        return this.readOnlyConnection;
    }

    boolean useGlobalLock() {
        return !"true".equals(this.config.getString("test.disable.global.locking"));
    }

    static {
        PORT = RelationalDatabaseConnectorConfig.PORT.withDefault(3306);
        ON_CONNECT_STATEMENTS = Field.create("database.initial.statements").withDisplayName("Initial statements").withType(Type.STRING).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED, 4)).withWidth(Width.LONG).withImportance(Importance.LOW).withDescription("A semicolon separated list of SQL statements to be executed when a JDBC connection (not binlog reading connection) to the database is established. Note that the connector may establish JDBC connections at its own discretion, so this should typically be used for configuration of session parameters only, but not for executing DML statements. Use doubled semicolon (';;') to use a semicolon as a character and not as a delimiter.");
        SERVER_ID = Field.create("database.server.id").withDisplayName("Cluster ID").withType(Type.LONG).withGroup(Field.createGroupEntry(Group.CONNECTION, 1)).withWidth(Width.LONG).withImportance(Importance.HIGH).required().withValidation(new Field.Validator[]{Field::isPositiveLong}).withDescription("A numeric ID of this database client, which must be unique across all currently-running database processes in the cluster. This connector joins the MySQL database cluster as another server (with this unique ID) so it can read the binlog.");
        SERVER_ID_OFFSET = Field.create("database.server.id.offset").withDisplayName("Cluster ID offset").withType(Type.LONG).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED, 0)).withWidth(Width.LONG).withImportance(Importance.HIGH).withDefault(10000L).withDescription("Only relevant if parallel snapshotting is configured. During parallel snapshotting, multiple (4) connections open to the database client, and they each need their own unique connection ID. This offset is used to generate those IDs from the base configured cluster ID.");
        SSL_MODE = Field.create("database.ssl.mode").withDisplayName("SSL mode").withEnum(SecureConnectionMode.class, SecureConnectionMode.PREFERRED).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED_SSL, 0)).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Whether to use an encrypted connection to MySQL. Options include: 'disabled' to use an unencrypted connection; 'preferred' (the default) to establish a secure (encrypted) connection if the server supports secure connections, but fall back to an unencrypted connection otherwise; 'required' to use a secure (encrypted) connection, and fail if one cannot be established; 'verify_ca' like 'required' but additionally verify the server TLS certificate against the configured Certificate Authority (CA) certificates, or fail if no valid matching CA certificates are found; or'verify_identity' like 'verify_ca' but additionally verify that the server certificate matches the host to which the connection is attempted.");
        SSL_KEYSTORE = Field.create("database.ssl.keystore").withDisplayName("SSL Keystore").withType(Type.STRING).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED_SSL, 1)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withDescription("The location of the key store file. This is optional and can be used for two-way authentication between the client and the MySQL Server.");
        SSL_KEYSTORE_PASSWORD = Field.create("database.ssl.keystore.password").withDisplayName("SSL Keystore Password").withType(Type.PASSWORD).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED_SSL, 2)).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("The password for the key store file. This is optional and only needed if 'database.ssl.keystore' is configured.");
        SSL_TRUSTSTORE = Field.create("database.ssl.truststore").withDisplayName("SSL Truststore").withType(Type.STRING).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED_SSL, 3)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withDescription("The location of the trust store file for the server certificate verification.");
        SSL_TRUSTSTORE_PASSWORD = Field.create("database.ssl.truststore.password").withDisplayName("SSL Truststore Password").withType(Type.PASSWORD).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED_SSL, 4)).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("The password for the trust store file. Used to check the integrity of the truststore, and unlock the truststore.");
        TABLES_IGNORE_BUILTIN = RelationalDatabaseConnectorConfig.TABLE_IGNORE_BUILTIN.withDependents(new String[]{"database.include.list"});
        JDBC_DRIVER = Field.create("database.jdbc.driver").withDisplayName("Jdbc Driver Class Name").withType(Type.CLASS).withGroup(Field.createGroupEntry(Group.CONNECTION, 41)).withWidth(Width.MEDIUM).withDefault(Driver.class.getName()).withImportance(Importance.LOW).withValidation(new Field.Validator[]{Field::isClassName}).withDescription("JDBC Driver class name used to connect to the MySQL database server.");
        GTID_SOURCE_INCLUDES = Field.create("gtid.source.includes").withDisplayName("Include GTID sources").withType(Type.LIST).withGroup(Field.createGroupEntry(Group.CONNECTOR, 24)).withWidth(Width.LONG).withImportance(Importance.HIGH).withDependents(new String[]{"table.include.list"}).withDescription("The source UUIDs used to include GTID ranges when determine the starting position in the MySQL server's binlog.");
        GTID_SOURCE_EXCLUDES = Field.create("gtid.source.excludes").withDisplayName("Exclude GTID sources").withType(Type.STRING).withGroup(Field.createGroupEntry(Group.CONNECTOR, 25)).withWidth(Width.LONG).withImportance(Importance.MEDIUM).withValidation(new Field.Validator[]{MySqlConnectorConfig::validateGtidSetExcludes}).withInvisibleRecommender().withDescription("The source UUIDs used to exclude GTID ranges when determine the starting position in the MySQL server's binlog.");
        GTID_SOURCE_FILTER_DML_EVENTS = Field.create("gtid.source.filter.dml.events").withDisplayName("Filter DML events").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Group.CONNECTOR, 23)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDefault(true).withDescription("If set to true, we will only produce DML events into Kafka for transactions that were written on mysql servers with UUIDs matching the filters defined by the gtid.source.includes or gtid.source.excludes configuration options, if they are specified.");
        CONNECTION_TIMEOUT_MS = Field.create("connect.timeout.ms").withDisplayName("Connection Timeout (ms)").withType(Type.INT).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED, 1)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Maximum time to wait after trying to connect to the database before timing out, given in milliseconds. Defaults to 30 seconds (30,000 ms).").withDefault(30000).withValidation(new Field.Validator[]{Field::isPositiveInteger});
        KEEP_ALIVE = Field.create("connect.keep.alive").withDisplayName("Keep connection alive (true/false)").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED, 2)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Whether a separate thread should be used to ensure the connection is kept alive.").withDefault(true).withValidation(new Field.Validator[]{Field::isBoolean});
        KEEP_ALIVE_INTERVAL_MS = Field.create("connect.keep.alive.interval.ms").withDisplayName("Keep alive interval (ms)").withType(Type.LONG).withGroup(Field.createGroupEntry(Group.CONNECTION_ADVANCED, 3)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Interval for connection checking if keep alive thread is used, given in milliseconds Defaults to 1 minute (60,000 ms).").withDefault(Duration.ofMinutes(1L).toMillis()).withValidation(new Field.Validator[]{Field::isPositiveInteger});
        ROW_COUNT_FOR_STREAMING_RESULT_SETS = Field.create("min.row.count.to.stream.results").withDisplayName("Stream result set of size").withType(Type.INT).withGroup(Field.createGroupEntry(Group.CONNECTOR_ADVANCED, 2)).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("The number of rows a table must contain to stream results rather than pull all into memory during snapshots. Defaults to 1,000. Use 0 to stream all results and completely avoid checking the size of each table.").withDefault(1000).withValidation(new Field.Validator[]{Field::isNonNegativeLong});
        BUFFER_SIZE_FOR_BINLOG_READER = Field.create("binlog.buffer.size").withDisplayName("Binlog reader buffer size").withType(Type.INT).withGroup(Field.createGroupEntry(Group.CONNECTOR_ADVANCED, 3)).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("The size of a look-ahead buffer used by the  binlog reader to decide whether the transaction in progress is going to be committed or rolled back. Use 0 to disable look-ahead buffering. Defaults to 0 (i.e. buffering is disabled).").withDefault(0).withValidation(new Field.Validator[]{Field::isNonNegativeInteger});
        TOPIC_NAMING_STRATEGY = Field.create("topic.naming.strategy").withDisplayName("Topic naming strategy class").withType(Type.CLASS).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("The name of the TopicNamingStrategy class that should be used to determine the topic name for data change, schema change, transaction, heartbeat event etc.").withDefault(DefaultTopicNamingStrategy.class.getName());
        INCLUDE_SQL_QUERY = Field.create("include.query").withDisplayName("Include original SQL query with in change events").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Group.CONNECTOR_ADVANCED, 0)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Whether the connector should include the original SQL query that generated the change event. Note: This option requires MySQL be configured with the binlog_rows_query_log_events option set to ON. Query will not be present for events generated from snapshot. WARNING: Enabling this option may expose tables or fields explicitly excluded or masked by including the original SQL statement in the change event. For this reason the default value is 'false'.").withDefault(false);
        SNAPSHOT_MODE = Field.create("snapshot.mode").withDisplayName("Snapshot mode").withEnum(SnapshotMode.class, SnapshotMode.INITIAL).withGroup(Field.createGroupEntry(Group.CONNECTOR_SNAPSHOT, 0)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("The criteria for running a snapshot upon startup of the connector. Select one of the following snapshot options: 'when_needed': On startup, the connector runs a snapshot if one is needed.; 'schema_only': If the connector does not detect any offsets for the logical server name, it runs a snapshot that captures only the schema (table structures), but not any table data. After the snapshot completes, the connector begins to stream changes from the binlog.; 'schema_only_recovery': The connector performs a snapshot that captures only the database schema history. The connector then transitions back to streaming. Use this setting to restore a corrupted or lost database schema history topic. Do not use if the database schema was modified after the connector stopped.; 'initial' (default): If the connector does not detect any offsets for the logical server name, it runs a snapshot that captures the current full state of the configured tables. After the snapshot completes, the connector begins to stream changes from the binlog.; 'initial_only': The connector performs a snapshot as it does for the 'initial' option, but after the connector completes the snapshot, it stops, and does not stream changes from the binlog.; 'never': The connector does not run a snapshot. Upon first startup, the connector immediately begins reading from the beginning of the binlog. The 'never' mode should be used with care, and only when the binlog is known to contain all history.");
        SNAPSHOT_LOCKING_MODE = Field.create("snapshot.locking.mode").withDisplayName("Snapshot locking mode").withEnum(SnapshotLockingMode.class, SnapshotLockingMode.MINIMAL).withGroup(Field.createGroupEntry(Group.CONNECTOR_SNAPSHOT, 1)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Controls how long the connector holds onto the global read lock while it is performing a snapshot. The default is 'minimal', which means the connector holds the global read lock (and thus prevents any updates) for just the initial portion of the snapshot while the database schemas and other metadata are being read. The remaining work in a snapshot involves selecting all rows from each table, and this can be done using the snapshot process' REPEATABLE READ transaction even when the lock is no longer held and other operations are updating the database. However, in some cases it may be desirable to block all writes for the entire duration of the snapshot; in such cases set this property to 'extended'. Using a value of 'none' will prevent the connector from acquiring any table locks during the snapshot process. This mode can only be used in combination with snapshot.mode values of 'schema_only' or 'schema_only_recovery' and is only safe to use if no schema changes are happening while the snapshot is taken.").withValidation(new Field.Validator[]{MySqlConnectorConfig::validateSnapshotLockingMode});
        SNAPSHOT_NEW_TABLES = Field.create("snapshot.new.tables").withDisplayName("Snapshot newly added tables").withEnum(SnapshotNewTables.class, SnapshotNewTables.OFF).withGroup(Field.createGroupEntry(Group.CONNECTOR_SNAPSHOT, 4)).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("BETA FEATURE: On connector restart, the connector will check if there have been any new tables added to the configuration, and snapshot them. There is presently only two options: 'off': Default behavior. Do not snapshot new tables. 'parallel': The snapshot of the new tables will occur in parallel to the continued binlog reading of the old tables. When the snapshot completes, an independent binlog reader will begin reading the events for the new tables until it catches up to present time. At this point, both old and new binlog readers will be momentarily halted and new binlog reader will start that will read the binlog for all configured tables. The parallel binlog reader will have a configured server id of 10000 + the primary binlog reader's server id.");
        TIME_PRECISION_MODE = RelationalDatabaseConnectorConfig.TIME_PRECISION_MODE.withEnum(TemporalPrecisionMode.class, TemporalPrecisionMode.ADAPTIVE_TIME_MICROSECONDS).withGroup(Field.createGroupEntry(Group.CONNECTOR, 26)).withValidation(new Field.Validator[]{MySqlConnectorConfig::validateTimePrecisionMode}).withDescription("Time, date and timestamps can be represented with different kinds of precisions, including: 'adaptive_time_microseconds': the precision of date and timestamp values is based the database column's precision; but time fields always use microseconds precision; 'connect': always represents time, date and timestamp values using Kafka Connect's built-in representations for Time, Date, and Timestamp, which uses millisecond precision regardless of the database columns' precision.");
        BIGINT_UNSIGNED_HANDLING_MODE = Field.create("bigint.unsigned.handling.mode").withDisplayName("BIGINT UNSIGNED Handling").withEnum(BigIntUnsignedHandlingMode.class, BigIntUnsignedHandlingMode.LONG).withGroup(Field.createGroupEntry(Group.CONNECTOR, 27)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Specify how BIGINT UNSIGNED columns should be represented in change events, including: 'precise' uses java.math.BigDecimal to represent values, which are encoded in the change events using a binary representation and Kafka Connect's 'org.apache.kafka.connect.data.Decimal' type; 'long' (the default) represents values using Java's 'long', which may not offer the precision but will be far easier to use in consumers.");
        EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE = Field.create("event.deserialization.failure.handling.mode").withDisplayName("Event deserialization failure handling").withEnum(EventProcessingFailureHandlingMode.class, EventProcessingFailureHandlingMode.FAIL).withGroup(Field.createGroupEntry(Group.CONNECTOR, 21)).withValidation(new Field.Validator[]{MySqlConnectorConfig::validateEventDeserializationFailureHandlingModeNotSet}).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Specify how failures during deserialization of binlog events (i.e. when encountering a corrupted event) should be handled, including: 'fail' (the default) an exception indicating the problematic event and its binlog position is raised, causing the connector to be stopped; 'warn' the problematic event and its binlog position will be logged and the event will be skipped; 'ignore' the problematic event will be skipped.");
        INCONSISTENT_SCHEMA_HANDLING_MODE = Field.create("inconsistent.schema.handling.mode").withDisplayName("Inconsistent schema failure handling").withEnum(EventProcessingFailureHandlingMode.class, EventProcessingFailureHandlingMode.FAIL).withGroup(Field.createGroupEntry(Group.ADVANCED, 2)).withWidth(Width.SHORT).withImportance(Importance.MEDIUM).withDescription("Specify how binlog events that belong to a table missing from internal schema representation (i.e. internal representation is not consistent with database) should be handled, including: 'fail' (the default) an exception indicating the problematic event and its binlog position is raised, causing the connector to be stopped; 'warn' the problematic event and its binlog position will be logged and the event will be skipped; 'skip' the problematic event will be skipped.");
        ENABLE_TIME_ADJUSTER = Field.create("enable.time.adjuster").withDisplayName("Enable Time Adjuster").withType(Type.BOOLEAN).withGroup(Field.createGroupEntry(Group.CONNECTOR, 22)).withDefault(true).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("MySQL allows user to insert year value as either 2-digit or 4-digit. In case of two digit the value is automatically mapped into 1970 - 2069.false - delegates the implicit conversion to the databasetrue - (the default) Debezium makes the conversion");
        READ_ONLY_CONNECTION = Field.create("read.only").withDisplayName("Read only connection").withType(Type.BOOLEAN).withDefault(false).withWidth(Width.SHORT).withImportance(Importance.LOW).withDescription("Switched connector to use alternative methods to deliver signals to Debezium instead of writing to signaling table");
        SOURCE_INFO_STRUCT_MAKER = CommonConnectorConfig.SOURCE_INFO_STRUCT_MAKER.withDefault(MySqlSourceInfoStructMaker.class.getName());
        STORE_ONLY_CAPTURED_DATABASES_DDL = HistorizedRelationalDatabaseConnectorConfig.STORE_ONLY_CAPTURED_DATABASES_DDL.withDefault(true);
        CONFIG_DEFINITION = HistorizedRelationalDatabaseConnectorConfig.CONFIG_DEFINITION.edit().name("MySQL").excluding(new Field[]{SCHEMA_INCLUDE_LIST, SCHEMA_EXCLUDE_LIST, RelationalDatabaseConnectorConfig.TIME_PRECISION_MODE, RelationalDatabaseConnectorConfig.TABLE_IGNORE_BUILTIN, HistorizedRelationalDatabaseConnectorConfig.STORE_ONLY_CAPTURED_DATABASES_DDL}).type(new Field[]{HOSTNAME, PORT, USER, PASSWORD, ON_CONNECT_STATEMENTS, SERVER_ID, SERVER_ID_OFFSET, SSL_MODE, SSL_KEYSTORE, SSL_KEYSTORE_PASSWORD, SSL_TRUSTSTORE, SSL_TRUSTSTORE_PASSWORD, JDBC_DRIVER}).connector(new Field[]{CONNECTION_TIMEOUT_MS, KEEP_ALIVE, KEEP_ALIVE_INTERVAL_MS, SNAPSHOT_MODE, SNAPSHOT_LOCKING_MODE, SNAPSHOT_NEW_TABLES, BIGINT_UNSIGNED_HANDLING_MODE, TIME_PRECISION_MODE, ENABLE_TIME_ADJUSTER, BINARY_HANDLING_MODE, SCHEMA_NAME_ADJUSTMENT_MODE, ROW_COUNT_FOR_STREAMING_RESULT_SETS, INCREMENTAL_SNAPSHOT_CHUNK_SIZE, INCREMENTAL_SNAPSHOT_ALLOW_SCHEMA_CHANGES, STORE_ONLY_CAPTURED_DATABASES_DDL}).events(new Field[]{INCLUDE_SQL_QUERY, TABLE_IGNORE_BUILTIN, DATABASE_INCLUDE_LIST, DATABASE_EXCLUDE_LIST, GTID_SOURCE_INCLUDES, GTID_SOURCE_EXCLUDES, GTID_SOURCE_FILTER_DML_EVENTS, BUFFER_SIZE_FOR_BINLOG_READER, EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE, INCONSISTENT_SCHEMA_HANDLING_MODE, SOURCE_INFO_STRUCT_MAKER}).create();
        ALL_FIELDS = Field.setOf(CONFIG_DEFINITION.all());
        EXPOSED_FIELDS = ALL_FIELDS;
        BUILT_IN_DB_NAMES = Collect.unmodifiableSet(new String[]{"mysql", "performance_schema", "sys", "information_schema"});
    }

    public static enum SnapshotMode implements EnumeratedValue {
        WHEN_NEEDED("when_needed", true, true, true, false, true),
        INITIAL("initial", true, true, true, false, false),
        SCHEMA_ONLY("schema_only", true, false, true, false, false),
        SCHEMA_ONLY_RECOVERY("schema_only_recovery", true, false, true, true, false),
        NEVER("never", false, false, true, false, false),
        INITIAL_ONLY("initial_only", true, true, false, false, false);

        private final String value;
        private final boolean includeSchema;
        private final boolean includeData;
        private final boolean shouldStream;
        private final boolean shouldSnapshotOnSchemaError;
        private final boolean shouldSnapshotOnDataError;

        private SnapshotMode(String value, boolean includeSchema, boolean includeData, boolean shouldStream, boolean shouldSnapshotOnSchemaError, boolean shouldSnapshotOnDataError) {
            this.value = value;
            this.includeSchema = includeSchema;
            this.includeData = includeData;
            this.shouldStream = shouldStream;
            this.shouldSnapshotOnSchemaError = shouldSnapshotOnSchemaError;
            this.shouldSnapshotOnDataError = shouldSnapshotOnDataError;
        }

        public String getValue() {
            return this.value;
        }

        public boolean includeSchema() {
            return this.includeSchema;
        }

        public boolean includeData() {
            return this.includeData;
        }

        public boolean shouldStream() {
            return this.shouldStream;
        }

        public boolean shouldSnapshotOnSchemaError() {
            return this.shouldSnapshotOnSchemaError;
        }

        public boolean shouldSnapshotOnDataError() {
            return this.shouldSnapshotOnDataError;
        }

        public boolean shouldSnapshot() {
            return this.includeSchema || this.includeData;
        }

        public static SnapshotMode parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                SnapshotMode[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    SnapshotMode option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static SnapshotMode parse(String value, String defaultValue) {
            SnapshotMode mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static SnapshotMode[] $values() {
            return new SnapshotMode[]{WHEN_NEEDED, INITIAL, SCHEMA_ONLY, SCHEMA_ONLY_RECOVERY, NEVER, INITIAL_ONLY};
        }
    }

    public static enum SnapshotLockingMode implements EnumeratedValue {
        EXTENDED("extended"),
        MINIMAL("minimal"),
        MINIMAL_PERCONA("minimal_percona"),
        NONE("none");

        private final String value;

        private SnapshotLockingMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public boolean usesMinimalLocking() {
            return this.value.equals(MINIMAL.value) || this.value.equals(MINIMAL_PERCONA.value);
        }

        public boolean usesLocking() {
            return !this.value.equals(NONE.value);
        }

        public boolean flushResetsIsolationLevel() {
            return !this.value.equals(MINIMAL_PERCONA.value);
        }

        public String getLockStatement() {
            return this.value.equals(MINIMAL_PERCONA.value) ? "LOCK TABLES FOR BACKUP" : "FLUSH TABLES WITH READ LOCK";
        }

        public static SnapshotLockingMode parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                SnapshotLockingMode[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    SnapshotLockingMode option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static SnapshotLockingMode parse(String value, String defaultValue) {
            SnapshotLockingMode mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static SnapshotLockingMode[] $values() {
            return new SnapshotLockingMode[]{EXTENDED, MINIMAL, MINIMAL_PERCONA, NONE};
        }
    }

    public static enum SnapshotNewTables implements EnumeratedValue {
        OFF("off"),
        PARALLEL("parallel");

        private final String value;

        private SnapshotNewTables(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static SnapshotNewTables parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                SnapshotNewTables[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    SnapshotNewTables option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static SnapshotNewTables parse(String value, String defaultValue) {
            SnapshotNewTables snapshotNewTables = parse(value);
            if (snapshotNewTables == null && defaultValue != null) {
                snapshotNewTables = parse(defaultValue);
            }

            return snapshotNewTables;
        }

        // $FF: synthetic method
        private static SnapshotNewTables[] $values() {
            return new SnapshotNewTables[]{OFF, PARALLEL};
        }
    }

    public static enum SecureConnectionMode implements EnumeratedValue {
        DISABLED("disabled"),
        PREFERRED("preferred"),
        REQUIRED("required"),
        VERIFY_CA("verify_ca"),
        VERIFY_IDENTITY("verify_identity");

        private final String value;

        private SecureConnectionMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static SecureConnectionMode parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                SecureConnectionMode[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    SecureConnectionMode option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static SecureConnectionMode parse(String value, String defaultValue) {
            SecureConnectionMode mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static SecureConnectionMode[] $values() {
            return new SecureConnectionMode[]{DISABLED, PREFERRED, REQUIRED, VERIFY_CA, VERIFY_IDENTITY};
        }
    }

    public static enum BigIntUnsignedHandlingMode implements EnumeratedValue {
        PRECISE("precise"),
        LONG("long");

        private final String value;

        private BigIntUnsignedHandlingMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public BigIntUnsignedMode asBigIntUnsignedMode() {
            switch (this) {
                case LONG:
                    return BigIntUnsignedMode.LONG;
                case PRECISE:
                default:
                    return BigIntUnsignedMode.PRECISE;
            }
        }

        public static BigIntUnsignedHandlingMode parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                BigIntUnsignedHandlingMode[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    BigIntUnsignedHandlingMode option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static BigIntUnsignedHandlingMode parse(String value, String defaultValue) {
            BigIntUnsignedHandlingMode mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static BigIntUnsignedHandlingMode[] $values() {
            return new BigIntUnsignedHandlingMode[]{PRECISE, LONG};
        }
    }
}
