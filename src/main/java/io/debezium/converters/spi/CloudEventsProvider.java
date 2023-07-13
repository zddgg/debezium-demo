package io.debezium.converters.spi;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public interface CloudEventsProvider {
    String getName();

    RecordParser createParser(Schema var1, Struct var2);

    CloudEventsMaker createMaker(RecordParser var1, SerializerType var2, String var3);
}
