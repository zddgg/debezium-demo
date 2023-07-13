package io.debezium.connector.mysql;

import com.github.shyiko.mysql.binlog.event.EventData;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDataDeserializationException;

public class EventDataDeserializationExceptionData implements EventData {
    private static final long serialVersionUID = 1L;
    private final EventDataDeserializationException cause;

    public EventDataDeserializationExceptionData(EventDataDeserializationException cause) {
        this.cause = cause;
    }

    public EventDataDeserializationException getCause() {
        return this.cause;
    }

    public String toString() {
        return "EventDataDeserializationExceptionData [cause=" + this.cause + "]";
    }
}
