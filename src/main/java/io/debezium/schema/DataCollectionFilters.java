package io.debezium.schema;

import io.debezium.spi.schema.DataCollectionId;

public interface DataCollectionFilters {
    DataCollectionFilter<?> dataCollectionFilter();

    @FunctionalInterface
    public interface DataCollectionFilter<T extends DataCollectionId> {
        boolean isIncluded(T var1);
    }
}
