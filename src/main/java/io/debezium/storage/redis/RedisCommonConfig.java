package io.debezium.storage.redis;

import io.debezium.DebeziumException;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.util.Collect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RedisCommonConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCommonConfig.class);
    public static final String CONFIGURATION_FIELD_PREFIX_STRING = "redis.";
    private static final Field PROP_ADDRESS = Field.create("redis.address").withDescription("The url that will be used to access Redis").required();
    private static final Field PROP_USER = Field.create("redis.user").withDescription("The user that will be used to access Redis");
    private static final Field PROP_PASSWORD = Field.create("redis.password").withDescription("The password that will be used to access Redis");
    private static final boolean DEFAULT_SSL_ENABLED = false;
    private static final Field PROP_SSL_ENABLED = Field.create("redis.ssl.enabled").withDescription("Use SSL for Redis connection").withDefault(false);
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 2000;
    private static final Field PROP_CONNECTION_TIMEOUT;
    private static final Integer DEFAULT_SOCKET_TIMEOUT;
    private static final Field PROP_SOCKET_TIMEOUT;
    private static final Integer DEFAULT_RETRY_INITIAL_DELAY;
    private static final Field PROP_RETRY_INITIAL_DELAY;
    private static final Integer DEFAULT_RETRY_MAX_DELAY;
    private static final Field PROP_RETRY_MAX_DELAY;
    private static final boolean DEFAULT_WAIT_ENABLED = false;
    private static final Field PROP_WAIT_ENABLED;
    private static final long DEFAULT_WAIT_TIMEOUT = 1000L;
    private static final Field PROP_WAIT_TIMEOUT;
    private static final boolean DEFAULT_WAIT_RETRY_ENABLED = false;
    private static final Field PROP_WAIT_RETRY_ENABLED;
    private static final long DEFAULT_WAIT_RETRY_DELAY = 1000L;
    private static final Field PROP_WAIT_RETRY_DELAY;
    private String address;
    private String user;
    private String password;
    private boolean sslEnabled;
    private Integer initialRetryDelay;
    private Integer maxRetryDelay;
    private Integer connectionTimeout;
    private Integer socketTimeout;
    private boolean waitEnabled;
    private long waitTimeout;
    private boolean waitRetryEnabled;
    private long waitRetryDelay;

    public RedisCommonConfig(Configuration config, String prefix) {
        config = config.subset(prefix, true);
        LOGGER.info("Configuration for '{}' with prefix '{}': {}", new Object[]{this.getClass().getSimpleName(), prefix, config.asMap()});
        if (!config.validateAndRecord(this.getAllConfigurationFields(), (error) -> {
            LOGGER.error("Validation error for property with prefix '{}': {}", prefix, error);
        })) {
            throw new DebeziumException(String.format("Error configuring an instance of '%s' with prefix '%s'; check the logs for errors", this.getClass().getSimpleName(), prefix));
        } else {
            this.init(config);
        }
    }

    protected List<Field> getAllConfigurationFields() {
        return Collect.arrayListOf(PROP_ADDRESS, new Field[]{PROP_USER, PROP_PASSWORD, PROP_SSL_ENABLED, PROP_CONNECTION_TIMEOUT, PROP_SOCKET_TIMEOUT, PROP_RETRY_INITIAL_DELAY, PROP_RETRY_MAX_DELAY, PROP_WAIT_ENABLED, PROP_WAIT_TIMEOUT, PROP_WAIT_RETRY_ENABLED, PROP_WAIT_RETRY_DELAY});
    }

    protected void init(Configuration config) {
        this.address = config.getString(PROP_ADDRESS);
        this.user = config.getString(PROP_USER);
        this.password = config.getString(PROP_PASSWORD);
        this.sslEnabled = config.getBoolean(PROP_SSL_ENABLED);
        this.initialRetryDelay = config.getInteger(PROP_RETRY_INITIAL_DELAY);
        this.maxRetryDelay = config.getInteger(PROP_RETRY_MAX_DELAY);
        this.connectionTimeout = config.getInteger(PROP_CONNECTION_TIMEOUT);
        this.socketTimeout = config.getInteger(PROP_SOCKET_TIMEOUT);
        this.waitEnabled = config.getBoolean(PROP_WAIT_ENABLED);
        this.waitTimeout = config.getLong(PROP_WAIT_TIMEOUT);
        this.waitRetryEnabled = config.getBoolean(PROP_WAIT_RETRY_ENABLED);
        this.waitRetryDelay = config.getLong(PROP_WAIT_RETRY_DELAY);
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return this.address;
    }

    public String getUser() {
        return this.user;
    }

    public boolean isSslEnabled() {
        return this.sslEnabled;
    }

    public Integer getInitialRetryDelay() {
        return this.initialRetryDelay;
    }

    public Integer getMaxRetryDelay() {
        return this.maxRetryDelay;
    }

    public Integer getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public Integer getSocketTimeout() {
        return this.socketTimeout;
    }

    public boolean isWaitEnabled() {
        return this.waitEnabled;
    }

    public long getWaitTimeout() {
        return this.waitTimeout;
    }

    public boolean isWaitRetryEnabled() {
        return this.waitRetryEnabled;
    }

    public long getWaitRetryDelay() {
        return this.waitRetryDelay;
    }

    static {
        PROP_CONNECTION_TIMEOUT = Field.create("redis.connection.timeout.ms").withDescription("Connection timeout (in ms)").withDefault(DEFAULT_CONNECTION_TIMEOUT);
        DEFAULT_SOCKET_TIMEOUT = 2000;
        PROP_SOCKET_TIMEOUT = Field.create("redis.socket.timeout.ms").withDescription("Socket timeout (in ms)").withDefault(DEFAULT_SOCKET_TIMEOUT);
        DEFAULT_RETRY_INITIAL_DELAY = 300;
        PROP_RETRY_INITIAL_DELAY = Field.create("redis.retry.initial.delay.ms").withDescription("Initial retry delay (in ms)").withDefault(DEFAULT_RETRY_INITIAL_DELAY);
        DEFAULT_RETRY_MAX_DELAY = 10000;
        PROP_RETRY_MAX_DELAY = Field.create("redis.retry.max.delay.ms").withDescription("Maximum retry delay (in ms)").withDefault(DEFAULT_RETRY_MAX_DELAY);
        PROP_WAIT_ENABLED = Field.create("redis.wait.enabled").withDescription("Enables wait for replica. In case Redis is configured with a replica shard, this allows to verify that the data has been written to the replica.").withDefault(false);
        PROP_WAIT_TIMEOUT = Field.create("redis.wait.timeout.ms").withDescription("Timeout when wait for replica").withDefault(1000L);
        PROP_WAIT_RETRY_ENABLED = Field.create("redis.wait.retry.enabled").withDescription("Enables retry on wait for replica failure").withDefault(false);
        PROP_WAIT_RETRY_DELAY = Field.create("redis.wait.retry.delay.ms").withDescription("Delay of retry on wait for replica failure").withDefault(1000L);
    }
}
