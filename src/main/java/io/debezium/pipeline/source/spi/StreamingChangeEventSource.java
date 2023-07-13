package io.debezium.pipeline.source.spi;

import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;

import java.util.Map;

public interface StreamingChangeEventSource<P extends Partition, O extends OffsetContext> extends ChangeEventSource {
    default void init(O offsetContext) throws InterruptedException {
    }

    void execute(ChangeEventSourceContext var1, P var2, O var3) throws InterruptedException;

    default boolean executeIteration(ChangeEventSourceContext context, P partition, O offsetContext) throws InterruptedException {
        throw new UnsupportedOperationException("Currently unsupported by the connector");
    }

    default void commitOffset(Map<String, ?> partition, Map<String, ?> offset) {
    }

    default O getOffsetContext() {
        return null;
    }
}
