package io.debezium.document;

import io.debezium.annotation.Immutable;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Immutable
public class ArraySerdes implements Serializer<Array>, Deserializer<Array> {
    private static final ArrayWriter ARRAY_WRITER = ArrayWriter.defaultWriter();
    private static final ArrayReader ARRAY_READER = ArrayReader.defaultReader();

    public void configure(Map<String, ?> arg0, boolean arg1) {
    }

    public byte[] serialize(String topic, Array data) {
        return ARRAY_WRITER.writeAsBytes(data);
    }

    public Array deserialize(String topic, byte[] data) {
        try {
            return ARRAY_READER.readArray(this.bytesToString(data));
        } catch (IOException var4) {
            throw new RuntimeException(var4);
        }
    }

    public void close() {
    }

    private String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
