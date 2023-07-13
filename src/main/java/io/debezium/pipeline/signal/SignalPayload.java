package io.debezium.pipeline.signal;

import io.debezium.document.Document;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;

import java.util.Map;

public class SignalPayload<P extends Partition> {
    public final String id;
    public final String type;
    public final Document data;
    public final P partition;
    public final OffsetContext offsetContext;
    public final Map<String, Object> additionalData;

    public SignalPayload(P partition, String id, String type, Document data, OffsetContext offsetContext, Map<String, Object> additionalData) {
        this.partition = partition;
        this.id = id;
        this.type = type;
        this.data = data;
        this.offsetContext = offsetContext;
        this.additionalData = additionalData;
    }

    public String toString() {
        return "SignalPayload{id='" + this.id + "', type='" + this.type + "', data=" + this.data + ", partition=" + this.partition + ", offsetContext=" + this.offsetContext + ", additionalData=" + this.additionalData + "}";
    }
}
