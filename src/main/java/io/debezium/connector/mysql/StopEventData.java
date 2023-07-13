package io.debezium.connector.mysql;

import com.github.shyiko.mysql.binlog.event.EventData;

public class StopEventData implements EventData {
    private static final long serialVersionUID = 1L;

    public String toString() {
        return "StopEventData{}";
    }
}
