package io.debezium.pipeline.source.snapshot.incremental;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.jdbc.JdbcConnection;
import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.notification.NotificationService;
import io.debezium.pipeline.source.spi.DataChangeEventListener;
import io.debezium.pipeline.source.spi.SnapshotProgressListener;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.RelationalDatabaseConnectorConfig;
import io.debezium.schema.DatabaseSchema;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@NotThreadSafe
public class SignalBasedIncrementalSnapshotChangeEventSource<P extends Partition, T extends DataCollectionId> extends AbstractIncrementalSnapshotChangeEventSource<P, T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignalBasedIncrementalSnapshotChangeEventSource.class);
    private final String signalWindowStatement;

    public SignalBasedIncrementalSnapshotChangeEventSource(RelationalDatabaseConnectorConfig config, JdbcConnection jdbcConnection, EventDispatcher<P, T> dispatcher, DatabaseSchema<?> databaseSchema, Clock clock, SnapshotProgressListener<P> progressListener, DataChangeEventListener<P> dataChangeEventListener, NotificationService<P, ? extends OffsetContext> notificationService) {
        super(config, jdbcConnection, dispatcher, databaseSchema, clock, progressListener, dataChangeEventListener, notificationService);
        String var10001 = this.getSignalTableName(config.getSignalingDataCollectionId());
        this.signalWindowStatement = "INSERT INTO " + var10001 + " VALUES (?, ?, null)";
    }

    public void processMessage(Partition partition, DataCollectionId dataCollectionId, Object key, OffsetContext offsetContext) {
        this.context = (IncrementalSnapshotContext<T>) offsetContext.getIncrementalSnapshotContext();
        if (this.context == null) {
            LOGGER.warn("Context is null, skipping message processing");
        } else {
            LOGGER.trace("Checking window for table '{}', key '{}', window contains '{}'", new Object[]{dataCollectionId, key, this.window});
            if (!this.window.isEmpty() && this.context.deduplicationNeeded()) {
                this.deduplicateWindow(dataCollectionId, key);
            }

        }
    }

    protected void emitWindowOpen() throws SQLException {
        this.jdbcConnection.prepareUpdate(this.signalWindowStatement, (x) -> {
            LOGGER.trace("Emitting open window for chunk = '{}'", this.context.currentChunkId());
            x.setString(1, this.context.currentChunkId() + "-open");
            x.setString(2, "snapshot-window-open");
        });
        this.jdbcConnection.commit();
    }

    protected void emitWindowClose(Partition partition, OffsetContext offsetContext) throws SQLException {
        this.jdbcConnection.prepareUpdate(this.signalWindowStatement, (x) -> {
            LOGGER.trace("Emitting close window for chunk = '{}'", this.context.currentChunkId());
            x.setString(1, this.context.currentChunkId() + "-close");
            x.setString(2, "snapshot-window-close");
        });
        this.jdbcConnection.commit();
    }
}
