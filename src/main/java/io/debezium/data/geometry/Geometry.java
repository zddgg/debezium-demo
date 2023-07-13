package io.debezium.data.geometry;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

public class Geometry {
    public static final String LOGICAL_NAME = "io.debezium.data.geometry.Geometry";
    public static final String WKB_FIELD = "wkb";
    public static final String SRID_FIELD = "srid";

    public static SchemaBuilder builder() {
        return SchemaBuilder.struct().name("io.debezium.data.geometry.Geometry").version(1).doc("Geometry").optional().field("wkb", Schema.BYTES_SCHEMA).field("srid", Schema.OPTIONAL_INT32_SCHEMA);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static Struct createValue(Schema geomSchema, byte[] wkb, Integer srid) {
        Struct result = new Struct(geomSchema);
        result.put("wkb", wkb);
        if (srid != null) {
            result.put("srid", srid);
        }

        return result;
    }
}
