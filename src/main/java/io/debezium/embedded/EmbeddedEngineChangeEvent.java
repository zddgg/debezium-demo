package io.debezium.embedded;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.Header;
import io.debezium.engine.RecordChangeEvent;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;

class EmbeddedEngineChangeEvent<K, V, H> implements ChangeEvent<K, V>, RecordChangeEvent<V> {
    private final K key;
    private final V value;
    private final List<Header<H>> headers;
    private final SourceRecord sourceRecord;

    EmbeddedEngineChangeEvent(K key, V value, List<Header<H>> headers, SourceRecord sourceRecord) {
        this.key = key;
        this.value = value;
        this.headers = headers;
        this.sourceRecord = sourceRecord;
    }

    public K key() {
        return this.key;
    }

    public V value() {
        return this.value;
    }

    public List<Header<H>> headers() {
        return this.headers;
    }

    public V record() {
        return this.value;
    }

    public String destination() {
        return this.sourceRecord.topic();
    }

    public SourceRecord sourceRecord() {
        return this.sourceRecord;
    }

    public String toString() {
        return "EmbeddedEngineChangeEvent [key=" + this.key + ", value=" + this.value + ", sourceRecord=" + this.sourceRecord + "]";
    }
}
