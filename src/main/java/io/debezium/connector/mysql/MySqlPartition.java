package io.debezium.connector.mysql;

import io.debezium.config.Configuration;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.AbstractPartition;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.util.Collect;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MySqlPartition extends AbstractPartition implements Partition {
    private static final String SERVER_PARTITION_KEY = "server";
    private final String serverName;

    public MySqlPartition(String serverName, String databaseName) {
        super(databaseName);
        this.serverName = serverName;
    }

    public Map<String, String> getSourcePartition() {
        return Collect.hashMapOf("server", this.serverName);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null && this.getClass() == obj.getClass()) {
            MySqlPartition other = (MySqlPartition) obj;
            return Objects.equals(this.serverName, other.serverName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.serverName.hashCode();
    }

    public String toString() {
        return "MySqlPartition [sourcePartition=" + this.getSourcePartition() + "]";
    }

    public static class Provider implements Partition.Provider<MySqlPartition> {
        private final MySqlConnectorConfig connectorConfig;
        private final Configuration taskConfig;

        public Provider(MySqlConnectorConfig connectorConfig, Configuration taskConfig) {
            this.connectorConfig = connectorConfig;
            this.taskConfig = taskConfig;
        }

        public Set<MySqlPartition> getPartitions() {
            return Collections.singleton(new MySqlPartition(this.connectorConfig.getLogicalName(), this.taskConfig.getString(RelationalDatabaseConnectorConfig.DATABASE_NAME.name())));
        }
    }
}
