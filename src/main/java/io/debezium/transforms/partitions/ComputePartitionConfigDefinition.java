package io.debezium.transforms.partitions;

import io.debezium.config.Configuration;
import io.debezium.config.Field;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @deprecated
 */
@Deprecated
public class ComputePartitionConfigDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputePartitionConfigDefinition.class);
    public static final String MAPPING_SEPARATOR = ":";
    public static final String LIST_SEPARATOR = ",";
    public static final String FIELD_TABLE_FIELD_NAME_MAPPINGS_CONF = "partition.data-collections.field.mappings";
    public static final String FIELD_TABLE_PARTITION_NUM_MAPPINGS_CONF = "partition.data-collections.partition.num.mappings";
    static final Field PARTITION_TABLE_FIELD_NAME_MAPPINGS_FIELD;
    static final Field FIELD_TABLE_PARTITION_NUM_MAPPINGS_FIELD;

    private ComputePartitionConfigDefinition() {
    }

    static Map<String, String> parseMappings(List<String> mappings) {
        Map<String, String> m = new HashMap();
        Iterator var2 = mappings.iterator();

        while (var2.hasNext()) {
            String mapping = (String) var2.next();
            String[] parts = mapping.split(":");
            if (parts.length != 2) {
                throw new ComputePartitionException("Invalid mapping: " + mapping);
            }

            m.put(parts[0], parts[1]);
        }

        return m;
    }

    public static int isValidMapping(Configuration config, Field field, Field.ValidationOutput problems) {
        List<String> values = config.getStrings(field, ",");

        try {
            parseMappings(values);
            return 0;
        } catch (Exception var5) {
            LOGGER.error(String.format("Error while parsing values %s", values), var5);
            problems.accept(field, values, "Problem parsing list of colon-delimited pairs, e.g. <code>foo:bar,abc:xyz</code>");
            return 1;
        }
    }

    static Map<String, Integer> parseParititionMappings(List<String> mappings) {
        Map<String, Integer> m = new HashMap();
        Iterator var2 = mappings.iterator();

        while (var2.hasNext()) {
            String mapping = (String) var2.next();
            String[] parts = mapping.split(":");
            if (parts.length != 2) {
                throw new ComputePartitionException("Invalid mapping: " + mapping);
            }

            try {
                int value = Integer.parseInt(parts[1]);
                if (value <= 0) {
                    throw new ComputePartitionException(String.format("Unable to validate config. %s: partition number for '%s' must be positive", "partition.data-collections.partition.num.mappings", parts[0]));
                }

                m.put(parts[0], value);
            } catch (NumberFormatException var6) {
                throw new ComputePartitionException(String.format("Invalid mapping value: %s", parts[1]), var6);
            }
        }

        return m;
    }

    static {
        PARTITION_TABLE_FIELD_NAME_MAPPINGS_FIELD = Field.create("partition.data-collections.field.mappings").withDisplayName("Data collection field mapping").withType(Type.STRING).withValidation(ComputePartitionConfigDefinition::isValidMapping).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Comma-separated list of colon-delimited data collection field pairs, e.g. inventory.products:name,inventory.orders:purchaser");
        FIELD_TABLE_PARTITION_NUM_MAPPINGS_FIELD = Field.create("partition.data-collections.partition.num.mappings").withDisplayName("Data collection number of partition mapping").withType(Type.STRING).withValidation(ComputePartitionConfigDefinition::isValidMapping).withWidth(Width.MEDIUM).withImportance(Importance.MEDIUM).withDescription("Comma-separated list of colon-delimited data-collections partition number pairs, e.g. inventory.products:2,inventory.orders:3");
    }
}
