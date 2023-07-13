package io.debezium.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Maps {

    public static <K, V> Map<K, V> of(K k1, V v1) {
        K k0 = Objects.requireNonNull(k1);
        V v0 = Objects.requireNonNull(v1);
        HashMap<K, V> map = new HashMap<>();
        map.put(k0, v0);
        return map;
    }

    public static Map<String, Object> of() {
        return new HashMap<>();
    }
}
