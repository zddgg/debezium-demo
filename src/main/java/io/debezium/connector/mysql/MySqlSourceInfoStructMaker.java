package io.debezium.connector.mysql;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.AbstractSourceInfoStructMaker;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public class MySqlSourceInfoStructMaker extends AbstractSourceInfoStructMaker<SourceInfo> {
    private Schema schema;

    public void init(String connector, String version, CommonConnectorConfig connectorConfig) {
        super.init(connector, version, connectorConfig);
        this.schema = this.commonSchemaBuilder().name("io.debezium.connector.mysql.Source").field("table", Schema.OPTIONAL_STRING_SCHEMA).field("server_id", Schema.INT64_SCHEMA).field("gtid", Schema.OPTIONAL_STRING_SCHEMA).field("file", Schema.STRING_SCHEMA).field("pos", Schema.INT64_SCHEMA).field("row", Schema.INT32_SCHEMA).field("thread", Schema.OPTIONAL_INT64_SCHEMA).field("query", Schema.OPTIONAL_STRING_SCHEMA).build();
    }

    public Schema schema() {
        return this.schema;
    }

    public Struct struct(SourceInfo sourceInfo) {
        Struct result = this.commonStruct(sourceInfo);
        result.put("server_id", sourceInfo.getServerId());
        if (sourceInfo.getCurrentGtid() != null) {
            result.put("gtid", sourceInfo.getCurrentGtid());
        }

        result.put("file", sourceInfo.getCurrentBinlogFilename());
        result.put("pos", sourceInfo.getCurrentBinlogPosition());
        result.put("row", sourceInfo.getCurrentRowNumber());
        if (sourceInfo.getThreadId() >= 0L) {
            result.put("thread", sourceInfo.getThreadId());
        }

        if (sourceInfo.table() != null) {
            result.put("table", sourceInfo.table());
        }

        if (sourceInfo.getQuery() != null) {
            result.put("query", sourceInfo.getQuery());
        }

        return result;
    }
}
