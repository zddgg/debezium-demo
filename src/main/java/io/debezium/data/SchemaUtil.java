package io.debezium.data;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemaUtil {
    private SchemaUtil() {
    }

    public static String asString(Object field) {
        return (new RecordWriter()).append(field).toString();
    }

    public static String asString(Field field) {
        return (new RecordWriter()).append((Object) field).toString();
    }

    public static String asString(Struct struct) {
        return (new RecordWriter()).append((Object) struct).toString();
    }

    public static String asString(Schema schema) {
        return (new RecordWriter()).append((Object) schema).toString();
    }

    public static String asString(SourceRecord record) {
        return (new RecordWriter()).append((Object) record).toString();
    }

    public static String asDetailedString(Field field) {
        return (new RecordWriter()).detailed(true).append((Object) field).toString();
    }

    public static String asDetailedString(Struct struct) {
        return (new RecordWriter()).detailed(true).append((Object) struct).toString();
    }

    public static String asDetailedString(Schema schema) {
        return (new RecordWriter()).detailed(true).append((Object) schema).toString();
    }

    public static String asDetailedString(SourceRecord record) {
        return (new RecordWriter()).detailed(true).append((Object) record).toString();
    }

    private static class RecordWriter {
        private final StringBuilder sb = new StringBuilder();
        private boolean detailed = false;

        public RecordWriter detailed(boolean detailed) {
            this.detailed = detailed;
            return this;
        }

        public String toString() {
            return this.sb.toString();
        }

        public RecordWriter append(Object obj) {
            if (obj == null) {
                this.sb.append("null");
            } else if (obj instanceof Schema) {
                Schema schema = (Schema) obj;
                this.sb.append('{');
                if (schema.name() != null) {
                    this.appendFirst("name", schema.name());
                    this.appendAdditional("type", schema.type());
                } else {
                    this.appendFirst("type", schema.type());
                }

                this.appendAdditional("optional", schema.isOptional());
                this.appendAdditional("default", schema.defaultValue());
                if (schema.doc() != null) {
                    this.appendAdditional("doc", schema.doc());
                }

                if (schema.version() != null) {
                    this.appendAdditional("version", schema.version());
                }

                switch (schema.type()) {
                    case STRUCT:
                        this.appendAdditional("fields", schema.fields());
                        break;
                    case MAP:
                        this.appendAdditional("key", schema.keySchema());
                        this.appendAdditional("value", schema.valueSchema());
                        break;
                    case ARRAY:
                        this.appendAdditional("value", schema.valueSchema());
                }

                this.sb.append('}');
            } else {
                boolean first;
                Iterator var4;
                if (obj instanceof Struct) {
                    Struct s = (Struct) obj;
                    this.sb.append('{');
                    first = true;

                    Field field;
                    for (var4 = s.schema().fields().iterator(); var4.hasNext(); this.appendFirst(field.name(), s.get(field))) {
                        field = (Field) var4.next();
                        if (first) {
                            first = false;
                        } else {
                            this.sb.append(", ");
                        }
                    }

                    this.sb.append('}');
                } else if (obj instanceof ByteBuffer) {
                    this.append((ByteBuffer) obj);
                } else if (obj instanceof byte[]) {
                    this.append((byte[]) obj);
                } else if (obj instanceof Map) {
                    Map<?, ?> map = (Map) obj;
                    this.sb.append('{');
                    first = true;
                    var4 = map.entrySet().iterator();

                    while (var4.hasNext()) {
                        Map.Entry<?, ?> entry = (Map.Entry) var4.next();
                        if (first) {
                            this.appendFirst(entry.getKey().toString(), entry.getValue());
                            first = false;
                        } else {
                            this.appendAdditional(entry.getKey().toString(), entry.getValue());
                        }
                    }

                    this.sb.append('}');
                } else if (obj instanceof List) {
                    List<?> list = (List) obj;
                    this.sb.append('[');
                    first = true;

                    Object value;
                    for (var4 = list.iterator(); var4.hasNext(); this.append(value)) {
                        value = var4.next();
                        if (first) {
                            first = false;
                        } else {
                            this.sb.append(", ");
                        }
                    }

                    this.sb.append(']');
                } else if (obj instanceof Field) {
                    Field f = (Field) obj;
                    this.sb.append('{');
                    this.appendFirst("name", f.name());
                    this.appendAdditional("index", f.index());
                    this.appendAdditional("schema", f.schema());
                    this.sb.append('}');
                } else if (obj instanceof String) {
                    this.sb.append('"').append(obj.toString()).append('"');
                } else if (obj instanceof Schema.Type) {
                    this.sb.append('"').append(obj.toString()).append('"');
                } else if (obj instanceof SourceRecord) {
                    SourceRecord record = (SourceRecord) obj;
                    this.sb.append('{');
                    this.appendFirst("sourcePartition", record.sourcePartition());
                    this.appendAdditional("sourceOffset", record.sourceOffset());
                    this.appendAdditional("topic", record.topic());
                    this.appendAdditional("kafkaPartition", record.kafkaPartition());
                    if (this.detailed) {
                        this.appendAdditional("keySchema", record.keySchema());
                    }

                    this.appendAdditional("key", record.key());
                    if (this.detailed) {
                        this.appendAdditional("valueSchema", record.valueSchema());
                    }

                    this.appendAdditional("value", record.value());
                    this.sb.append('}');
                } else if (obj instanceof Time) {
                    Time time = (Time) obj;
                    this.append((Object) DateTimeFormatter.ISO_LOCAL_TIME.format(time.toLocalTime()));
                } else if (obj instanceof Date) {
                    Date date = (Date) obj;
                    this.append((Object) DateTimeFormatter.ISO_DATE.format(date.toLocalDate()));
                } else if (obj instanceof Timestamp) {
                    Timestamp ts = (Timestamp) obj;
                    Instant instant = ts.toInstant();
                    this.append((Object) DateTimeFormatter.ISO_INSTANT.format(instant));
                } else if (obj instanceof java.util.Date) {
                    java.util.Date date = (java.util.Date) obj;
                    this.append((Object) DateTimeFormatter.ISO_INSTANT.format(date.toInstant()));
                } else if (obj instanceof TemporalAccessor) {
                    TemporalAccessor temporal = (TemporalAccessor) obj;
                    this.append((Object) DateTimeFormatter.ISO_INSTANT.format(temporal));
                } else {
                    this.append((Object) obj.toString());
                }
            }

            return this;
        }

        protected void append(ByteBuffer b) {
            this.append(b.array());
        }

        protected void append(byte[] b) {
            this.sb.append(Arrays.toString(b));
        }

        protected void appendFirst(String name, Object value) {
            this.append((Object) name);
            this.sb.append(" : ");
            this.append(value);
        }

        protected void appendAdditional(String name, Object value) {
            this.sb.append(", ");
            this.append((Object) name);
            this.sb.append(" : ");
            this.append(value);
        }
    }
}
