package io.debezium.relational;

import io.debezium.data.Envelope;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.util.Clock;

public class SnapshotChangeRecordEmitter<P extends Partition> extends RelationalChangeRecordEmitter<P> {
    private final Object[] row;

    public SnapshotChangeRecordEmitter(P partition, OffsetContext offset, Object[] row, Clock clock, RelationalDatabaseConnectorConfig connectorConfig) {
        super(partition, offset, clock, connectorConfig);
        this.row = row;
    }

    public Envelope.Operation getOperation() {
        return Envelope.Operation.READ;
    }

    protected Object[] getOldColumnValues() {
        throw new UnsupportedOperationException("Can't get old row values for READ record");
    }

    protected Object[] getNewColumnValues() {
        return this.row;
    }
}
