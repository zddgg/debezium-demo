package io.debezium.storage.redis.offset;

import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.storage.redis.RedisCommonConfig;
import io.debezium.util.Collect;

import java.util.List;

public class RedisOffsetBackingStoreConfig extends RedisCommonConfig {
    private static final String PROP_PREFIX = "offset.storage.";
    private static final String DEFAULT_REDIS_KEY_NAME = "metadata:debezium:offsets";
    private static final Field PROP_KEY_NAME = Field.create("redis.key").withDescription("The Redis key that will be used to store the offsets").withDefault("metadata:debezium:offsets");
    private String redisKeyName;

    public RedisOffsetBackingStoreConfig(Configuration config) {
        super(config, "offset.storage.");
    }

    protected void init(Configuration config) {
        super.init(config);
        this.redisKeyName = config.getString(PROP_KEY_NAME);
    }

    protected List<Field> getAllConfigurationFields() {
        List<Field> fields = Collect.arrayListOf(PROP_KEY_NAME, new Field[0]);
        fields.addAll(super.getAllConfigurationFields());
        return fields;
    }

    public String getRedisKeyName() {
        return this.redisKeyName;
    }
}
