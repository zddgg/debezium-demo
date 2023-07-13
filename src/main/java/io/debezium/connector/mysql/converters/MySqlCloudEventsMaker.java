package io.debezium.connector.mysql.converters;

import io.debezium.converters.spi.CloudEventsMaker;
import io.debezium.converters.spi.RecordParser;
import io.debezium.converters.spi.SerializerType;

public class MySqlCloudEventsMaker extends CloudEventsMaker {
    public MySqlCloudEventsMaker(RecordParser parser, SerializerType contentType, String dataSchemaUriBase) {
        super(parser, contentType, dataSchemaUriBase);
    }

    public String ceId() {
        Object var10000 = this.recordParser.getMetadata("name");
        return "name:" + var10000 + ";file:" + this.recordParser.getMetadata("file") + ";pos:" + this.recordParser.getMetadata("pos");
    }
}
