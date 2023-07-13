package io.debezium.storage.redis;

import io.debezium.DebeziumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JedisClient implements RedisClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisClient.class);
    private final Jedis jedis;

    public JedisClient(Jedis jedis) {
        this.jedis = jedis;
    }

    public void disconnect() {
        this.tryErrors(() -> {
            this.jedis.disconnect();
        });
    }

    public void close() {
        this.tryErrors(() -> {
            this.jedis.close();
        });
    }

    public String xadd(String key, Map<String, String> hash) {
        return (String) this.tryErrors(() -> {
            return this.jedis.xadd(key, (StreamEntryID) null, hash).toString();
        });
    }

    public List<String> xadd(List<AbstractMap.SimpleEntry<String, Map<String, String>>> hashes) {
        return (List) this.tryErrors(() -> {
            try {
                this.jedis.ping();
                Pipeline pipeline = this.jedis.pipelined();
                hashes.forEach((hash) -> {
                    pipeline.xadd((String) hash.getKey(), StreamEntryID.NEW_ENTRY, (Map) hash.getValue());
                });
                return (List) pipeline.syncAndReturnAll().stream().map((response) -> {
                    return response.toString();
                }).collect(Collectors.toList());
            } catch (JedisDataException var3) {
                if (var3.getMessage().equals("LOADING Redis is loading the dataset in memory")) {
                    LOGGER.error("Redis is starting", var3);
                    return Collections.emptyList();
                } else {
                    LOGGER.error("Unexpected JedisDataException", var3);
                    throw new DebeziumException(var3);
                }
            }
        });
    }

    public List<Map<String, String>> xrange(String key) {
        return (List) this.tryErrors(() -> {
            return (List) this.jedis.xrange(key, (StreamEntryID) null, (StreamEntryID) null).stream().map((item) -> {
                return item.getFields();
            }).collect(Collectors.toList());
        });
    }

    public long xlen(String key) {
        return (Long) this.tryErrors(() -> {
            return this.jedis.xlen(key);
        });
    }

    public Map<String, String> hgetAll(String key) {
        return (Map) this.tryErrors(() -> {
            return this.jedis.hgetAll(key);
        });
    }

    public long hset(byte[] key, byte[] field, byte[] value) {
        return (Long) this.tryErrors(() -> {
            return this.jedis.hset(key, field, value);
        });
    }

    public long waitReplicas(int replicas, long timeout) {
        return (Long) this.tryErrors(() -> {
            return this.jedis.waitReplicas(replicas, timeout);
        });
    }

    public String toString() {
        return "JedisClient [jedis=" + this.jedis + "]";
    }

    private void tryErrors(Runnable runnable) {
        this.tryErrors(() -> {
            runnable.run();
            return null;
        });
    }

    private <R> R tryErrors(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (JedisConnectionException var3) {
            throw new RedisClientConnectionException(var3);
        }
    }

    public String info(String section) {
        return (String) this.tryErrors(() -> {
            return this.jedis.info(section);
        });
    }
}
