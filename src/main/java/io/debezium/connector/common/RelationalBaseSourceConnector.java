package io.debezium.connector.common;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.Configuration;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.util.Strings;
import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public abstract class RelationalBaseSourceConnector extends SourceConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationalBaseSourceConnector.class);

    public Config validate(Map<String, String> connectorConfigs) {
        Configuration config = Configuration.from(connectorConfigs);
        Map<String, ConfigValue> results = this.validateAllFields(config);
        ConfigValue logicalName = (ConfigValue) results.get(CommonConnectorConfig.TOPIC_PREFIX.name());
        ConfigValue hostnameValue = (ConfigValue) results.get(RelationalDatabaseConnectorConfig.HOSTNAME.name());
        ConfigValue portValue = (ConfigValue) results.get(RelationalDatabaseConnectorConfig.PORT.name());
        ConfigValue userValue = (ConfigValue) results.get(RelationalDatabaseConnectorConfig.USER.name());
        ConfigValue passwordValue = (ConfigValue) results.get(RelationalDatabaseConnectorConfig.PASSWORD.name());
        String passwordStringValue = config.getString(RelationalDatabaseConnectorConfig.PASSWORD);
        if (Strings.isNullOrEmpty(passwordStringValue)) {
            LOGGER.debug("The connection password is empty");
        }

        if (logicalName.errorMessages().isEmpty() && hostnameValue.errorMessages().isEmpty() && portValue.errorMessages().isEmpty() && userValue.errorMessages().isEmpty() && passwordValue.errorMessages().isEmpty()) {
            this.validateConnection(results, config);
        }

        return new Config(new ArrayList(results.values()));
    }

    protected abstract void validateConnection(Map<String, ConfigValue> var1, Configuration var2);

    protected abstract Map<String, ConfigValue> validateAllFields(Configuration var1);
}
