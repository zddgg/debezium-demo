package io.debezium.serde;

import io.debezium.common.annotation.Incubating;
import io.debezium.serde.json.JsonSerde;
import org.apache.kafka.common.serialization.Serde;

@Incubating
public class DebeziumSerdes {
    public static <T> Serde<T> payloadJson(Class<T> objectType) {
        return new JsonSerde(objectType);
    }
}
