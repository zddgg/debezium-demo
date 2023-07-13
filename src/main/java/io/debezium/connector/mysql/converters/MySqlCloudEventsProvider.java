package io.debezium.connector.mysql.converters;

import io.debezium.connector.mysql.Module;
import io.debezium.converters.spi.CloudEventsMaker;
import io.debezium.converters.spi.CloudEventsProvider;
import io.debezium.converters.spi.RecordParser;
import io.debezium.converters.spi.SerializerType;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public class MySqlCloudEventsProvider implements CloudEventsProvider {
    public String getName() {
        return Module.name();
    }

    public RecordParser createParser(Schema schema, Struct record) {
        return new MySqlRecordParser(schema, record);
    }

    public CloudEventsMaker createMaker(RecordParser parser, SerializerType contentType, String dataSchemaUriBase) {
        return new MySqlCloudEventsMaker(parser, contentType, dataSchemaUriBase);
    }
}
