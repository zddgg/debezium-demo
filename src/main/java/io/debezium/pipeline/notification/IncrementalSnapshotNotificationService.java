/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.pipeline.notification;

import io.debezium.connector.common.BaseSourceInfo;
import io.debezium.pipeline.source.snapshot.incremental.DataCollection;
import io.debezium.pipeline.source.snapshot.incremental.IncrementalSnapshotContext;
import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import org.apache.kafka.connect.errors.DataException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IncrementalSnapshotNotificationService<P extends Partition, O extends OffsetContext> {

    public static final String INCREMENTAL_SNAPSHOT = "Incremental Snapshot";
    public static final String DATA_COLLECTIONS = "data_collections";
    public static final String CURRENT_COLLECTION_IN_PROGRESS = "current_collection_in_progress";
    public static final String MAXIMUM_KEY = "maximum_key";
    public static final String LAST_PROCESSED_KEY = "last_processed_key";
    public static final String NONE = "<none>";
    public static final String CONNECTOR_NAME = "connector_name";
    public static final String TOTAL_ROWS_SCANNED = "total_rows_scanned";
    public static final String STATUS = "status";
    public static final String LIST_DELIMITER = ",";

    private final NotificationService<P, O> notificationService;

    public enum SnapshotStatus {
        STARTED,
        PAUSED,
        RESUMED,
        ABORTED,
        IN_PROGRESS,
        TABLE_SCAN_COMPLETED,
        COMPLETED
    }

    public enum TableScanCompletionStatus {
        EMPTY,
        NO_PRIMARY_KEY,
        SKIPPED,
        SQL_EXCEPTION,
        SUCCEEDED,
        UNKNOWN_SCHEMA
    }

    public IncrementalSnapshotNotificationService(NotificationService<P, O> notificationService) {
        this.notificationService = notificationService;
    }

    public <T extends DataCollectionId> void notifyStarted(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        String dataCollections = incrementalSnapshotContext.getDataCollections().stream().map(DataCollection::getId)
                .map(DataCollectionId::identifier)
                .collect(Collectors.joining(LIST_DELIMITER));

        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, dataCollections);

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.STARTED,
                map, offsetContext), Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyPaused(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        String dataCollections = incrementalSnapshotContext.getDataCollections().stream().map(DataCollection::getId)
                .map(DataCollectionId::identifier)
                .collect(Collectors.joining(LIST_DELIMITER));

        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, dataCollections);

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.PAUSED,
                        map, offsetContext),
                Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyResumed(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        String dataCollections = incrementalSnapshotContext.getDataCollections().stream().map(DataCollection::getId)
                .map(DataCollectionId::identifier)
                .collect(Collectors.joining(LIST_DELIMITER));

        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, dataCollections);

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.RESUMED,
                        map, offsetContext),
                Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyAborted(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.ABORTED,
                new HashMap<>(), offsetContext), Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyAborted(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext,
                                                           List<String> dataCollectionIds) {
        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, String.join(LIST_DELIMITER, dataCollectionIds));

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.ABORTED,
                        map, offsetContext),
                Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyTableScanCompleted(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext,
                                                                      long totalRowsScanned, TableScanCompletionStatus status) {

        String dataCollections = incrementalSnapshotContext.getDataCollections().stream().map(DataCollection::getId)
                .map(DataCollectionId::identifier)
                .collect(Collectors.joining(LIST_DELIMITER));

        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, dataCollections);
        map.put(TOTAL_ROWS_SCANNED, String.valueOf(totalRowsScanned));
        map.put(STATUS, status.name());

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.TABLE_SCAN_COMPLETED,
                        map,
                        offsetContext),
                Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyInProgress(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        String dataCollections = incrementalSnapshotContext.getDataCollections().stream().map(DataCollection::getId)
                .map(DataCollectionId::identifier)
                .collect(Collectors.joining(LIST_DELIMITER));

        Map<String, String> map = new HashMap<>();
        map.put(DATA_COLLECTIONS, dataCollections);
        map.put(CURRENT_COLLECTION_IN_PROGRESS, incrementalSnapshotContext.currentDataCollectionId().getId().identifier());
        map.put(MAXIMUM_KEY, incrementalSnapshotContext.maximumKey().orElse(new Object[0])[0].toString());
        map.put(LAST_PROCESSED_KEY, incrementalSnapshotContext.chunkEndPosititon()[0].toString());

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.IN_PROGRESS,
                        map,
                        offsetContext),
                Offsets.of(partition, offsetContext));
    }

    public <T extends DataCollectionId> void notifyCompleted(IncrementalSnapshotContext<T> incrementalSnapshotContext, P partition, OffsetContext offsetContext) {

        notificationService.notify(buildNotificationWith(incrementalSnapshotContext, SnapshotStatus.COMPLETED,
                        new HashMap<>(), offsetContext),
                Offsets.of(partition, offsetContext));
    }

    private <T extends DataCollectionId> Notification buildNotificationWith(IncrementalSnapshotContext<T> incrementalSnapshotContext, SnapshotStatus type,
                                                                            Map<String, String> additionalData, OffsetContext offsetContext) {

        Map<String, String> fullMap = new HashMap<>(additionalData);

        String connectorName;
        try {
            connectorName = offsetContext.getSourceInfo().getString(BaseSourceInfo.SERVER_NAME_KEY);
        } catch (DataException e) {
            connectorName = NONE;
        }
        fullMap.put(CONNECTOR_NAME, connectorName);

        String id = incrementalSnapshotContext.getCorrelationId() != null ? incrementalSnapshotContext.getCorrelationId() : UUID.randomUUID().toString();
        return Notification.Builder.builder()
                .withId(id)
                .withAggregateType(INCREMENTAL_SNAPSHOT)
                .withType(type.name())
                .withAdditionalData(fullMap)
                .build();
    }

}
