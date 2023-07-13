package io.debezium.connector.mysql;

import com.github.shyiko.mysql.binlog.event.deserialization.EventDataDeserializer;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;

import java.io.IOException;

public class StopEventDataDeserializer implements EventDataDeserializer<StopEventData> {
    public StopEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        return new StopEventData();
    }
}
