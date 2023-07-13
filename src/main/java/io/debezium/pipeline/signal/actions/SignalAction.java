package io.debezium.pipeline.signal.actions;

import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.spi.Partition;

@FunctionalInterface
public interface SignalAction<P extends Partition> {
    boolean arrived(SignalPayload<P> var1) throws InterruptedException;
}
