package io.debezium.schema;

import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Offsets;
import io.debezium.pipeline.spi.Partition;
import io.debezium.relational.TableId;
import io.debezium.spi.schema.DataCollectionId;

import java.util.Collection;
import java.util.function.Predicate;

public interface HistorizedDatabaseSchema<I extends DataCollectionId> extends DatabaseSchema<I> {
    void applySchemaChange(SchemaChangeEvent var1);

    default void recover(Partition partition, OffsetContext offset) {
        this.recover(Offsets.of(partition, offset));
    }

    void recover(Offsets<?, ?> var1);

    void initializeStorage();

    Predicate<String> ddlFilter();

    boolean skipUnparseableDdlStatements();

    boolean storeOnlyCapturedTables();

    boolean storeOnlyCapturedDatabases();

    @FunctionalInterface
    public interface SchemaChangeEventConsumer {
        SchemaChangeEventConsumer NOOP = (x, y) -> {
        };

        void consume(SchemaChangeEvent var1, Collection<TableId> var2);
    }
}
