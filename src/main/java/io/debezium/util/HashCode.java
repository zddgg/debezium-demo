package io.debezium.util;

import io.debezium.annotation.Immutable;

import java.util.Arrays;

@Immutable
public class HashCode {
    private static final int PRIME = 103;

    public static int compute(Object... objects) {
        return computeHashCode(0, objects);
    }

    private static int computeHashCode(int seed, Object... objects) {
        if (objects != null && objects.length != 0) {
            int hc = seed;
            Object[] var3 = objects;
            int var4 = objects.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Object object = var3[var5];
                hc = 103 * hc;
                if (object instanceof byte[]) {
                    hc += Arrays.hashCode((byte[]) object);
                } else if (object instanceof boolean[]) {
                    hc += Arrays.hashCode((boolean[]) object);
                } else if (object instanceof short[]) {
                    hc += Arrays.hashCode((short[]) object);
                } else if (object instanceof int[]) {
                    hc += Arrays.hashCode((int[]) object);
                } else if (object instanceof long[]) {
                    hc += Arrays.hashCode((long[]) object);
                } else if (object instanceof float[]) {
                    hc += Arrays.hashCode((float[]) object);
                } else if (object instanceof double[]) {
                    hc += Arrays.hashCode((double[]) object);
                } else if (object instanceof char[]) {
                    hc += Arrays.hashCode((char[]) object);
                } else if (object instanceof Object[]) {
                    hc += Arrays.hashCode((Object[]) object);
                } else if (object != null) {
                    hc += object.hashCode();
                }
            }

            return hc;
        } else {
            return seed * 103;
        }
    }

    private HashCode() {
    }
}
