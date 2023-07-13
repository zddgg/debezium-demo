package io.debezium.data;

import io.debezium.schema.SchemaFactory;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Envelope {
    public static final int SCHEMA_VERSION = 1;
    public static final boolean OPERATION_REQUIRED = true;
    public static final Set<String> ALL_FIELD_NAMES;
    public static String SCHEMA_NAME_SUFFIX;
    private final Schema schema;

    public static Builder defineSchema() {
        return SchemaFactory.get().datatypeEnvelopeSchema();
    }

    public static Envelope fromSchema(Schema schema) {
        return new Envelope(schema);
    }

    public Envelope(Schema schema) {
        this.schema = schema;
    }

    public Schema schema() {
        return this.schema;
    }

    public Struct read(Object record, Struct source, Instant timestamp) {
        Struct struct = new Struct(this.schema);
        struct.put("op", Operation.READ.code());
        struct.put("after", record);
        if (source != null) {
            struct.put("source", source);
        }

        if (timestamp != null) {
            struct.put("ts_ms", timestamp.toEpochMilli());
        }

        return struct;
    }

    public Struct create(Object record, Struct source, Instant timestamp) {
        Struct struct = new Struct(this.schema);
        struct.put("op", Operation.CREATE.code());
        struct.put("after", record);
        if (source != null) {
            struct.put("source", source);
        }

        if (timestamp != null) {
            struct.put("ts_ms", timestamp.toEpochMilli());
        }

        return struct;
    }

    public Struct update(Object before, Struct after, Struct source, Instant timestamp) {
        Struct struct = new Struct(this.schema);
        struct.put("op", Operation.UPDATE.code());
        if (before != null) {
            struct.put("before", before);
        }

        struct.put("after", after);
        if (source != null) {
            struct.put("source", source);
        }

        if (timestamp != null) {
            struct.put("ts_ms", timestamp.toEpochMilli());
        }

        return struct;
    }

    public Struct delete(Object before, Struct source, Instant timestamp) {
        Struct struct = new Struct(this.schema);
        struct.put("op", Operation.DELETE.code());
        if (before != null) {
            struct.put("before", before);
        }

        if (source != null) {
            struct.put("source", source);
        }

        if (timestamp != null) {
            struct.put("ts_ms", timestamp.toEpochMilli());
        }

        return struct;
    }

    public Struct truncate(Struct source, Instant timestamp) {
        Struct struct = new Struct(this.schema);
        struct.put("op", Operation.TRUNCATE.code());
        struct.put("source", source);
        struct.put("ts_ms", timestamp.toEpochMilli());
        return struct;
    }

    public static Operation operationFor(SourceRecord record) {
        Struct value = (Struct) record.value();
        Field opField = value.schema().field("op");
        return opField != null ? Operation.forCode(value.getString(opField.name())) : null;
    }

    public static String schemaName(String type) {
        return type + SCHEMA_NAME_SUFFIX;
    }

    public static boolean isEnvelopeSchema(String schemaName) {
        return schemaName.endsWith(SCHEMA_NAME_SUFFIX);
    }

    public static boolean isEnvelopeSchema(Schema schema) {
        return isEnvelopeSchema(schema.name());
    }

    static {
        Set<String> fields = new HashSet();
        fields.add("op");
        fields.add("ts_ms");
        fields.add("before");
        fields.add("after");
        fields.add("source");
        fields.add("transaction");
        ALL_FIELD_NAMES = Collections.unmodifiableSet(fields);
        SCHEMA_NAME_SUFFIX = ".Envelope";
    }

    public interface Builder {
        default Builder withRecord(Schema schema) {
            return this.withSchema(schema, "before", "after");
        }

        default Builder withSource(Schema sourceSchema) {
            return this.withSchema(sourceSchema, "source");
        }

        Builder withSchema(Schema var1, String... var2);

        Builder withName(String var1);

        Builder withDoc(String var1);

        Envelope build();
    }

    public static final class FieldName {
        public static final String BEFORE = "before";
        public static final String AFTER = "after";
        public static final String OPERATION = "op";
        public static final String SOURCE = "source";
        public static final String TRANSACTION = "transaction";
        public static final String TIMESTAMP = "ts_ms";
    }

    public static enum Operation {
        READ("r"),
        CREATE("c"),
        UPDATE("u"),
        DELETE("d"),
        TRUNCATE("t"),
        MESSAGE("m");

        private final String code;

        private Operation(String code) {
            this.code = code;
        }

        public static Operation forCode(String code) {
            Operation[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Operation op = var1[var3];
                if (op.code().equalsIgnoreCase(code)) {
                    return op;
                }
            }

            return null;
        }

        public String code() {
            return this.code;
        }

        // $FF: synthetic method
        private static Operation[] $values() {
            return new Operation[]{READ, CREATE, UPDATE, DELETE, TRUNCATE, MESSAGE};
        }
    }
}
