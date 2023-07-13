package io.debezium.connector.mysql;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MySqlGeometry {
    private static final int WKB_POINT_SIZE = 21;
    private static final byte[] WKB_EMPTY_GEOMETRYCOLLECTION = new byte[]{1, 7, 0, 0, 0, 0, 0, 0, 0};
    private final byte[] wkb;
    private final Integer srid;

    private MySqlGeometry(byte[] wkb, Integer srid) {
        this.wkb = wkb;
        this.srid = srid;
    }

    public static MySqlGeometry fromBytes(byte[] mysqlBytes) {
        ByteBuffer buf = ByteBuffer.wrap(mysqlBytes);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        Integer srid = buf.getInt();
        if (srid == 0) {
            srid = null;
        }

        byte[] wkb = new byte[buf.remaining()];
        buf.get(wkb);
        return new MySqlGeometry(wkb, srid);
    }

    public byte[] getWkb() {
        return this.wkb;
    }

    public Integer getSrid() {
        return this.srid;
    }

    public boolean isPoint() {
        return this.wkb.length == 21;
    }

    public static MySqlGeometry createEmpty() {
        return new MySqlGeometry(WKB_EMPTY_GEOMETRYCOLLECTION, (Integer) null);
    }
}
