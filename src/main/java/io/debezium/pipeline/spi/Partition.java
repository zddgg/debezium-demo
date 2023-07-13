package io.debezium.pipeline.spi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Partition {
    Map<String, String> getSourcePartition();

    default Map<String, String> getLoggingContext() {
        return Collections.emptyMap();
    }

    public interface Provider<P extends Partition> {
        Set<P> getPartitions();
    }
}
