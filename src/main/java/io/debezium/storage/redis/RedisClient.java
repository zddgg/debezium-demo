package io.debezium.storage.redis;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public interface RedisClient {
    void disconnect();

    void close();

    String xadd(String var1, Map<String, String> var2);

    List<String> xadd(List<AbstractMap.SimpleEntry<String, Map<String, String>>> var1);

    List<Map<String, String>> xrange(String var1);

    long xlen(String var1);

    Map<String, String> hgetAll(String var1);

    long hset(byte[] var1, byte[] var2, byte[] var3);

    long waitReplicas(int var1, long var2);

    String info(String var1);
}
