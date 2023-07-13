package io.debezium.pipeline.source.spi;

import io.debezium.data.Envelope;
import io.debezium.pipeline.ConnectorEvent;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Struct;

public interface DataChangeEventListener<P extends Partition> {
    void onEvent(P var1, DataCollectionId var2, OffsetContext var3, Object var4, Struct var5, Envelope.Operation var6);

    void onFilteredEvent(P var1, String var2);

    void onFilteredEvent(P var1, String var2, Envelope.Operation var3);

    void onErroneousEvent(P var1, String var2);

    void onErroneousEvent(P var1, String var2, Envelope.Operation var3);

    void onConnectorEvent(P var1, ConnectorEvent var2);

    static <P extends Partition> DataChangeEventListener<P> NO_OP() {
        return new DataChangeEventListener<P>() {
            public void onFilteredEvent(P partition, String event) {
            }

            public void onFilteredEvent(P partition, String event, Envelope.Operation operation) {
            }

            public void onErroneousEvent(P partition, String event) {
            }

            public void onErroneousEvent(P partition, String event, Envelope.Operation operation) {
            }

            public void onConnectorEvent(P partition, ConnectorEvent event) {
            }

            public void onEvent(P partition, DataCollectionId source, OffsetContext offset, Object key, Struct value, Envelope.Operation operation) {
            }
        };
    }
}
