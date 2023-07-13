package io.debezium.connector;

import io.debezium.config.CommonConnectorConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;

public abstract class AbstractSourceInfo {
    public static final String DEBEZIUM_VERSION_KEY = "version";
    public static final String DEBEZIUM_CONNECTOR_KEY = "connector";
    public static final String SERVER_NAME_KEY = "name";
    public static final String TIMESTAMP_KEY = "ts_ms";
    public static final String SNAPSHOT_KEY = "snapshot";
    public static final String DATABASE_NAME_KEY = "db";
    public static final String SCHEMA_NAME_KEY = "schema";
    public static final String TABLE_NAME_KEY = "table";
    public static final String COLLECTION_NAME_KEY = "collection";
    public static final String SEQUENCE_KEY = "sequence";
    private final CommonConnectorConfig config;

    protected AbstractSourceInfo(CommonConnectorConfig config) {
        this.config = config;
    }

    public Schema schema() {
        return this.config.getSourceInfoStructMaker().schema();
    }

    protected SourceInfoStructMaker<AbstractSourceInfo> structMaker() {
        return this.config.getSourceInfoStructMaker();
    }

    protected abstract Instant timestamp();

    protected abstract SnapshotRecord snapshot();

    protected abstract String database();

    protected String serverName() {
        return this.config.getLogicalName();
    }

    public Struct struct() {
        return this.structMaker().struct(this);
    }

    protected String sequence() {
        return null;
    }
}
