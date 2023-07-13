package io.debezium.connector.common;

import io.debezium.pipeline.spi.OffsetContext;
import io.debezium.pipeline.spi.Partition;
import org.apache.kafka.connect.storage.OffsetStorageReader;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OffsetReader<P extends Partition, O extends OffsetContext, L extends OffsetContext.Loader<O>> {
    private final OffsetStorageReader reader;
    private final L loader;

    public OffsetReader(OffsetStorageReader reader, L loader) {
        this.reader = reader;
        this.loader = loader;
    }

    public Map<P, O> offsets(Set<P> partitions) {
        Set<Map<String, String>> sourcePartitions = (Set) partitions.stream().map(Partition::getSourcePartition).collect(Collectors.toCollection(HashSet::new));
        Map<Map<String, String>, Map<String, Object>> sourceOffsets = this.reader.offsets(sourcePartitions);
        Map<P, O> offsets = new LinkedHashMap();
        partitions.forEach((partition) -> {
            Map<String, String> sourcePartition = partition.getSourcePartition();
            Map<String, Object> sourceOffset = (Map) sourceOffsets.get(sourcePartition);
            O offset = null;
            if (sourceOffset != null) {
                offset = this.loader.load(sourceOffset);
            }

            offsets.put(partition, offset);
        });
        return offsets;
    }
}
