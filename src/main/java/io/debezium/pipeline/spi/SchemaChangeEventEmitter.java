package io.debezium.pipeline.spi;

import io.debezium.schema.SchemaChangeEvent;

public interface SchemaChangeEventEmitter {
    void emitSchemaChangeEvent(Receiver var1) throws InterruptedException;

    public interface Receiver {
        void schemaChangeEvent(SchemaChangeEvent var1) throws InterruptedException;
    }
}
