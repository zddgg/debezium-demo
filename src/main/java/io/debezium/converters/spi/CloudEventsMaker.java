package io.debezium.converters.spi;

import io.debezium.util.Collect;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public abstract class CloudEventsMaker {
    private static final String SCHEMA_URL_PATH = "/schemas/ids/";
    public static final String CLOUDEVENTS_SPECVERSION = "1.0";
    private final SerializerType dataContentType;
    private final String dataSchemaUriBase;
    private final Schema ceDataAttributeSchema;
    protected final RecordParser recordParser;
    static final Map<SerializerType, String> CONTENT_TYPE_NAME_MAP;

    protected CloudEventsMaker(RecordParser parser, SerializerType contentType, String dataSchemaUriBase) {
        this.recordParser = parser;
        this.dataContentType = contentType;
        this.dataSchemaUriBase = dataSchemaUriBase;
        this.ceDataAttributeSchema = this.recordParser.dataSchema();
    }

    public abstract String ceId();

    public String ceSource(String logicalName) {
        String var10000 = this.recordParser.connectorType();
        return "/debezium/" + var10000 + "/" + logicalName;
    }

    public String ceSpecversion() {
        return "1.0";
    }

    public String ceType() {
        return "io.debezium." + this.recordParser.connectorType() + ".datachangeevent";
    }

    public String ceDatacontenttype() {
        return (String) CONTENT_TYPE_NAME_MAP.get(this.dataContentType);
    }

    public String ceDataschemaUri(String schemaId) {
        return this.dataSchemaUriBase + "/schemas/ids/" + schemaId;
    }

    public String ceTime() {
        long time = (Long) this.recordParser.getMetadata("ts_ms");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(time);
    }

    public Schema ceDataAttributeSchema() {
        return this.ceDataAttributeSchema;
    }

    public Struct ceDataAttribute() {
        return this.recordParser.data();
    }

    public String ceEnvelopeSchemaName() {
        Object var10000 = this.recordParser.getMetadata("name");
        return "" + var10000 + "." + this.recordParser.getMetadata("db") + ".CloudEvents.Envelope";
    }

    static {
        CONTENT_TYPE_NAME_MAP = Collect.hashMapOf(SerializerType.JSON, "application/json", SerializerType.AVRO, "application/avro");
    }

    public static final class FieldName {
        public static final String ID = "id";
        public static final String SOURCE = "source";
        public static final String SPECVERSION = "specversion";
        public static final String TYPE = "type";
        public static final String DATACONTENTTYPE = "datacontenttype";
        public static final String DATASCHEMA = "dataschema";
        public static final String SUBJECT = "subject";
        public static final String TIME = "time";
        public static final String DATA = "data";
        public static final String SCHEMA_FIELD_NAME = "schema";
        public static final String PAYLOAD_FIELD_NAME = "payload";
    }
}
