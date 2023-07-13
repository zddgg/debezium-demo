package io.debezium.serde.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.debezium.common.annotation.Incubating;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Incubating
public class JsonSerde<T> implements Serde<T> {
    private static final String PAYLOAD_FIELD = "payload";
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectReader reader;
    private boolean isKey;
    private JsonSerdeConfig config;

    public JsonSerde(Class<T> objectType) {
        this.mapper.registerModule(new JavaTimeModule());
        this.reader = this.mapper.readerFor(objectType);
    }

    public void configure(Map<String, ?> configs, boolean isKey) {
        this.isKey = isKey;
        this.config = new JsonSerdeConfig(configs);
        if (this.config.isUnknownPropertiesIgnored() && this.mapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
            this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.reader = this.reader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }

    }

    public void close() {
    }

    public Serializer<T> serializer() {
        return new JsonSerializer();
    }

    public Deserializer<T> deserializer() {
        return new JsonDeserializer();
    }

    private final class JsonSerializer implements Serializer<T> {
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        public byte[] serialize(String topic, T data) {
            try {
                return JsonSerde.this.mapper.writeValueAsBytes(data);
            } catch (JsonProcessingException var4) {
                throw new RuntimeException(var4);
            }
        }

        public void close() {
        }
    }

    private final class JsonDeserializer implements Deserializer<T> {
        public void configure(Map<String, ?> configs, boolean isKey) {
        }

        public T deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            } else {
                try {
                    JsonNode node = JsonSerde.this.mapper.readTree(data);
                    return JsonSerde.this.isKey ? this.readKey(node) : this.readValue(node);
                } catch (IOException var4) {
                    throw new RuntimeException(var4);
                }
            }
        }

        private T readValue(JsonNode node) throws IOException {
            JsonNode payload = node.get("payload");
            if (payload != null) {
                node = payload;
            }

            if (JsonSerde.this.config.asEnvelope()) {
                return JsonSerde.this.reader.readValue(node);
            } else {
                return node.has("source") && node.has(JsonSerde.this.config.sourceField()) ? JsonSerde.this.reader.readValue(node.get(JsonSerde.this.config.sourceField())) : JsonSerde.this.reader.readValue(node);
            }
        }

        private T readKey(JsonNode node) throws IOException {
            if (!node.isObject()) {
                return JsonSerde.this.reader.readValue(node);
            } else {
                JsonNode keys = node.has("payload") ? node.get("payload") : node;
                Iterator<String> keyFields = keys.fieldNames();
                if (keyFields.hasNext()) {
                    String id = (String) keyFields.next();
                    return !keyFields.hasNext() ? JsonSerde.this.reader.readValue(keys.get(id)) : JsonSerde.this.reader.readValue(keys);
                } else {
                    return JsonSerde.this.reader.readValue(keys);
                }
            }
        }

        public void close() {
        }
    }
}
