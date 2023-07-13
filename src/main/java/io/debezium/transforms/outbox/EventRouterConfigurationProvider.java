package io.debezium.transforms.outbox;

import java.util.Map;

public interface EventRouterConfigurationProvider {
    String getName();

    void configure(Map<String, ?> var1);

    String getFieldEventId();

    String getFieldEventKey();

    String getFieldEventTimestamp();

    String getFieldPayload();

    String getRouteByField();
}
