package io.debezium.schema;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.data.Envelope;
import io.debezium.relational.history.ConnectTableChangeSerializer;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.util.HashSet;
import java.util.Set;

public class SchemaFactory {
    private static final String HEARTBEAT_KEY_SCHEMA_NAME = "io.debezium.connector.common.ServerNameKey";
    private static final int HEARTBEAT_KEY_SCHEMA_VERSION = 1;
    private static final String HEARTBEAT_VALUE_SCHEMA_NAME = "io.debezium.connector.common.Heartbeat";
    private static final int HEARTBEAT_VALUE_SCHEMA_VERSION = 1;
    private static final String TRANSACTION_METADATA_KEY_SCHEMA_NAME = "io.debezium.connector.common.TransactionMetadataKey";
    private static final int TRANSACTION_METADATA_KEY_SCHEMA_VERSION = 1;
    private static final String TRANSACTION_METADATA_VALUE_SCHEMA_NAME = "io.debezium.connector.common.TransactionMetadataValue";
    private static final int TRANSACTION_METADATA_VALUE_SCHEMA_VERSION = 1;
    private static final String TRANSACTION_BLOCK_SCHEMA_NAME = "event.block";
    private static final int TRANSACTION_BLOCK_SCHEMA_VERSION = 1;
    private static final String TRANSACTION_EVENT_COUNT_COLLECTION_SCHEMA_NAME = "event.collection";
    private static final int TRANSACTION_EVENT_COUNT_COLLECTION_SCHEMA_VERSION = 1;
    private static final String SCHEMA_HISTORY_CONNECTOR_SCHEMA_NAME_PREFIX = "io.debezium.connector.";
    private static final String SCHEMA_HISTORY_CONNECTOR_KEY_SCHEMA_NAME_SUFFIX = ".SchemaChangeKey";
    private static final int SCHEMA_HISTORY_CONNECTOR_KEY_SCHEMA_VERSION = 1;
    private static final String SCHEMA_HISTORY_CONNECTOR_VALUE_SCHEMA_NAME_SUFFIX = ".SchemaChangeValue";
    private static final int SCHEMA_HISTORY_CONNECTOR_VALUE_SCHEMA_VERSION = 1;
    private static final String SCHEMA_HISTORY_TABLE_SCHEMA_NAME = "io.debezium.connector.schema.Table";
    private static final int SCHEMA_HISTORY_TABLE_SCHEMA_VERSION = 1;
    private static final String SCHEMA_HISTORY_COLUMN_SCHEMA_NAME = "io.debezium.connector.schema.Column";
    private static final int SCHEMA_HISTORY_COLUMN_SCHEMA_VERSION = 1;
    private static final String SCHEMA_HISTORY_CHANGE_SCHEMA_NAME = "io.debezium.connector.schema.Change";
    private static final int SCHEMA_HISTORY_CHANGE_SCHEMA_VERSION = 1;
    private static final String NOTIFICATION_KEY_SCHEMA_NAME = "io.debezium.connector.common.NotificationKey";
    private static final Integer NOTIFICATION_KEY_SCHEMA_VERSION = 1;
    private static final String NOTIFICATION_VALUE_SCHEMA_NAME = "io.debezium.connector.common.Notification";
    private static final Integer NOTIFICATION_VALUE_SCHEMA_VERSION = 1;
    private static final SchemaFactory schemaFactoryObject = new SchemaFactory();

    public static SchemaFactory get() {
        return schemaFactoryObject;
    }

    public Schema heartbeatKeySchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.ServerNameKey")).version(1).field("serverName", Schema.STRING_SCHEMA).build();
    }

    public Schema heartbeatValueSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.Heartbeat")).version(1).field("ts_ms", Schema.INT64_SCHEMA).build();
    }

    public Schema transactionBlockSchema() {
        return SchemaBuilder.struct().optional().name("event.block").version(1).field("id", Schema.STRING_SCHEMA).field("total_order", Schema.INT64_SCHEMA).field("data_collection_order", Schema.INT64_SCHEMA).build();
    }

    public Schema transactionEventCountPerDataCollectionSchema() {
        return SchemaBuilder.struct().optional().name("event.collection").version(1).field("data_collection", Schema.STRING_SCHEMA).field("event_count", Schema.INT64_SCHEMA).build();
    }

    public Schema transactionKeySchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.TransactionMetadataKey")).version(1).field("id", Schema.STRING_SCHEMA).build();
    }

    public Schema transactionValueSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.TransactionMetadataValue")).version(1).field("status", Schema.STRING_SCHEMA).field("id", Schema.STRING_SCHEMA).field("event_count", Schema.OPTIONAL_INT64_SCHEMA).field("data_collections", SchemaBuilder.array(this.transactionEventCountPerDataCollectionSchema()).optional().build()).field("ts_ms", Schema.INT64_SCHEMA).build();
    }

    public Schema schemaHistoryColumnSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.schema.Column")).version(1).field("name", Schema.STRING_SCHEMA).field("jdbcType", Schema.INT32_SCHEMA).field("nativeType", Schema.OPTIONAL_INT32_SCHEMA).field("typeName", Schema.STRING_SCHEMA).field("typeExpression", Schema.OPTIONAL_STRING_SCHEMA).field("charsetName", Schema.OPTIONAL_STRING_SCHEMA).field("length", Schema.OPTIONAL_INT32_SCHEMA).field("scale", Schema.OPTIONAL_INT32_SCHEMA).field("position", Schema.INT32_SCHEMA).field("optional", Schema.OPTIONAL_BOOLEAN_SCHEMA).field("autoIncremented", Schema.OPTIONAL_BOOLEAN_SCHEMA).field("generated", Schema.OPTIONAL_BOOLEAN_SCHEMA).field("comment", Schema.OPTIONAL_STRING_SCHEMA).field("defaultValueExpression", Schema.OPTIONAL_STRING_SCHEMA).field("enumValues", SchemaBuilder.array(Schema.STRING_SCHEMA).optional().build()).build();
    }

    public Schema schemaHistoryTableSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.schema.Table")).version(1).field("defaultCharsetName", Schema.OPTIONAL_STRING_SCHEMA).field("primaryKeyColumnNames", SchemaBuilder.array(Schema.STRING_SCHEMA).optional().build()).field("columns", SchemaBuilder.array(this.schemaHistoryColumnSchema(adjuster)).build()).field("comment", Schema.OPTIONAL_STRING_SCHEMA).build();
    }

    public Schema schemaHistoryChangeSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.schema.Change")).version(1).field("type", Schema.STRING_SCHEMA).field("id", Schema.STRING_SCHEMA).field("table", this.schemaHistoryTableSchema(adjuster)).build();
    }

    public Schema schemaHistoryConnectorKeySchema(SchemaNameAdjuster adjuster, CommonConnectorConfig config) {
        return SchemaBuilder.struct().name(adjuster.adjust(String.format("%s%s%s", "io.debezium.connector.", config.getConnectorName(), ".SchemaChangeKey"))).version(1).field("databaseName", Schema.STRING_SCHEMA).build();
    }

    public Schema schemaHistoryConnectorValueSchema(SchemaNameAdjuster adjuster, CommonConnectorConfig config, ConnectTableChangeSerializer serializer) {
        return SchemaBuilder.struct().name(adjuster.adjust(String.format("%s%s%s", "io.debezium.connector.", config.getConnectorName(), ".SchemaChangeValue"))).version(1).field("source", config.getSourceInfoStructMaker().schema()).field("ts_ms", Schema.INT64_SCHEMA).field("databaseName", Schema.OPTIONAL_STRING_SCHEMA).field("schemaName", Schema.OPTIONAL_STRING_SCHEMA).field("ddl", Schema.OPTIONAL_STRING_SCHEMA).field("tableChanges", SchemaBuilder.array(serializer.getChangeSchema()).build()).build();
    }

    public Schema notificationKeySchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.NotificationKey")).version(NOTIFICATION_KEY_SCHEMA_VERSION).field("id", Schema.STRING_SCHEMA).build();
    }

    public Schema notificationValueSchema(SchemaNameAdjuster adjuster) {
        return SchemaBuilder.struct().name(adjuster.adjust("io.debezium.connector.common.Notification")).version(NOTIFICATION_VALUE_SCHEMA_VERSION).field("id", SchemaBuilder.STRING_SCHEMA).field("type", Schema.STRING_SCHEMA).field("aggregate_type", Schema.STRING_SCHEMA).field("additional_data", SchemaBuilder.map(Schema.STRING_SCHEMA, Schema.STRING_SCHEMA).optional().build()).build();
    }

    public SchemaBuilder datatypeBitsSchema(int length) {
        return SchemaBuilder.bytes().name("io.debezium.data.Bits").version(1).parameter("length", Integer.toString(length));
    }

    public SchemaBuilder datatypeEnumSchema(String allowedValues) {
        return SchemaBuilder.string().name("io.debezium.data.Enum").version(1).parameter("allowed", allowedValues);
    }

    public SchemaBuilder datatypeEnumSetSchema(String allowedValues) {
        return SchemaBuilder.string().name("io.debezium.data.EnumSet").version(1).parameter("allowed", allowedValues);
    }

    public SchemaBuilder datatypeJsonSchema() {
        return SchemaBuilder.string().name("io.debezium.data.Json").version(1);
    }

    public SchemaBuilder datatypeUuidSchema() {
        return SchemaBuilder.string().name("io.debezium.data.Uuid").version(1);
    }

    public SchemaBuilder datatypeVariableScaleDecimalSchema() {
        return SchemaBuilder.struct().name("io.debezium.data.VariableScaleDecimal").version(1).doc("Variable scaled decimal").field("scale", Schema.INT32_SCHEMA).field("value", Schema.BYTES_SCHEMA);
    }

    public SchemaBuilder datatypeXmlSchema() {
        return SchemaBuilder.string().name("io.debezium.data.Xml").version(1);
    }

    public Envelope.Builder datatypeEnvelopeSchema() {
        return new Envelope.Builder() {
            private final SchemaBuilder builder = SchemaBuilder.struct().version(1);
            private final Set<String> missingFields = new HashSet();

            public Envelope.Builder withSchema(Schema fieldSchema, String... fieldNames) {
                String[] var3 = fieldNames;
                int var4 = fieldNames.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    String fieldName = var3[var5];
                    this.builder.field(fieldName, fieldSchema);
                }

                return this;
            }

            public Envelope.Builder withName(String name) {
                this.builder.name(name);
                return this;
            }

            public Envelope.Builder withDoc(String doc) {
                this.builder.doc(doc);
                return this;
            }

            public Envelope build() {
                this.builder.field("op", Schema.STRING_SCHEMA);
                this.builder.field("ts_ms", Schema.OPTIONAL_INT64_SCHEMA);
                this.builder.field("transaction", SchemaFactory.this.transactionBlockSchema());
                this.checkFieldIsDefined("op");
                this.checkFieldIsDefined("before");
                this.checkFieldIsDefined("after");
                this.checkFieldIsDefined("source");
                this.checkFieldIsDefined("transaction");
                if (!this.missingFields.isEmpty()) {
                    throw new IllegalStateException("The envelope schema is missing field(s) " + String.join(", ", this.missingFields));
                } else {
                    return new Envelope(this.builder.build());
                }
            }

            private void checkFieldIsDefined(String fieldName) {
                if (this.builder.field(fieldName) == null) {
                    this.missingFields.add(fieldName);
                }

            }
        };
    }
}
