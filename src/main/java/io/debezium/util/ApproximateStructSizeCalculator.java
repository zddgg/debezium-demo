package io.debezium.util;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ApproximateStructSizeCalculator {
    private static final int EMPTY_STRUCT_SIZE = 56;
    private static final int EMPTY_STRING_SIZE = 56;
    private static final int EMPTY_BYTES_SIZE = 24;
    private static final int EMPTY_ARRAY_SIZE = 64;
    private static final int EMPTY_MAP_SIZE = 88;
    private static final int EMPTY_PRIMITIVE = 24;
    private static final int REFERENCE_SIZE = 8;

    public static long getApproximateRecordSize(SourceRecord changeEvent) {
        long value = (long) changeEvent.sourcePartition().size() * 100L + (long) changeEvent.sourceOffset().size() * 100L + (long) changeEvent.headers().size() * 100L;
        value += 8L;
        return value + getStructSize((Struct) changeEvent.key()) + getStructSize((Struct) changeEvent.value()) + (long) changeEvent.topic().getBytes().length;
    }

    private static long getStructSize(Struct struct) {
        if (struct == null) {
            return 0L;
        } else {
            long size = 56L;
            Schema schema = struct.schema();

            Field field;
            for (Iterator var4 = schema.fields().iterator(); var4.hasNext(); size += getValueSize(field.schema(), struct.getWithoutDefault(field.name()))) {
                field = (Field) var4.next();
                size += 8L;
            }

            return size;
        }
    }

    private static long getValueSize(Schema schema, Object value) {
        switch (schema.type()) {
            case BOOLEAN:
            case INT8:
            case INT16:
            case FLOAT32:
            case INT32:
            case FLOAT64:
            case INT64:
                return 24L;
            case STRING:
                String s = (String) value;
                return s == null ? 0L : (long) (56 + s.getBytes().length);
            case BYTES:
                byte[] b;
                if (value instanceof BigDecimal) {
                    b = ((BigDecimal) value).unscaledValue().toByteArray();
                } else if (value instanceof ByteBuffer) {
                    ByteBuffer buffer = (ByteBuffer) value;
                    b = toArray(buffer, 0, buffer.remaining());
                } else {
                    b = (byte[]) value;
                }

                return b == null ? 0L : (long) (24 + b.length);
            case STRUCT:
                return getStructSize((Struct) value);
            case ARRAY:
                return getArraySize(schema.valueSchema(), (List) value);
            case MAP:
                return getMapSize(schema.keySchema(), schema.valueSchema(), (Map) value);
            default:
                return 0L;
        }
    }

    private static long getArraySize(Schema elementSchema, List<Object> array) {
        if (array == null) {
            return 0L;
        } else {
            long size = 64L;

            Object element;
            for (Iterator var4 = array.iterator(); var4.hasNext(); size += getValueSize(elementSchema, element)) {
                element = var4.next();
                size += 8L;
            }

            return size;
        }
    }

    private static long getMapSize(Schema keySchema, Schema valueSchema, Map<Object, Object> map) {
        if (map == null) {
            return 0L;
        } else {
            long size = 88L;

            Map.Entry entry;
            for (Iterator var5 = map.entrySet().iterator(); var5.hasNext(); size += getValueSize(valueSchema, entry.getValue())) {
                entry = (Map.Entry) var5.next();
                size += 16L;
                size += getValueSize(keySchema, entry.getKey());
            }

            return size;
        }
    }

    private static byte[] toArray(ByteBuffer buffer, int offset, int size) {
        byte[] dest = new byte[size];
        if (buffer.hasArray()) {
            System.arraycopy(buffer.array(), buffer.position() + buffer.arrayOffset() + offset, dest, 0, size);
        } else {
            int pos = buffer.position();
            buffer.position(pos + offset);
            buffer.get(dest);
            buffer.position(pos);
        }

        return dest;
    }
}
