package io.debezium.data.geometry;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Point extends Geometry {
    public static final String LOGICAL_NAME = "io.debezium.data.geometry.Point";
    public static final String X_FIELD = "x";
    public static final String Y_FIELD = "y";
    private static final int WKB_POINT = 1;
    private static final int WKB_POINT_SIZE = 21;

    public static SchemaBuilder builder() {
        return SchemaBuilder.struct().name("io.debezium.data.geometry.Point").version(1).doc("Geometry (POINT)").field("x", Schema.FLOAT64_SCHEMA).field("y", Schema.FLOAT64_SCHEMA).field("wkb", Schema.OPTIONAL_BYTES_SCHEMA).field("srid", Schema.OPTIONAL_INT32_SCHEMA);
    }

    private static byte[] buildWKBPoint(double x, double y) {
        ByteBuffer wkb = ByteBuffer.allocate(21);
        wkb.put((byte) 1);
        wkb.order(ByteOrder.LITTLE_ENDIAN);
        wkb.putInt(1);
        wkb.putDouble(x);
        wkb.putDouble(y);
        return wkb.array();
    }

    public static double[] parseWKBPoint(byte[] wkb) throws IllegalArgumentException {
        if (wkb.length != 21) {
            throw new IllegalArgumentException(String.format("Invalid WKB for Point (length %d < %d)", wkb.length, 21));
        } else {
            ByteBuffer reader = ByteBuffer.wrap(wkb);
            reader.order(reader.get() != 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            int geomType = reader.getInt();
            if (geomType != 1) {
                throw new IllegalArgumentException(String.format("Invalid WKB for 2D Point (wrong type %d)", geomType));
            } else {
                double x = reader.getDouble();
                double y = reader.getDouble();
                return new double[]{x, y};
            }
        }
    }

    public static Struct createValue(Schema geomSchema, double x, double y) {
        byte[] wkb = buildWKBPoint(x, y);
        Struct result = Geometry.createValue(geomSchema, wkb, (Integer) null);
        result.put("x", x);
        result.put("y", y);
        return result;
    }

    public static Struct createValue(Schema geomSchema, byte[] wkb, Integer srid) throws IllegalArgumentException {
        Struct result = Geometry.createValue(geomSchema, wkb, srid);
        double[] pt = parseWKBPoint(wkb);
        result.put("x", pt[0]);
        result.put("y", pt[1]);
        return result;
    }
}
