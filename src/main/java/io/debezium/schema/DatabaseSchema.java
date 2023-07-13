package io.debezium.schema;

import io.debezium.spi.schema.DataCollectionId;

public interface DatabaseSchema<I extends DataCollectionId> extends AutoCloseable {
    String NO_CAPTURED_DATA_COLLECTIONS_WARNING = "After applying the include/exclude list filters, no changes will be captured. Please check your configuration!";

    DataCollectionSchema schemaFor(I var1);

    boolean tableInformationComplete();

    default void assureNonEmptySchema() {
    }

    boolean isHistorized();
}
