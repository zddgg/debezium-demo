package io.debezium.relational;

import io.debezium.annotation.Immutable;
import io.debezium.data.Envelope;
import io.debezium.data.SchemaUtil;
import io.debezium.schema.DataCollectionSchema;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

@Immutable
public class TableSchema implements DataCollectionSchema {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableSchema.class);
    private final TableId id;
    private final Schema keySchema;
    private final Envelope envelopeSchema;
    private final Schema valueSchema;
    private final StructGenerator keyGenerator;
    private final StructGenerator valueGenerator;

    public TableSchema(TableId id, Schema keySchema, StructGenerator keyGenerator, Envelope envelopeSchema, Schema valueSchema, StructGenerator valueGenerator) {
        this.id = id;
        this.keySchema = keySchema;
        this.envelopeSchema = envelopeSchema;
        this.valueSchema = valueSchema;
        this.keyGenerator = keyGenerator != null ? keyGenerator : (row) -> {
            return null;
        };
        this.valueGenerator = valueGenerator != null ? valueGenerator : (row) -> {
            return null;
        };
    }

    public TableId id() {
        return this.id;
    }

    public Schema valueSchema() {
        return this.valueSchema;
    }

    public Schema keySchema() {
        return this.keySchema;
    }

    public Envelope getEnvelopeSchema() {
        return this.envelopeSchema;
    }

    public Struct keyFromColumnData(Object[] columnData) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("columnData from current stack: {}", Arrays.toString(columnData));
        }

        return columnData == null ? null : this.keyGenerator.generateValue(columnData);
    }

    public Struct valueFromColumnData(Object[] columnData) {
        return columnData == null ? null : this.valueGenerator.generateValue(columnData);
    }

    public int hashCode() {
        return this.valueSchema().hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof TableSchema)) {
            return false;
        } else {
            TableSchema that = (TableSchema) obj;
            return Objects.equals(this.keySchema(), that.keySchema()) && Objects.equals(this.valueSchema(), that.valueSchema());
        }
    }

    public String toString() {
        String var10000 = SchemaUtil.asString(this.keySchema());
        return "{ key : " + var10000 + ", value : " + SchemaUtil.asString(this.valueSchema()) + " }";
    }
}
