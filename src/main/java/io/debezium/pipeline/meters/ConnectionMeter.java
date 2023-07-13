package io.debezium.pipeline.meters;

import io.debezium.annotation.ThreadSafe;
import io.debezium.pipeline.metrics.traits.ConnectionMetricsMXBean;

import java.util.concurrent.atomic.AtomicBoolean;

@ThreadSafe
public class ConnectionMeter implements ConnectionMetricsMXBean {
    private final AtomicBoolean connected = new AtomicBoolean();

    public boolean isConnected() {
        return this.connected.get();
    }

    public void connected(boolean connected) {
        this.connected.set(connected);
    }

    public void reset() {
        this.connected.set(false);
    }
}
