package io.debezium.pipeline.source.spi;

import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.pipeline.spi.SnapshotResult;

public interface SnapshotChangeEventSource<P extends Partition, O extends OffsetContext> extends ChangeEventSource {
    SnapshotResult<O> execute(ChangeEventSourceContext var1, P var2, O var3) throws InterruptedException;
}
