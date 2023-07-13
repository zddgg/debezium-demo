package io.debezium.spi.schema;

import java.util.List;

public interface DataCollectionId {
    String identifier();

    List<String> parts();

    List<String> databaseParts();

    List<String> schemaParts();
}
