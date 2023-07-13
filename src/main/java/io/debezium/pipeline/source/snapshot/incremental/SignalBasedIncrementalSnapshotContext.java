package io.debezium.pipeline.source.snapshot.incremental;

import io.debezium.annotation.NotThreadSafe;

import java.util.Map;

@NotThreadSafe
public class SignalBasedIncrementalSnapshotContext<T> extends AbstractIncrementalSnapshotContext<T> {
    public SignalBasedIncrementalSnapshotContext() {
        this(true);
    }

    public SignalBasedIncrementalSnapshotContext(boolean useCatalogBeforeSchema) {
        super(useCatalogBeforeSchema);
    }

    public static <U> IncrementalSnapshotContext<U> load(Map<String, ?> offsets) {
        return load(offsets, true);
    }

    public static <U> SignalBasedIncrementalSnapshotContext<U> load(Map<String, ?> offsets, boolean useCatalogBeforeSchema) {
        SignalBasedIncrementalSnapshotContext<U> context = new SignalBasedIncrementalSnapshotContext(useCatalogBeforeSchema);
        init(context, offsets);
        return context;
    }
}
