package io.debezium.pipeline.metrics;

import io.debezium.annotation.ThreadSafe;
import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.pipeline.meters.SnapshotMeter;
import io.debezium.pipeline.source.spi.EventMetadataProvider;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.TableId;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@ThreadSafe
public class DefaultSnapshotChangeEventSourceMetrics<P extends Partition> extends PipelineMetrics<P> implements SnapshotChangeEventSourceMetrics<P>, SnapshotChangeEventSourceMetricsMXBean {
    private final SnapshotMeter snapshotMeter;

    public <T extends CdcSourceTaskContext> DefaultSnapshotChangeEventSourceMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider) {
        super(taskContext, "snapshot", changeEventQueueMetrics, metadataProvider);
        this.snapshotMeter = new SnapshotMeter(taskContext.getClock());
    }

    public <T extends CdcSourceTaskContext> DefaultSnapshotChangeEventSourceMetrics(T taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider, Map<String, String> tags) {
        super(taskContext, changeEventQueueMetrics, metadataProvider, tags);
        this.snapshotMeter = new SnapshotMeter(taskContext.getClock());
    }

    public int getTotalTableCount() {
        return this.snapshotMeter.getTotalTableCount();
    }

    public int getRemainingTableCount() {
        return this.snapshotMeter.getRemainingTableCount();
    }

    public boolean getSnapshotRunning() {
        return this.snapshotMeter.getSnapshotRunning();
    }

    public boolean getSnapshotPaused() {
        return this.snapshotMeter.getSnapshotPaused();
    }

    public boolean getSnapshotCompleted() {
        return this.snapshotMeter.getSnapshotCompleted();
    }

    public boolean getSnapshotAborted() {
        return this.snapshotMeter.getSnapshotAborted();
    }

    public long getSnapshotDurationInSeconds() {
        return this.snapshotMeter.getSnapshotDurationInSeconds();
    }

    public long getSnapshotPausedDurationInSeconds() {
        return this.snapshotMeter.getSnapshotPausedDurationInSeconds();
    }

    public String[] getCapturedTables() {
        return this.snapshotMeter.getCapturedTables();
    }

    public void monitoredDataCollectionsDetermined(P partition, Iterable<? extends DataCollectionId> dataCollectionIds) {
        this.snapshotMeter.monitoredDataCollectionsDetermined(dataCollectionIds);
    }

    public void dataCollectionSnapshotCompleted(P partition, DataCollectionId dataCollectionId, long numRows) {
        this.snapshotMeter.dataCollectionSnapshotCompleted(dataCollectionId, numRows);
    }

    public void snapshotStarted(P partition) {
        this.snapshotMeter.snapshotStarted();
    }

    public void snapshotPaused(P partition) {
        this.snapshotMeter.snapshotPaused();
    }

    public void snapshotResumed(P partition) {
        this.snapshotMeter.snapshotResumed();
    }

    public void snapshotCompleted(P partition) {
        this.snapshotMeter.snapshotCompleted();
    }

    public void snapshotAborted(P partition) {
        this.snapshotMeter.snapshotAborted();
    }

    public void rowsScanned(P partition, TableId tableId, long numRows) {
        this.snapshotMeter.rowsScanned(tableId, numRows);
    }

    public ConcurrentMap<String, Long> getRowsScanned() {
        return this.snapshotMeter.getRowsScanned();
    }

    public void currentChunk(P partition, String chunkId, Object[] chunkFrom, Object[] chunkTo) {
        this.snapshotMeter.currentChunk(chunkId, chunkFrom, chunkTo);
    }

    public void currentChunk(P partition, String chunkId, Object[] chunkFrom, Object[] chunkTo, Object[] tableTo) {
        this.snapshotMeter.currentChunk(chunkId, chunkFrom, chunkTo, tableTo);
    }

    public String getChunkId() {
        return this.snapshotMeter.getChunkId();
    }

    public String getChunkFrom() {
        return this.snapshotMeter.getChunkFrom();
    }

    public String getChunkTo() {
        return this.snapshotMeter.getChunkTo();
    }

    public String getTableFrom() {
        return this.snapshotMeter.getTableFrom();
    }

    public String getTableTo() {
        return this.snapshotMeter.getTableTo();
    }

    public void reset() {
        super.reset();
        this.snapshotMeter.reset();
    }
}
