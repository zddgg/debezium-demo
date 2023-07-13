package io.debezium.storage.redis;

import io.debezium.DebeziumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConnection.class);
    public static final String DEBEZIUM_OFFSETS_CLIENT_NAME = "debezium:offsets";
    public static final String DEBEZIUM_SCHEMA_HISTORY = "debezium:schema_history";
    private String address;
    private String user;
    private String password;
    private int connectionTimeout;
    private int socketTimeout;
    private boolean sslEnabled;

    public RedisConnection(String address, String user, String password, int connectionTimeout, int socketTimeout, boolean sslEnabled) {
        this.address = address;
        this.user = user;
        this.password = password;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.sslEnabled = sslEnabled;
    }

    public RedisClient getRedisClient(String clientName, boolean waitEnabled, long waitTimeout, boolean waitRetry, long waitRetryDelay) {
        if (waitEnabled && waitTimeout <= 0L) {
            throw new DebeziumException("Redis client wait timeout should be positive");
        } else {
            HostAndPort address = HostAndPort.from(this.address);

            Jedis client;
            try {
                client = new Jedis(address.getHost(), address.getPort(), this.connectionTimeout, this.socketTimeout, this.sslEnabled);
                if (this.user != null) {
                    client.auth(this.user, this.password);
                } else if (this.password != null) {
                    client.auth(this.password);
                } else {
                    client.ping();
                }

                try {
                    client.clientSetname(clientName);
                } catch (JedisDataException var12) {
                    LOGGER.warn("Failed to set client name", var12);
                }
            } catch (JedisConnectionException var13) {
                throw new RedisClientConnectionException(var13);
            }

            RedisClient jedisClient = new JedisClient(client);
            RedisClient redisClient = waitEnabled ? new WaitReplicasRedisClient(jedisClient, 1, waitTimeout, waitRetry, waitRetryDelay) : jedisClient;
            LOGGER.info("Using Redis client '{}'", redisClient);
            return (RedisClient) redisClient;
        }
    }
}
