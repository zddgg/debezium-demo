package io.debezium.pipeline.source.spi;

public interface ChangeEventSource {
    public interface ChangeEventSourceContext {
        boolean isRunning();
    }
}
