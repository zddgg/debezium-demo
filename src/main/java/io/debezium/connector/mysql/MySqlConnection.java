package io.debezium.connector.mysql;

import com.mysql.cj.CharsetMapping;
import io.debezium.DebeziumException;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.CommonConnectorConfig.EventProcessingFailureHandlingMode;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.jdbc.JdbcConfiguration;
import io.debezium.jdbc.JdbcConnection;
import io.debezium.relational.Column;
import io.debezium.relational.Table;
import io.debezium.relational.TableId;
import io.debezium.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

public class MySqlConnection extends JdbcConnection {
    private static Logger LOGGER = LoggerFactory.getLogger(MySqlConnection.class);
    private static final String SQL_SHOW_SYSTEM_VARIABLES = "SHOW VARIABLES";
    private static final String SQL_SHOW_SYSTEM_VARIABLES_CHARACTER_SET = "SHOW VARIABLES WHERE Variable_name IN ('character_set_server','collation_server')";
    private static final String SQL_SHOW_SESSION_VARIABLE_SSL_VERSION = "SHOW SESSION STATUS LIKE 'Ssl_version'";
    private static final String QUOTED_CHARACTER = "`";
    protected static final String URL_PATTERN = "jdbc:mysql://${hostname}:${port}/?useInformationSchema=true&nullCatalogMeansCurrent=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&connectTimeout=${connectTimeout}";
    private final Map<String, String> originalSystemProperties;
    private final MySqlConnectionConfiguration connectionConfig;
    private final MySqlFieldReader mysqlFieldReader;

    public MySqlConnection(MySqlConnectionConfiguration connectionConfig, MySqlFieldReader fieldReader) {
        super(connectionConfig.jdbcConfig, connectionConfig.factory(), "`", "`");
        this.originalSystemProperties = new HashMap();
        this.connectionConfig = connectionConfig;
        this.mysqlFieldReader = fieldReader;
    }

    public MySqlConnection(MySqlConnectionConfiguration connectionConfig) {
        this(connectionConfig, new MySqlTextProtocolFieldReader((MySqlConnectorConfig) null));
    }

    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            this.originalSystemProperties.forEach((name, value) -> {
                if (value != null) {
                    System.setProperty(name, value);
                } else {
                    System.clearProperty(name);
                }

            });
        }

    }

    protected Map<String, String> readMySqlCharsetSystemVariables() {
        LOGGER.debug("Reading MySQL charset-related system variables before parsing DDL history.");
        return this.querySystemVariables("SHOW VARIABLES WHERE Variable_name IN ('character_set_server','collation_server')");
    }

    protected Map<String, String> readMySqlSystemVariables() {
        LOGGER.debug("Reading MySQL system variables");
        return this.querySystemVariables("SHOW VARIABLES");
    }

    private Map<String, String> querySystemVariables(String statement) {
        Map<String, String> variables = new HashMap();

        try {
            this.query(statement, (rs) -> {
                while (rs.next()) {
                    String varName = rs.getString(1);
                    String value = rs.getString(2);
                    if (varName != null && value != null) {
                        variables.put(varName, value);
                        LOGGER.debug("\t{} = {}", Strings.pad(varName, 45, ' '), Strings.pad(value, 45, ' '));
                    }
                }

            });
            return variables;
        } catch (SQLException var4) {
            throw new DebeziumException("Error reading MySQL variables: " + var4.getMessage(), var4);
        }
    }

    protected String setStatementFor(Map<String, String> variables) {
        StringBuilder sb = new StringBuilder("SET ");
        boolean first = true;
        List<String> varNames = new ArrayList(variables.keySet());
        Collections.sort(varNames);

        String value;
        for (Iterator var5 = varNames.iterator(); var5.hasNext(); sb.append(value)) {
            String varName = (String) var5.next();
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(varName).append("=");
            value = (String) variables.get(varName);
            if (value == null) {
                value = "";
            }

            if (value.contains(",") || value.contains(";")) {
                value = "'" + value + "'";
            }
        }

        return sb.append(";").toString();
    }

    protected void setSystemProperty(String property, Field field, boolean showValueInError) {
        String value = this.connectionConfig.originalConfig().getString(field);
        if (value != null) {
            value = value.trim();
            String existingValue = System.getProperty(property);
            String msg;
            if (existingValue == null) {
                msg = System.setProperty(property, value);
                this.originalSystemProperties.put(property, msg);
            } else {
                existingValue = existingValue.trim();
                if (!existingValue.equalsIgnoreCase(value)) {
                    msg = "System or JVM property '" + property + "' is already defined, but the configuration property '" + field.name() + "' defines a different value";
                    if (showValueInError) {
                        msg = "System or JVM property '" + property + "' is already defined as " + existingValue + ", but the configuration property '" + field.name() + "' defines a different value '" + value + "'";
                    }

                    throw new DebeziumException(msg);
                }
            }
        }

    }

    protected String getSessionVariableForSslVersion() {
        String SSL_VERSION = "Ssl_version";
        LOGGER.debug("Reading MySQL Session variable for Ssl Version");
        Map<String, String> sessionVariables = this.querySystemVariables("SHOW SESSION STATUS LIKE 'Ssl_version'");
        return !sessionVariables.isEmpty() && sessionVariables.containsKey("Ssl_version") ? (String) sessionVariables.get("Ssl_version") : null;
    }

    public boolean isGtidModeEnabled() {
        try {
            return (Boolean) this.queryAndMap("SHOW GLOBAL VARIABLES LIKE 'GTID_MODE'", (rs) -> {
                return rs.next() ? "ON".equalsIgnoreCase(rs.getString(2)) : false;
            });
        } catch (SQLException var2) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at GTID mode: ", var2);
        }
    }

    public String knownGtidSet() {
        try {
            return (String) this.queryAndMap("SHOW MASTER STATUS", (rs) -> {
                return rs.next() && rs.getMetaData().getColumnCount() > 4 ? rs.getString(5) : "";
            });
        } catch (SQLException var2) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at GTID mode: ", var2);
        }
    }

    public GtidSet subtractGtidSet(GtidSet set1, GtidSet set2) {
        try {
            return (GtidSet) this.prepareQueryAndMap("SELECT GTID_SUBTRACT(?, ?)", (ps) -> {
                ps.setString(1, set1.toString());
                ps.setString(2, set2.toString());
            }, (rs) -> {
                return rs.next() ? new GtidSet(rs.getString(1)) : new GtidSet("");
            });
        } catch (SQLException var4) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at GTID mode: ", var4);
        }
    }

    public GtidSet purgedGtidSet() {
        try {
            return (GtidSet) this.queryAndMap("SELECT @@global.gtid_purged", (rs) -> {
                return rs.next() && rs.getMetaData().getColumnCount() > 0 ? new GtidSet(rs.getString(1)) : new GtidSet("");
            });
        } catch (SQLException var2) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at gtid_purged variable: ", var2);
        }
    }

    public boolean userHasPrivileges(String grantName) {
        try {
            return (Boolean) this.queryAndMap("SHOW GRANTS FOR CURRENT_USER", (rs) -> {
                while (true) {
                    if (rs.next()) {
                        String grants = rs.getString(1);
                        LOGGER.debug(grants);
                        if (grants == null) {
                            return false;
                        }

                        grants = grants.toUpperCase();
                        if (!grants.contains("ALL") && !grants.contains(grantName.toUpperCase())) {
                            continue;
                        }

                        return true;
                    }

                    return false;
                }
            });
        } catch (SQLException var3) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at privileges for current user: ", var3);
        }
    }

    public String earliestBinlogFilename() {
        List<String> logNames = new ArrayList();

        try {
            LOGGER.info("Checking all known binlogs from MySQL");
            this.query("SHOW BINARY LOGS", (rs) -> {
                while (rs.next()) {
                    logNames.add(rs.getString(1));
                }

            });
        } catch (SQLException var3) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking for binary logs: ", var3);
        }

        return logNames.isEmpty() ? null : (String) logNames.get(0);
    }

    protected boolean isBinlogRowImageFull() {
        try {
            String rowImage = (String) this.queryAndMap("SHOW GLOBAL VARIABLES LIKE 'binlog_row_image'", (rs) -> {
                return rs.next() ? rs.getString(2) : "FULL";
            });
            LOGGER.debug("binlog_row_image={}", rowImage);
            return "FULL".equalsIgnoreCase(rowImage);
        } catch (SQLException var2) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at BINLOG_ROW_IMAGE mode: ", var2);
        }
    }

    protected boolean isBinlogFormatRow() {
        try {
            String mode = (String) this.queryAndMap("SHOW GLOBAL VARIABLES LIKE 'binlog_format'", (rs) -> {
                return rs.next() ? rs.getString(2) : "";
            });
            LOGGER.debug("binlog_format={}", mode);
            return "ROW".equalsIgnoreCase(mode);
        } catch (SQLException var2) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking at BINLOG_FORMAT mode: ", var2);
        }
    }

    public List<String> availableBinlogFiles() {
        List<String> logNames = new ArrayList();

        try {
            LOGGER.info("Get all known binlogs from MySQL");
            this.query("SHOW BINARY LOGS", (rs) -> {
                while (rs.next()) {
                    logNames.add(rs.getString(1));
                }

            });
            return logNames;
        } catch (SQLException var3) {
            throw new DebeziumException("Unexpected error while connecting to MySQL and looking for binary logs: ", var3);
        }
    }

    public OptionalLong getEstimatedTableSize(TableId tableId) {
        try {
            this.execute(new String[]{"USE `" + tableId.catalog() + "`;"});
            return (OptionalLong) this.queryAndMap("SHOW TABLE STATUS LIKE '" + tableId.table() + "';", (rs) -> {
                return rs.next() ? OptionalLong.of(rs.getLong(5)) : OptionalLong.empty();
            });
        } catch (SQLException var3) {
            LOGGER.debug("Error while getting number of rows in table {}: {}", new Object[]{tableId, var3.getMessage(), var3});
            return OptionalLong.empty();
        }
    }

    public boolean isTableIdCaseSensitive() {
        return !"0".equals(this.readMySqlSystemVariables().get("lower_case_table_names"));
    }

    protected Map<String, DatabaseLocales> readDatabaseCollations() {
        LOGGER.debug("Reading default database charsets");

        try {
            return (Map) this.queryAndMap("SELECT schema_name, default_character_set_name, default_collation_name FROM information_schema.schemata", (rs) -> {
                Map<String, DatabaseLocales> charsets = new HashMap();

                while (true) {
                    String dbName;
                    String charset;
                    String collation;
                    do {
                        do {
                            if (!rs.next()) {
                                return charsets;
                            }

                            dbName = rs.getString(1);
                            charset = rs.getString(2);
                            collation = rs.getString(3);
                        } while (dbName == null);
                    } while (charset == null && collation == null);

                    charsets.put(dbName, new DatabaseLocales(charset, collation));
                    LOGGER.debug("\t{} = {}, {}", new Object[]{Strings.pad(dbName, 45, ' '), Strings.pad(charset, 45, ' '), Strings.pad(collation, 45, ' ')});
                }
            });
        } catch (SQLException var2) {
            throw new DebeziumException("Error reading default database charsets: " + var2.getMessage(), var2);
        }
    }

    public MySqlConnectionConfiguration connectionConfig() {
        return this.connectionConfig;
    }

    public String connectionString() {
        return this.connectionString("jdbc:mysql://${hostname}:${port}/?useInformationSchema=true&nullCatalogMeansCurrent=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&connectTimeout=${connectTimeout}");
    }

    public static String getJavaEncodingForMysqlCharSet(String mysqlCharsetName) {
        return CharsetMappingWrapper.getJavaEncodingForMysqlCharSet(mysqlCharsetName);
    }

    public Object getColumnValue(ResultSet rs, int columnIndex, Column column, Table table) throws SQLException {
        return this.mysqlFieldReader.readField(rs, columnIndex, column, table);
    }

    public String quotedTableIdString(TableId tableId) {
        return tableId.toQuotedString('`');
    }

    public static class MySqlConnectionConfiguration {
        protected static final String JDBC_PROPERTY_CONNECTION_TIME_ZONE = "connectionTimeZone";
        private final JdbcConfiguration jdbcConfig;
        private final ConnectionFactory factory;
        private final Configuration config;

        public MySqlConnectionConfiguration(Configuration config) {
            this.config = config;
            boolean useSSL = this.sslModeEnabled();
            Configuration dbConfig = ((Configuration.Builder) config.edit().withDefault(MySqlConnectorConfig.PORT, MySqlConnectorConfig.PORT.defaultValue())).build().subset("database.", true).merge(new Configuration[]{config.subset("driver.", true)});
            Configuration.Builder jdbcConfigBuilder = dbConfig.edit().with("connectTimeout", Long.toString(this.getConnectionTimeout().toMillis())).with("sslMode", this.sslMode().getValue());
            if (useSSL) {
                if (!Strings.isNullOrBlank(this.sslTrustStore())) {
                    jdbcConfigBuilder.with("trustCertificateKeyStoreUrl", "file:" + this.sslTrustStore());
                }

                if (this.sslTrustStorePassword() != null) {
                    jdbcConfigBuilder.with("trustCertificateKeyStorePassword", String.valueOf(this.sslTrustStorePassword()));
                }

                if (!Strings.isNullOrBlank(this.sslKeyStore())) {
                    jdbcConfigBuilder.with("clientCertificateKeyStoreUrl", "file:" + this.sslKeyStore());
                }

                if (this.sslKeyStorePassword() != null) {
                    jdbcConfigBuilder.with("clientCertificateKeyStorePassword", String.valueOf(this.sslKeyStorePassword()));
                }
            }

            jdbcConfigBuilder.with("connectionTimeZone", determineConnectionTimeZone(dbConfig));
            ((Configuration.Builder) jdbcConfigBuilder.with("allowLoadLocalInfile", "false").with("allowUrlInLocalInfile", "false").with("autoDeserialize", false)).without("queryInterceptors");
            this.jdbcConfig = JdbcConfiguration.adapt(jdbcConfigBuilder.build());
            String driverClassName = this.jdbcConfig.getString(MySqlConnectorConfig.JDBC_DRIVER);
            this.factory = JdbcConnection.patternBasedFactory("jdbc:mysql://${hostname}:${port}/?useInformationSchema=true&nullCatalogMeansCurrent=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&connectTimeout=${connectTimeout}", driverClassName, this.getClass().getClassLoader(), new Field[0]);
        }

        private static String determineConnectionTimeZone(Configuration dbConfig) {
            String connectionTimeZone = dbConfig.getString("connectionTimeZone");
            return connectionTimeZone != null ? connectionTimeZone : "SERVER";
        }

        public JdbcConfiguration config() {
            return this.jdbcConfig;
        }

        public Configuration originalConfig() {
            return this.config;
        }

        public ConnectionFactory factory() {
            return this.factory;
        }

        public String username() {
            return this.config.getString(MySqlConnectorConfig.USER);
        }

        public String password() {
            return this.config.getString(MySqlConnectorConfig.PASSWORD);
        }

        public String hostname() {
            return this.config.getString(MySqlConnectorConfig.HOSTNAME);
        }

        public int port() {
            return this.config.getInteger(MySqlConnectorConfig.PORT);
        }

        public MySqlConnectorConfig.SecureConnectionMode sslMode() {
            String mode = this.config.getString(MySqlConnectorConfig.SSL_MODE);
            return MySqlConnectorConfig.SecureConnectionMode.parse(mode);
        }

        public boolean sslModeEnabled() {
            return this.sslMode() != MySqlConnectorConfig.SecureConnectionMode.DISABLED;
        }

        public String sslKeyStore() {
            return this.config.getString(MySqlConnectorConfig.SSL_KEYSTORE);
        }

        public char[] sslKeyStorePassword() {
            String password = this.config.getString(MySqlConnectorConfig.SSL_KEYSTORE_PASSWORD);
            return Strings.isNullOrBlank(password) ? null : password.toCharArray();
        }

        public String sslTrustStore() {
            return this.config.getString(MySqlConnectorConfig.SSL_TRUSTSTORE);
        }

        public char[] sslTrustStorePassword() {
            String password = this.config.getString(MySqlConnectorConfig.SSL_TRUSTSTORE_PASSWORD);
            return Strings.isNullOrBlank(password) ? null : password.toCharArray();
        }

        public Duration getConnectionTimeout() {
            return Duration.ofMillis(this.config.getLong(MySqlConnectorConfig.CONNECTION_TIMEOUT_MS));
        }

        public EventProcessingFailureHandlingMode eventProcessingFailureHandlingMode() {
            String mode = this.config.getString(CommonConnectorConfig.EVENT_PROCESSING_FAILURE_HANDLING_MODE);
            if (mode == null) {
                mode = this.config.getString(MySqlConnectorConfig.EVENT_DESERIALIZATION_FAILURE_HANDLING_MODE);
            }

            return EventProcessingFailureHandlingMode.parse(mode);
        }

        public EventProcessingFailureHandlingMode inconsistentSchemaHandlingMode() {
            String mode = this.config.getString(MySqlConnectorConfig.INCONSISTENT_SCHEMA_HANDLING_MODE);
            return EventProcessingFailureHandlingMode.parse(mode);
        }
    }

    private static final class CharsetMappingWrapper extends CharsetMapping {
        static String getJavaEncodingForMysqlCharSet(String mySqlCharsetName) {
            return CharsetMapping.getStaticJavaEncodingForMysqlCharset(mySqlCharsetName);
        }
    }

    public static class DatabaseLocales {
        private final String charset;
        private final String collation;

        public DatabaseLocales(String charset, String collation) {
            this.charset = charset;
            this.collation = collation;
        }

        public void appendToDdlStatement(String dbName, StringBuilder ddl) {
            if (this.charset != null) {
                MySqlConnection.LOGGER.debug("Setting default charset '{}' for database '{}'", this.charset, dbName);
                ddl.append(" CHARSET ").append(this.charset);
            } else {
                MySqlConnection.LOGGER.info("Default database charset for '{}' not found", dbName);
            }

            if (this.collation != null) {
                MySqlConnection.LOGGER.debug("Setting default collation '{}' for database '{}'", this.collation, dbName);
                ddl.append(" COLLATE ").append(this.collation);
            } else {
                MySqlConnection.LOGGER.info("Default database collation for '{}' not found", dbName);
            }

        }
    }
}
