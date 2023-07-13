package io.debezium.schema;

import io.debezium.data.Envelope;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.data.Schema;

public interface DataCollectionSchema {
    DataCollectionId id();

    Schema keySchema();

    Envelope getEnvelopeSchema();
}
