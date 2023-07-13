package io.debezium.storage.redis.offset;

import io.debezium.config.Configuration;
import io.debezium.storage.redis.RedisClient;
import io.debezium.storage.redis.RedisClientConnectionException;
import io.debezium.storage.redis.RedisConnection;
import io.smallrye.mutiny.Uni;
import org.apache.kafka.connect.runtime.WorkerConfig;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RedisOffsetBackingStore extends MemoryOffsetBackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisOffsetBackingStore.class);
    private RedisOffsetBackingStoreConfig config;
    private RedisClient client;

    void connect() {
        RedisConnection redisConnection = new RedisConnection(this.config.getAddress(), this.config.getUser(), this.config.getPassword(), this.config.getConnectionTimeout(), this.config.getSocketTimeout(), this.config.isSslEnabled());
        this.client = redisConnection.getRedisClient("debezium:offsets", this.config.isWaitEnabled(), this.config.getWaitTimeout(), this.config.isWaitRetryEnabled(), this.config.getWaitRetryDelay());
    }

    public void configure(WorkerConfig config) {
        super.configure(config);
        Configuration configuration = Configuration.from(config.originalsStrings());
        this.config = new RedisOffsetBackingStoreConfig(configuration);
    }

    public synchronized void start() {
        super.start();
        LOGGER.info("Starting RedisOffsetBackingStore");
        this.connect();
        this.load();
    }

    public synchronized void stop() {
        super.stop();
        LOGGER.info("Stopped RedisOffsetBackingStore");
    }

    private void load() {
        Map<String, String> offsets = (Map) Uni.createFrom().item(() -> {
            return this.client.hgetAll(this.config.getRedisKeyName());
        }).onFailure().invoke((f) -> {
            LOGGER.warn("Reading from Redis offset store failed with " + f);
            LOGGER.warn("Will retry");
        }).onFailure(RedisClientConnectionException.class).invoke((f) -> {
            LOGGER.warn("Attempting to reconnect to Redis");
            this.connect();
        }).onFailure().retry().withBackOff(Duration.ofMillis((long) this.config.getInitialRetryDelay()), Duration.ofMillis((long) this.config.getMaxRetryDelay())).indefinitely().invoke((item) -> {
            LOGGER.trace("Offsets fetched from Redis: " + item);
        }).await().indefinitely();
        this.data = new HashMap();
        Iterator var2 = offsets.entrySet().iterator();

        while (var2.hasNext()) {
            Map.Entry<String, String> mapEntry = (Map.Entry) var2.next();
            ByteBuffer key = mapEntry.getKey() != null ? ByteBuffer.wrap(((String) mapEntry.getKey()).getBytes()) : null;
            ByteBuffer value = mapEntry.getValue() != null ? ByteBuffer.wrap(((String) mapEntry.getValue()).getBytes()) : null;
            this.data.put(key, value);
        }

    }

    protected void save() {
        Iterator var1 = this.data.entrySet().iterator();

        while (var1.hasNext()) {
            Map.Entry<ByteBuffer, ByteBuffer> mapEntry = (Map.Entry) var1.next();
            byte[] key = mapEntry.getKey() != null ? ((ByteBuffer) mapEntry.getKey()).array() : null;
            byte[] value = mapEntry.getValue() != null ? ((ByteBuffer) mapEntry.getValue()).array() : null;
            Uni.createFrom().item(() -> {
                return this.client.hset(this.config.getRedisKeyName().getBytes(), key, value);
            }).onFailure().invoke((f) -> {
                LOGGER.warn("Writing to Redis offset store failed with " + f);
                LOGGER.warn("Will retry");
            }).onFailure(RedisClientConnectionException.class).invoke((f) -> {
                LOGGER.warn("Attempting to reconnect to Redis");
                this.connect();
            }).onFailure().retry().withBackOff(Duration.ofSeconds(1L), Duration.ofSeconds(2L)).indefinitely().invoke((item) -> {
                LOGGER.trace("Offsets written to Redis: " + value);
            }).await().indefinitely();
        }

    }
}
