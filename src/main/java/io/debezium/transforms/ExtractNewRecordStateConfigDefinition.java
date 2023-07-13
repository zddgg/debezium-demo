package io.debezium.transforms;

import io.debezium.config.EnumeratedValue;
import io.debezium.config.Field;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

public class ExtractNewRecordStateConfigDefinition {
    public static final String DEBEZIUM_OPERATION_HEADER_KEY = "__op";
    public static final String DELETED_FIELD = "__deleted";
    public static final String METADATA_FIELD_PREFIX = "__";
    public static final Field DROP_TOMBSTONES;
    public static final Field HANDLE_DELETES;
    public static final Field ROUTE_BY_FIELD;
    public static final Field ADD_FIELDS_PREFIX;
    public static final Field ADD_FIELDS;
    public static final Field ADD_HEADERS_PREFIX;
    public static final Field ADD_HEADERS;

    static {
        DROP_TOMBSTONES = Field.create("drop.tombstones").withDisplayName("Drop tombstones").withType(Type.BOOLEAN).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault(true).withDescription("Debezium by default generates a tombstone record to enable Kafka compaction after a delete record was generated. This record is usually filtered out to avoid duplicates as a delete record is converted to a tombstone record, too");
        HANDLE_DELETES = Field.create("delete.handling.mode").withDisplayName("Handle delete records").withEnum(DeleteHandling.class, DeleteHandling.DROP).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("How to handle delete records. Options are: none - records are passed,drop - records are removed (the default),rewrite - __deleted field is added to records.");
        ROUTE_BY_FIELD = Field.create("route.by.field").withDisplayName("Route by field name").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withDescription("The column which determines how the events will be routed, the value will replace the topic name.").withDefault("");
        ADD_FIELDS_PREFIX = Field.create("add.fields.prefix").withDisplayName("Field prefix to be added to each field.").withType(Type.STRING).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault("__").withDescription("Adds this prefix to each field listed.");
        ADD_FIELDS = Field.create("add.fields").withDisplayName("Adds the specified field(s) to the message if they exist.").withType(Type.LIST).withWidth(Width.LONG).withImportance(Importance.LOW).withDefault("").withDescription("Adds each field listed, prefixed with __ (or __<struct>_ if the struct is specified). Example: 'version,connector,source.ts_ms' would add __version, __connector and __source_ts_ms fields. Optionally one can also map new field name like version:VERSION,connector:CONNECTOR,source.ts_ms:EVENT_TIMESTAMP.Please note that the new field name is case-sensitive.");
        ADD_HEADERS_PREFIX = Field.create("add.headers.prefix").withDisplayName("Header prefix to be added to each header.").withType(Type.STRING).withWidth(Width.SHORT).withImportance(Importance.LOW).withDefault("__").withDescription("Adds this prefix listed to each header.");
        ADD_HEADERS = Field.create("add.headers").withDisplayName("Adds the specified fields to the header if they exist.").withType(Type.LIST).withWidth(Width.LONG).withImportance(Importance.LOW).withDefault("").withDescription("Adds each field listed to the header,  __ (or __<struct>_ if the struct is specified). Example: 'version,connector,source.ts_ms' would add __version, __connector and __source_ts_ms fields. Optionally one can also map new field name like version:VERSION,connector:CONNECTOR,source.ts_ms:EVENT_TIMESTAMP.Please note that the new field name is case-sensitive.");
    }

    public static enum DeleteHandling implements EnumeratedValue {
        DROP("drop"),
        REWRITE("rewrite"),
        NONE("none");

        private final String value;

        private DeleteHandling(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static DeleteHandling parse(String value) {
            if (value == null) {
                return null;
            } else {
                value = value.trim();
                DeleteHandling[] var1 = values();
                int var2 = var1.length;

                for (int var3 = 0; var3 < var2; ++var3) {
                    DeleteHandling option = var1[var3];
                    if (option.getValue().equalsIgnoreCase(value)) {
                        return option;
                    }
                }

                return null;
            }
        }

        public static DeleteHandling parse(String value, String defaultValue) {
            DeleteHandling mode = parse(value);
            if (mode == null && defaultValue != null) {
                mode = parse(defaultValue);
            }

            return mode;
        }

        // $FF: synthetic method
        private static DeleteHandling[] $values() {
            return new DeleteHandling[]{DROP, REWRITE, NONE};
        }
    }
}
