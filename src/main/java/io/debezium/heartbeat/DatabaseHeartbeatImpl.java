package io.debezium.heartbeat;

import io.debezium.config.Field;
import io.debezium.function.BlockingConsumer;
import io.debezium.jdbc.JdbcConnection;
import io.debezium.schema.SchemaNameAdjuster;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;

public class DatabaseHeartbeatImpl extends HeartbeatImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHeartbeatImpl.class);
    public static final String HEARTBEAT_ACTION_QUERY_PROPERTY_NAME = "heartbeat.action.query";
    public static final Field HEARTBEAT_ACTION_QUERY;
    private final String heartBeatActionQuery;
    private final JdbcConnection jdbcConnection;
    private final HeartbeatErrorHandler errorHandler;

    public DatabaseHeartbeatImpl(Duration heartbeatInterval, String topicName, String key, JdbcConnection jdbcConnection, String heartBeatActionQuery, HeartbeatErrorHandler errorHandler, SchemaNameAdjuster schemaNameAdjuster) {
        super(heartbeatInterval, topicName, key, schemaNameAdjuster);
        this.heartBeatActionQuery = heartBeatActionQuery;
        this.jdbcConnection = jdbcConnection;
        this.errorHandler = errorHandler;
    }

    public void forcedBeat(Map<String, ?> partition, Map<String, ?> offset, BlockingConsumer<SourceRecord> consumer) throws InterruptedException {
        try {
            this.jdbcConnection.execute(this.heartBeatActionQuery);
        } catch (SQLException var5) {
            if (this.errorHandler != null) {
                this.errorHandler.onError(var5);
            }

            LOGGER.error("Could not execute heartbeat action (Error: " + var5.getSQLState() + ")", var5);
        }

        LOGGER.debug("Executed heartbeat action query");
        super.forcedBeat(partition, offset, consumer);
    }

    public void close() {
        try {
            this.jdbcConnection.close();
        } catch (SQLException var2) {
            LOGGER.error("Exception while closing the heartbeat JDBC connection", var2);
        }

    }

    static {
        HEARTBEAT_ACTION_QUERY = Field.create("heartbeat.action.query").withDisplayName("An optional query to execute with every heartbeat").withType(Type.STRING).withGroup(Field.createGroupEntry(Field.Group.ADVANCED_HEARTBEAT, 2)).withWidth(Width.MEDIUM).withImportance(Importance.LOW).withDescription("The query executed with every heartbeat.");
    }
}
