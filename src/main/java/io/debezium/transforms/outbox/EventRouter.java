package io.debezium.transforms.outbox;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;

import java.util.Map;

public class EventRouter<R extends ConnectRecord<R>> implements Transformation<R> {
    EventRouterDelegate<R> eventRouterDelegate = new EventRouterDelegate();

    public R apply(R r) {
        return this.eventRouterDelegate.apply(r, (rec) -> {
            return rec;
        });
    }

    public ConfigDef config() {
        return this.eventRouterDelegate.config();
    }

    public void close() {
        this.eventRouterDelegate.close();
    }

    public void configure(Map<String, ?> configMap) {
        this.eventRouterDelegate.configure(configMap);
    }
}
