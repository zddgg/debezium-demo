package io.debezium.connector.mysql;

import io.debezium.annotation.Immutable;
import io.debezium.config.Configuration;
import io.debezium.connector.common.RelationalBaseSourceConnector;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlConnector extends RelationalBaseSourceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlConnector.class);
    @Immutable
    private Map<String, String> properties;

    public String version() {
        return Module.version();
    }

    public void start(Map<String, String> props) {
        this.properties = Collections.unmodifiableMap(new HashMap(props));
    }

    public Class<? extends Task> taskClass() {
        return MySqlConnectorTask.class;
    }

    public List<Map<String, String>> taskConfigs(int maxTasks) {
        if (maxTasks > 1) {
            throw new IllegalArgumentException("Only a single connector task may be started");
        } else {
            return Collections.singletonList(this.properties);
        }
    }

    public void stop() {
    }

    public ConfigDef config() {
        return MySqlConnectorConfig.configDef();
    }

    protected void validateConnection(Map<String, ConfigValue> configValues, Configuration config) {
        ConfigValue hostnameValue = (ConfigValue) configValues.get(RelationalDatabaseConnectorConfig.HOSTNAME.name());
        MySqlConnection.MySqlConnectionConfiguration connectionConfig = new MySqlConnection.MySqlConnectionConfiguration(config);

        try {
            MySqlConnection connection = new MySqlConnection(connectionConfig);

            try {
                try {
                    connection.connect();
                    connection.execute(new String[]{"SELECT version()"});
                    LOGGER.info("Successfully tested connection for {} with user '{}'", connection.connectionString(), connectionConfig.username());
                } catch (SQLException var9) {
                    LOGGER.error("Failed testing connection for {} with user '{}'", new Object[]{connection.connectionString(), connectionConfig.username(), var9});
                    hostnameValue.addErrorMessage("Unable to connect: " + var9.getMessage());
                }
            } catch (Throwable var10) {
                try {
                    connection.close();
                } catch (Throwable var8) {
                    var10.addSuppressed(var8);
                }

                throw var10;
            }

            connection.close();
        } catch (SQLException var11) {
            LOGGER.error("Unexpected error shutting down the database connection", var11);
        }

    }

    protected Map<String, ConfigValue> validateAllFields(Configuration config) {
        return config.validate(MySqlConnectorConfig.ALL_FIELDS);
    }
}
