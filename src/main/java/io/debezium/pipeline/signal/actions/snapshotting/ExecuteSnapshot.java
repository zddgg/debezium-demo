package io.debezium.pipeline.signal.actions.snapshotting;

import io.debezium.document.Array;
import io.debezium.document.Document;
import io.debezium.pipeline.EventDispatcher;
import io.debezium.pipeline.signal.SignalPayload;
import io.debezium.pipeline.signal.actions.AbstractSnapshotSignal;
import io.debezium.pipeline.spi.Partition;
import io.debezium.spi.schema.DataCollectionId;
import io.debezium.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExecuteSnapshot<P extends Partition> extends AbstractSnapshotSignal<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteSnapshot.class);
    public static final String NAME = "execute-snapshot";
    private final EventDispatcher<P, ? extends DataCollectionId> dispatcher;

    public ExecuteSnapshot(EventDispatcher<P, ? extends DataCollectionId> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public boolean arrived(SignalPayload<P> signalPayload) throws InterruptedException {
        List<String> dataCollections = getDataCollections(signalPayload.data);
        if (dataCollections == null) {
            return false;
        } else {
            SnapshotType type = getSnapshotType(signalPayload.data);
            Optional<String> additionalCondition = getAdditionalCondition(signalPayload.data);
            Optional<String> surrogateKey = getSurrogateKey(signalPayload.data);
            LOGGER.info("Requested '{}' snapshot of data collections '{}' with additional condition '{}' and surrogate key '{}'", new Object[]{type, dataCollections, additionalCondition.orElse("No condition passed"), surrogateKey.orElse("PK of table will be used")});
            switch (type) {
                case INCREMENTAL:
                    this.dispatcher.getIncrementalSnapshotChangeEventSource().addDataCollectionNamesToSnapshot(signalPayload, dataCollections, additionalCondition, surrogateKey);
                default:
                    return true;
            }
        }
    }

    public static List<String> getDataCollections(Document data) {
        Array dataCollectionsArray = data.getArray("data-collections");
        if (dataCollectionsArray != null && !dataCollectionsArray.isEmpty()) {
            return (List) dataCollectionsArray.streamValues().map((v) -> {
                return v.asString().trim();
            }).collect(Collectors.toList());
        } else {
            LOGGER.warn("Execute snapshot signal '{}' has arrived but the requested field '{}' is missing from data or is empty", data, "data-collections");
            return null;
        }
    }

    public static Optional<String> getAdditionalCondition(Document data) {
        String additionalCondition = data.getString("additional-condition");
        return Strings.isNullOrBlank(additionalCondition) ? Optional.empty() : Optional.of(additionalCondition);
    }

    public static Optional<String> getSurrogateKey(Document data) {
        String surrogateKey = data.getString("surrogate-key");
        return Strings.isNullOrBlank(surrogateKey) ? Optional.empty() : Optional.of(surrogateKey);
    }
}
