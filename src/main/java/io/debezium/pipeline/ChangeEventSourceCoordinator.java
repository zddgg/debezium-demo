package io.debezium.pipeline;

import io.debezium.annotation.ThreadSafe;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.base.ChangeEventQueueMetrics;
import io.debezium.connector.common.CdcSourceTaskContext;
import io.debezium.pipeline.metrics.SnapshotChangeEventSourceMetrics;
import io.debezium.pipeline.metrics.StreamingChangeEventSourceMetrics;
import io.debezium.pipeline.metrics.spi.ChangeEventSourceMetricsFactory;
import io.debezium.pipeline.notification.Notification;
import io.debezium.pipeline.notification.NotificationService;
import io.debezium.pipeline.signal.SignalProcessor;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotChangeEventSource;
import io.debezium.pipeline.source.spi.*;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.pipeline.spi.SnapshotResult;
import io.debezium.schema.DatabaseSchema;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.LoggingContext;
import io.debezium.util.Threads;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@ThreadSafe
public class ChangeEventSourceCoordinator<P extends Partition, O extends OffsetContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeEventSourceCoordinator.class);
    public static final Duration SHUTDOWN_WAIT_TIMEOUT = Duration.ofSeconds(90L);
    protected final Offsets<P, O> previousOffsets;
    protected final ErrorHandler errorHandler;
    protected final ChangeEventSourceFactory<P, O> changeEventSourceFactory;
    protected final ChangeEventSourceMetricsFactory<P> changeEventSourceMetricsFactory;
    protected final ExecutorService executor;
    protected final EventDispatcher<P, ?> eventDispatcher;
    protected final DatabaseSchema<?> schema;
    protected final SignalProcessor<P, O> signalProcessor;
    protected final NotificationService<P, O> notificationService;
    private volatile boolean running;
    protected volatile StreamingChangeEventSource<P, O> streamingSource;
    protected final ReentrantLock commitOffsetLock = new ReentrantLock();
    protected SnapshotChangeEventSourceMetrics<P> snapshotMetrics;
    protected StreamingChangeEventSourceMetrics<P> streamingMetrics;

    public ChangeEventSourceCoordinator(Offsets<P, O> previousOffsets, ErrorHandler errorHandler, Class<? extends SourceConnector> connectorType, CommonConnectorConfig connectorConfig, ChangeEventSourceFactory<P, O> changeEventSourceFactory, ChangeEventSourceMetricsFactory<P> changeEventSourceMetricsFactory, EventDispatcher<P, ?> eventDispatcher, DatabaseSchema<?> schema, SignalProcessor<P, O> signalProcessor, NotificationService<P, O> notificationService) {
        this.previousOffsets = previousOffsets;
        this.errorHandler = errorHandler;
        this.changeEventSourceFactory = changeEventSourceFactory;
        this.changeEventSourceMetricsFactory = changeEventSourceMetricsFactory;
        this.executor = Threads.newSingleThreadExecutor(connectorType, connectorConfig.getLogicalName(), "change-event-source-coordinator");
        this.eventDispatcher = eventDispatcher;
        this.schema = schema;
        this.signalProcessor = signalProcessor;
        this.notificationService = notificationService;
    }

    public synchronized void start(CdcSourceTaskContext taskContext, ChangeEventQueueMetrics changeEventQueueMetrics, EventMetadataProvider metadataProvider) {
        AtomicReference<LoggingContext.PreviousContext> previousLogContext = new AtomicReference();

        try {
            this.snapshotMetrics = this.changeEventSourceMetricsFactory.getSnapshotMetrics(taskContext, changeEventQueueMetrics, metadataProvider);
            this.streamingMetrics = this.changeEventSourceMetricsFactory.getStreamingMetrics(taskContext, changeEventQueueMetrics, metadataProvider);
            this.running = true;
            this.executor.submit(() -> {
                try {
                    previousLogContext.set(taskContext.configureLoggingContext("snapshot"));
                    this.snapshotMetrics.register();
                    this.streamingMetrics.register();
                    LOGGER.info("Metrics registered");
                    ChangeEventSource.ChangeEventSourceContext context = new ChangeEventSourceContextImpl();
                    LOGGER.info("Context created");
                    SnapshotChangeEventSource<P, O> snapshotSource = this.changeEventSourceFactory.getSnapshotChangeEventSource(this.snapshotMetrics);
                    this.executeChangeEventSources(taskContext, snapshotSource, this.previousOffsets, previousLogContext, context);
                } catch (InterruptedException var9) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Change event source executor was interrupted", var9);
                } catch (Throwable var10) {
                    this.errorHandler.setProducerThrowable(var10);
                } finally {
                    this.streamingConnected(false);
                }

            });
            this.getSignalProcessor(this.previousOffsets).ifPresent(SignalProcessor::start);
        } finally {
            if (previousLogContext.get() != null) {
                ((LoggingContext.PreviousContext) previousLogContext.get()).restore();
            }

        }

    }

    public Optional<SignalProcessor<P, O>> getSignalProcessor(Offsets<P, O> previousOffset) {
        return previousOffset != null && previousOffset.getOffsets().size() != 1 ? Optional.empty() : Optional.ofNullable(this.signalProcessor);
    }

    protected void executeChangeEventSources(CdcSourceTaskContext taskContext, SnapshotChangeEventSource<P, O> snapshotSource, Offsets<P, O> previousOffsets, AtomicReference<LoggingContext.PreviousContext> previousLogContext, ChangeEventSource.ChangeEventSourceContext context) throws InterruptedException {
        P partition = previousOffsets.getTheOnlyPartition();
        O previousOffset = previousOffsets.getTheOnlyOffset();
        previousLogContext.set(taskContext.configureLoggingContext("snapshot", partition));
        SnapshotResult<O> snapshotResult = this.doSnapshot(snapshotSource, context, partition, previousOffset);

        try {
            this.notificationService.notify(Notification.Builder.builder().withId(UUID.randomUUID().toString()).withAggregateType("Initial Snapshot").withType("Status " + snapshotResult.getStatus()).build(), Offsets.of(previousOffsets.getTheOnlyPartition(), snapshotResult.getOffset()));
        } catch (UnsupportedOperationException var10) {
            LOGGER.warn("Initial Snapshot notification not currently supported for MongoDB");
        }

        this.getSignalProcessor(previousOffsets).ifPresent((s) -> {
            s.setContext(snapshotResult.getOffset());
        });
        LOGGER.debug("Snapshot result {}", snapshotResult);
        if (this.running && snapshotResult.isCompletedOrSkipped()) {
            previousLogContext.set(taskContext.configureLoggingContext("streaming", partition));
            this.streamEvents(context, partition, snapshotResult.getOffset());
        }

    }

    protected SnapshotResult<O> doSnapshot(SnapshotChangeEventSource<P, O> snapshotSource, ChangeEventSource.ChangeEventSourceContext context, P partition, O previousOffset) throws InterruptedException {
        CatchUpStreamingResult catchUpStreamingResult = this.executeCatchUpStreaming(context, snapshotSource, partition, previousOffset);
        if (catchUpStreamingResult.performedCatchUpStreaming) {
            this.streamingConnected(false);
            this.commitOffsetLock.lock();
            this.streamingSource = null;
            this.commitOffsetLock.unlock();
        }

        this.eventDispatcher.setEventListener(this.snapshotMetrics);
        SnapshotResult<O> snapshotResult = snapshotSource.execute(context, partition, previousOffset);
        LOGGER.info("Snapshot ended with {}", snapshotResult);
        if (snapshotResult.getStatus() == SnapshotResult.SnapshotResultStatus.COMPLETED || this.schema.tableInformationComplete()) {
            this.schema.assureNonEmptySchema();
        }

        return snapshotResult;
    }

    protected CatchUpStreamingResult executeCatchUpStreaming(ChangeEventSource.ChangeEventSourceContext context, SnapshotChangeEventSource<P, O> snapshotSource, P partition, O previousOffset) throws InterruptedException {
        return new CatchUpStreamingResult(false);
    }

    protected void streamEvents(ChangeEventSource.ChangeEventSourceContext context, P partition, O offsetContext) throws InterruptedException {
        this.initStreamEvents(partition, offsetContext);
        LOGGER.info("Starting streaming");
        this.streamingSource.execute(context, partition, offsetContext);
        LOGGER.info("Finished streaming");
    }

    protected void initStreamEvents(P partition, O offsetContext) throws InterruptedException {
        this.streamingSource = this.changeEventSourceFactory.getStreamingChangeEventSource();
        this.eventDispatcher.setEventListener(this.streamingMetrics);
        this.streamingConnected(true);
        this.streamingSource.init(offsetContext);
        this.getSignalProcessor(this.previousOffsets).ifPresent((s) -> {
            s.setContext(this.streamingSource.getOffsetContext());
        });
        Optional<IncrementalSnapshotChangeEventSource<P, ? extends DataCollectionId>> incrementalSnapshotChangeEventSource = this.changeEventSourceFactory.getIncrementalSnapshotChangeEventSource(offsetContext, this.snapshotMetrics, this.snapshotMetrics, this.notificationService);
        this.eventDispatcher.setIncrementalSnapshotChangeEventSource(incrementalSnapshotChangeEventSource);
        incrementalSnapshotChangeEventSource.ifPresent((x) -> {
            x.init(partition, offsetContext);
        });
    }

    public void commitOffset(Map<String, ?> partition, Map<String, ?> offset) {
        if (!this.commitOffsetLock.isLocked() && this.streamingSource != null && offset != null) {
            this.streamingSource.commitOffset(partition, offset);
        }

    }

    public synchronized void stop() throws InterruptedException {
        this.running = false;

        try {
            Thread.interrupted();
            this.executor.shutdown();
            boolean isShutdown = this.executor.awaitTermination(SHUTDOWN_WAIT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!isShutdown) {
                LOGGER.warn("Coordinator didn't stop in the expected time, shutting down executor now");
                Thread.interrupted();
                this.executor.shutdownNow();
                this.executor.awaitTermination(SHUTDOWN_WAIT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            }

            Optional<SignalProcessor<P, O>> processor = this.getSignalProcessor(this.previousOffsets);
            if (processor.isPresent()) {
                ((SignalProcessor) processor.get()).stop();
            }

            if (this.notificationService != null) {
                this.notificationService.stop();
            }

            this.eventDispatcher.close();
        } finally {
            this.snapshotMetrics.unregister();
            this.streamingMetrics.unregister();
        }

    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    protected void streamingConnected(boolean status) {
        if (this.changeEventSourceMetricsFactory.connectionMetricHandledByCoordinator()) {
            this.streamingMetrics.connected(status);
            LOGGER.info("Connected metrics set to '{}'", status);
        }

    }

    protected class CatchUpStreamingResult {
        public boolean performedCatchUpStreaming;

        public CatchUpStreamingResult(boolean performedCatchUpStreaming) {
            this.performedCatchUpStreaming = performedCatchUpStreaming;
        }
    }

    public class ChangeEventSourceContextImpl implements ChangeEventSource.ChangeEventSourceContext {
        public boolean isRunning() {
            return ChangeEventSourceCoordinator.this.running;
        }
    }
}
