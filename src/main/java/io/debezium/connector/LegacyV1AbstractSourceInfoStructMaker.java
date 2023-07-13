package io.debezium.connector;

import io.debezium.config.CommonConnectorConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import java.util.Objects;

public abstract class LegacyV1AbstractSourceInfoStructMaker<T extends AbstractSourceInfo> implements SourceInfoStructMaker<T> {
    public static final String DEBEZIUM_VERSION_KEY = "version";
    public static final String DEBEZIUM_CONNECTOR_KEY = "connector";
    private String version;
    private String connector;
    protected String serverName;

    public void init(String connector, String version, CommonConnectorConfig connectorConfig) {
        this.connector = (String) Objects.requireNonNull(connector);
        this.version = (String) Objects.requireNonNull(version);
        this.serverName = connectorConfig.getLogicalName();
    }

    protected SchemaBuilder commonSchemaBuilder() {
        return SchemaBuilder.struct().field("version", Schema.OPTIONAL_STRING_SCHEMA).field("connector", Schema.OPTIONAL_STRING_SCHEMA);
    }

    protected Struct commonStruct() {
        return (new Struct(this.schema())).put("version", this.version).put("connector", this.connector);
    }
}
