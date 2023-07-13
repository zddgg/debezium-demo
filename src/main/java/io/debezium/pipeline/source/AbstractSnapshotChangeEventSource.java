package io.debezium.pipeline.source;

import io.debezium.DebeziumException;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.config.ConfigurationDefaults;
import io.debezium.pipeline.source.spi.SnapshotChangeEventSource;
import io.debezium.pipeline.source.spi.SnapshotProgressListener;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import io.debezium.pipeline.spi.SnapshotResult;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Clock;
import io.debezium.util.Metronome;
import io.debezium.util.Threads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AbstractSnapshotChangeEventSource<P extends Partition, O extends OffsetContext> implements SnapshotChangeEventSource<P, O>, AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSnapshotChangeEventSource.class);
    public static final Duration LOG_INTERVAL = Duration.ofMillis(10000L);
    private final CommonConnectorConfig connectorConfig;
    private final SnapshotProgressListener<P> snapshotProgressListener;

    public AbstractSnapshotChangeEventSource(CommonConnectorConfig connectorConfig, SnapshotProgressListener<P> snapshotProgressListener) {
        this.connectorConfig = connectorConfig;
        this.snapshotProgressListener = snapshotProgressListener;
    }

    public SnapshotResult<O> execute(ChangeEventSourceContext context, P partition, O previousOffset) throws InterruptedException {
        SnapshottingTask snapshottingTask = this.getSnapshottingTask(partition, previousOffset);
        if (snapshottingTask.shouldSkipSnapshot()) {
            LOGGER.debug("Skipping snapshotting");
            return SnapshotResult.skipped(previousOffset);
        } else {
            this.delaySnapshotIfNeeded(context);

            SnapshotContext ctx;
            try {
                ctx = this.prepare(partition);
            } catch (Exception var15) {
                LOGGER.error("Failed to initialize snapshot context.", var15);
                throw new RuntimeException(var15);
            }

            boolean completedSuccessfully = true;

            SnapshotResult var7;
            try {
                this.snapshotProgressListener.snapshotStarted(partition);
                var7 = this.doExecute(context, previousOffset, ctx, snapshottingTask);
            } catch (InterruptedException var13) {
                completedSuccessfully = false;
                LOGGER.warn("Snapshot was interrupted before completion");
                throw var13;
            } catch (Exception var14) {
                completedSuccessfully = false;
                throw new DebeziumException(var14);
            } finally {
                LOGGER.info("Snapshot - Final stage");
                this.close();
                if (completedSuccessfully) {
                    LOGGER.info("Snapshot completed");
                    this.completed(ctx);
                    this.snapshotProgressListener.snapshotCompleted(partition);
                } else {
                    LOGGER.warn("Snapshot was not completed successfully, it will be re-executed upon connector restart");
                    this.aborted(ctx);
                    this.snapshotProgressListener.snapshotAborted(partition);
                }

            }

            return var7;
        }
    }

    protected <T extends DataCollectionId> Stream<T> determineDataCollectionsToBeSnapshotted(Collection<T> allDataCollections) {
        Set<Pattern> snapshotAllowedDataCollections = this.connectorConfig.getDataCollectionsToBeSnapshotted();
        return snapshotAllowedDataCollections.size() == 0 ? allDataCollections.stream() : allDataCollections.stream().filter((dataCollectionId) -> {
            return snapshotAllowedDataCollections.stream().anyMatch((s) -> {
                return s.matcher(dataCollectionId.identifier()).matches();
            });
        });
    }

    protected void delaySnapshotIfNeeded(ChangeEventSourceContext context) throws InterruptedException {
        Duration snapshotDelay = this.connectorConfig.getSnapshotDelay();
        if (!snapshotDelay.isZero() && !snapshotDelay.isNegative()) {
            Threads.Timer timer = Threads.timer(Clock.SYSTEM, snapshotDelay);
            Metronome metronome = Metronome.parker(ConfigurationDefaults.RETURN_CONTROL_INTERVAL, Clock.SYSTEM);

            while (!timer.expired()) {
                if (!context.isRunning()) {
                    throw new InterruptedException("Interrupted while awaiting initial snapshot delay");
                }

                LOGGER.info("The connector will wait for {}s before proceeding", timer.remaining().getSeconds());
                metronome.pause();
            }

        }
    }

    protected abstract SnapshotResult<O> doExecute(ChangeEventSourceContext var1, O var2, SnapshotContext<P, O> var3, SnapshottingTask var4) throws Exception;

    protected abstract SnapshottingTask getSnapshottingTask(P var1, O var2);

    protected abstract SnapshotContext<P, O> prepare(P var1) throws Exception;

    public void close() {
    }

    protected void completed(SnapshotContext<P, O> snapshotContext) {
    }

    protected void aborted(SnapshotContext<P, O> snapshotContext) {
    }

    public static class SnapshottingTask {
        private final boolean snapshotSchema;
        private final boolean snapshotData;

        public SnapshottingTask(boolean snapshotSchema, boolean snapshotData) {
            this.snapshotSchema = snapshotSchema;
            this.snapshotData = snapshotData;
        }

        public boolean snapshotData() {
            return this.snapshotData;
        }

        public boolean snapshotSchema() {
            return this.snapshotSchema;
        }

        public boolean shouldSkipSnapshot() {
            return !this.snapshotSchema() && !this.snapshotData();
        }

        public String toString() {
            return "SnapshottingTask [snapshotSchema=" + this.snapshotSchema + ", snapshotData=" + this.snapshotData + "]";
        }
    }

    public static class SnapshotContext<P extends Partition, O extends OffsetContext> implements AutoCloseable {
        public P partition;
        public O offset;

        public SnapshotContext(P partition) {
            this.partition = partition;
        }

        public void close() throws Exception {
        }
    }
}
