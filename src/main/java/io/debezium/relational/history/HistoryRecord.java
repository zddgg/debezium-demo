package io.debezium.relational.history;

import io.debezium.document.Array;
import io.debezium.document.Document;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class HistoryRecord {
    private final Document doc;
    private static final TableChanges.TableChangesSerializer<Array> tableChangesSerializer = new JsonTableChangeSerializer();

    public HistoryRecord(Document document) {
        this.doc = document;
    }

    public HistoryRecord(Map<String, ?> source, Map<String, ?> position, String databaseName, String schemaName, String ddl, TableChanges changes, Instant timestamp) {
        this.doc = Document.create();
        Document src = this.doc.setDocument("source");
        if (source != null) {
            Objects.requireNonNull(src);
            source.forEach(src::set);
        }

        Document pos = this.doc.setDocument("position");
        if (position != null) {
            Iterator var10 = position.entrySet().iterator();

            while (var10.hasNext()) {
                Map.Entry<String, ?> positionElement = (Map.Entry) var10.next();
                if (positionElement.getValue() instanceof byte[]) {
                    pos.setBinary((CharSequence) positionElement.getKey(), (byte[]) positionElement.getValue());
                } else {
                    pos.set((CharSequence) positionElement.getKey(), positionElement.getValue());
                }
            }
        }

        if (timestamp != null) {
            this.doc.setNumber("ts_ms", timestamp.toEpochMilli());
        }

        if (databaseName != null) {
            this.doc.setString("databaseName", databaseName);
        }

        if (schemaName != null) {
            this.doc.setString("schemaName", schemaName);
        }

        if (ddl != null) {
            this.doc.setString("ddl", ddl);
        }

        if (changes != null) {
            this.doc.setArray("tableChanges", (Array) ((Array) tableChangesSerializer.serialize(changes)));
        }

    }

    public Document document() {
        return this.doc;
    }

    protected Document source() {
        return this.doc.getDocument("source");
    }

    protected Document position() {
        return this.doc.getDocument("position");
    }

    protected String databaseName() {
        return this.doc.getString("databaseName");
    }

    protected String schemaName() {
        return this.doc.getString("schemaName");
    }

    protected String ddl() {
        return this.doc.getString("ddl");
    }

    protected Array tableChanges() {
        return this.doc.getArray("tableChanges");
    }

    protected long timestamp() {
        return this.doc.getLong("ts_ms");
    }

    public String toString() {
        return this.doc.toString();
    }

    public boolean isValid() {
        return this.source() != null && this.position() != null;
    }

    public static final class Fields {
        public static final String SOURCE = "source";
        public static final String POSITION = "position";
        public static final String DATABASE_NAME = "databaseName";
        public static final String SCHEMA_NAME = "schemaName";
        public static final String DDL_STATEMENTS = "ddl";
        public static final String TABLE_CHANGES = "tableChanges";
        public static final String TIMESTAMP = "ts_ms";
    }
}
