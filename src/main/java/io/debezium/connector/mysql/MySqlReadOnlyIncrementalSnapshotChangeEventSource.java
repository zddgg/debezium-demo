package io.debezium.connector.mysql;

import io.debezium.DebeziumException;
import io.debezium.jdbc.JdbcConnection;
import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.notification.NotificationService;
import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.source.snapshot.incremental.AbstractIncrementalSnapshotChangeEventSource;
import io.debezium.pipeline.source.spi.DataChangeEventListener;
import io.debezium.pipeline.source.spi.SnapshotProgressListener;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.schema.DatabaseSchema;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class MySqlReadOnlyIncrementalSnapshotChangeEventSource<T extends DataCollectionId> extends AbstractIncrementalSnapshotChangeEventSource<MySqlPartition, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlReadOnlyIncrementalSnapshotChangeEventSource.class);
    private final String showMasterStmt = "SHOW MASTER STATUS";

    public MySqlReadOnlyIncrementalSnapshotChangeEventSource(RelationalDatabaseConnectorConfig config, JdbcConnection jdbcConnection, EventDispatcher<MySqlPartition, T> dispatcher, DatabaseSchema<?> databaseSchema, Clock clock, SnapshotProgressListener<MySqlPartition> progressListener, DataChangeEventListener<MySqlPartition> dataChangeEventListener, NotificationService<MySqlPartition, MySqlOffsetContext> notificationService) {
        super(config, jdbcConnection, dispatcher, databaseSchema, clock, progressListener, dataChangeEventListener, notificationService);
    }

    public void processMessage(MySqlPartition partition, DataCollectionId dataCollectionId, Object key, OffsetContext offsetContext) throws InterruptedException {
        if (this.getContext() == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            LOGGER.trace("Checking window for table '{}', key '{}', window contains '{}'", new Object[]{dataCollectionId, key, this.window});
            boolean windowClosed = this.getContext().updateWindowState(offsetContext);
            if (windowClosed) {
                this.sendWindowEvents(partition, offsetContext);
                this.readChunk(partition, offsetContext);
            } else if (!this.window.isEmpty() && this.getContext().deduplicationNeeded()) {
                this.deduplicateWindow(dataCollectionId, key);
            }

        }
    }

    public void processHeartbeat(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        if (this.getContext() == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            this.readUntilGtidChange(partition, offsetContext);
        }
    }

    private void readUntilGtidChange(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        String currentGtid = this.getContext().getCurrentGtid(offsetContext);

        while (this.getContext().snapshotRunning() && this.getContext().reachedHighWatermark(currentGtid)) {
            this.getContext().closeWindow();
            this.sendWindowEvents(partition, offsetContext);
            this.readChunk(partition, offsetContext);
            if (currentGtid == null && this.getContext().watermarksChanged()) {
                return;
            }
        }

    }

    public void processFilteredEvent(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        if (this.getContext() == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            boolean windowClosed = this.getContext().updateWindowState(offsetContext);
            if (windowClosed) {
                this.sendWindowEvents(partition, offsetContext);
                this.readChunk(partition, offsetContext);
            }

        }
    }

    public void processTransactionStartedEvent(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        if (this.getContext() == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            boolean windowClosed = this.getContext().updateWindowState(offsetContext);
            if (windowClosed) {
                this.sendWindowEvents(partition, offsetContext);
                this.readChunk(partition, offsetContext);
            }

        }
    }

    public void processTransactionCommittedEvent(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        if (this.getContext() == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            this.readUntilGtidChange(partition, offsetContext);
        }
    }

    protected void updateLowWatermark() {
        MySqlReadOnlyIncrementalSnapshotContext var10001 = this.getContext();
        Objects.requireNonNull(var10001);
        this.getExecutedGtidSet(var10001::setLowWatermark);
    }

    protected void updateHighWatermark() {
        MySqlReadOnlyIncrementalSnapshotContext var10001 = this.getContext();
        Objects.requireNonNull(var10001);
        this.getExecutedGtidSet(var10001::setHighWatermark);
    }

    private void getExecutedGtidSet(Consumer<GtidSet> watermark) {
        try {
            this.jdbcConnection.query("SHOW MASTER STATUS", (rs) -> {
                if (rs.next()) {
                    if (rs.getMetaData().getColumnCount() <= 4) {
                        throw new UnsupportedOperationException("Need to add support for executed GTIDs for versions prior to 5.6.5");
                    }

                    String gtidSet = rs.getString(5);
                    watermark.accept(new GtidSet(gtidSet));
                }

            });
            this.jdbcConnection.commit();
        } catch (SQLException var3) {
            throw new DebeziumException(var3);
        }
    }

    protected void emitWindowOpen() {
        this.updateLowWatermark();
    }

    protected void emitWindowClose(MySqlPartition partition, OffsetContext offsetContext) throws InterruptedException {
        this.updateHighWatermark();
        if (this.getContext().serverUuidChanged()) {
            this.rereadChunk(partition, offsetContext);
        }

    }

    protected void sendEvent(MySqlPartition partition, EventDispatcher<MySqlPartition, T> dispatcher, OffsetContext offsetContext, Object[] row) throws InterruptedException {
        SourceInfo sourceInfo = ((MySqlOffsetContext) offsetContext).getSource();
        String query = sourceInfo.getQuery();
        sourceInfo.setQuery((String) null);
        super.sendEvent(partition, dispatcher, offsetContext, row);
        sourceInfo.setQuery(query);
    }

    public void addDataCollectionNamesToSnapshot(SignalPayload<MySqlPartition> signalPayload, List<String> dataCollectionIds, Optional<String> additionalCondition, Optional<String> surrogateKey) throws InterruptedException {
        Map<String, Object> additionalData = signalPayload.additionalData;
        super.addDataCollectionNamesToSnapshot(signalPayload, dataCollectionIds, additionalCondition, surrogateKey);
        this.getContext().setSignalOffset((Long) additionalData.get("channelOffset"));
    }

    public void stopSnapshot(MySqlPartition partition, OffsetContext offsetContext, Map<String, Object> additionalData, List<String> dataCollectionIds) {
        super.stopSnapshot(partition, offsetContext, additionalData, dataCollectionIds);
        this.getContext().setSignalOffset((Long) additionalData.get("channelOffset"));
    }

    private MySqlReadOnlyIncrementalSnapshotContext<T> getContext() {
        return (MySqlReadOnlyIncrementalSnapshotContext) this.context;
    }
}
