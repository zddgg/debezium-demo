package io.debezium.document;

import io.debezium.annotation.Immutable;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Immutable
public class DocumentSerdes implements Serializer<Document>, Deserializer<Document> {
    public static DocumentSerdes INSTANCE = new DocumentSerdes();
    private static final DocumentReader DOCUMENT_READER = DocumentReader.defaultReader();
    private static final DocumentWriter DOCUMENT_WRITER = DocumentWriter.defaultWriter();

    public void configure(Map<String, ?> arg0, boolean arg1) {
    }

    public byte[] serialize(String topic, Document data) {
        return DOCUMENT_WRITER.writeAsBytes(data);
    }

    public Document deserialize(String topic, byte[] data) {
        try {
            return DOCUMENT_READER.read(this.bytesToString(data));
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
