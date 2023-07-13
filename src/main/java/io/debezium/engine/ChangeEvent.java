package io.debezium.engine;

import java.util.Collections;
import java.util.List;

public interface ChangeEvent<K, V> {
    K key();

    V value();

    default <H> List<Header<H>> headers() {
        return Collections.emptyList();
    }

    String destination();
}
