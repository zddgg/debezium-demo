package io.debezium.util;

import io.debezium.annotation.NotThreadSafe;
import org.apache.kafka.common.cache.Cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@NotThreadSafe
public class LRUCacheMap<K, V> implements Cache<K, V> {
    private final LinkedHashMap<K, V> delegate;

    public LRUCacheMap(final int maxSize) {
        this.delegate = new LinkedHashMap<K, V>(16, 0.75F, true) {
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return this.size() > maxSize;
            }
        };
    }

    public V get(K key) {
        return this.delegate.get(key);
    }

    public void put(K key, V value) {
        this.delegate.put(key, value);
    }

    public boolean remove(K key) {
        return this.delegate.remove(key) != null;
    }

    public long size() {
        return (long) this.delegate.size();
    }

    public Set<K> keySet() {
        return this.delegate.keySet();
    }

    public Collection<V> values() {
        return this.delegate.values();
    }

    public String toString() {
        return this.delegate.toString();
    }
}
