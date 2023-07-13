package io.debezium.connector;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.data.Enum;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import java.time.Instant;
import java.util.Objects;

public abstract class AbstractSourceInfoStructMaker<T extends AbstractSourceInfo> implements SourceInfoStructMaker<T> {
    public static final Schema SNAPSHOT_RECORD_SCHEMA = Enum.builder("true,last,false,incremental").defaultValue("false").optional().build();
    private String version;
    private String connector;
    private String serverName;

    public void init(String connector, String version, CommonConnectorConfig connectorConfig) {
        this.connector = (String) Objects.requireNonNull(connector);
        this.version = (String) Objects.requireNonNull(version);
        this.serverName = connectorConfig.getLogicalName();
    }

    protected SchemaBuilder commonSchemaBuilder() {
        return SchemaBuilder.struct().field("version", Schema.STRING_SCHEMA).field("connector", Schema.STRING_SCHEMA).field("name", Schema.STRING_SCHEMA).field("ts_ms", Schema.INT64_SCHEMA).field("snapshot", SNAPSHOT_RECORD_SCHEMA).field("db", Schema.STRING_SCHEMA).field("sequence", Schema.OPTIONAL_STRING_SCHEMA);
    }

    protected Struct commonStruct(T sourceInfo) {
        Instant timestamp = sourceInfo.timestamp() == null ? Instant.now() : sourceInfo.timestamp();
        String database = sourceInfo.database() == null ? "" : sourceInfo.database();
        Struct ret = (new Struct(this.schema())).put("version", this.version).put("connector", this.connector).put("name", this.serverName).put("ts_ms", timestamp.toEpochMilli()).put("db", database);
        String sequence = sourceInfo.sequence();
        if (sequence != null) {
            ret.put("sequence", sequence);
        }

        SnapshotRecord snapshot = sourceInfo.snapshot();
        if (snapshot != null) {
            snapshot.toSource(ret);
        }

        return ret;
    }
}
