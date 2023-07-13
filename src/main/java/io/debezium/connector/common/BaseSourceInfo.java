package io.debezium.connector.common;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.AbstractSourceInfo;
import io.debezium.connector.SnapshotRecord;

public abstract class BaseSourceInfo extends AbstractSourceInfo {
    protected SnapshotRecord snapshotRecord;

    public BaseSourceInfo(CommonConnectorConfig config) {
        super(config);
    }

    public boolean isSnapshot() {
        return this.snapshotRecord != SnapshotRecord.INCREMENTAL && this.snapshotRecord != SnapshotRecord.FALSE;
    }

    public void setSnapshot(SnapshotRecord snapshot) {
        this.snapshotRecord = snapshot;
    }

    public SnapshotRecord snapshot() {
        return this.snapshotRecord;
    }
}
