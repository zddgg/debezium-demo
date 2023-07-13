package io.debezium.storage.redis.history;

import io.debezium.annotation.ThreadSafe;
import io.debezium.config.Configuration;
import io.debezium.document.DocumentReader;
import io.debezium.document.DocumentWriter;
import io.debezium.relational.history.*;
import io.debezium.storage.redis.RedisClient;
import io.debezium.storage.redis.RedisClientConnectionException;
import io.debezium.storage.redis.RedisConnection;
import io.debezium.util.DelayStrategy;
import io.debezium.util.Loggings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@ThreadSafe
public class RedisSchemaHistory extends AbstractSchemaHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSchemaHistory.class);
    private Duration initialRetryDelay;
    private Duration maxRetryDelay;
    private final DocumentWriter writer = DocumentWriter.defaultWriter();
    private final DocumentReader reader = DocumentReader.defaultReader();
    private final AtomicBoolean running = new AtomicBoolean();
    private RedisClient client;
    private RedisSchemaHistoryConfig config;

    void connect() {
        RedisConnection redisConnection = new RedisConnection(this.config.getAddress(), this.config.getUser(), this.config.getPassword(), this.config.getConnectionTimeout(), this.config.getSocketTimeout(), this.config.isSslEnabled());
        this.client = redisConnection.getRedisClient("debezium:schema_history", this.config.isWaitEnabled(), this.config.getWaitTimeout(), this.config.isWaitRetryEnabled(), this.config.getWaitRetryDelay());
    }

    public void configure(Configuration config, HistoryRecordComparator comparator, SchemaHistoryListener listener, boolean useCatalogBeforeSchema) {
        this.config = new RedisSchemaHistoryConfig(config);
        this.initialRetryDelay = Duration.ofMillis((long) this.config.getInitialRetryDelay());
        this.maxRetryDelay = Duration.ofMillis((long) this.config.getMaxRetryDelay());
        super.configure(config, comparator, listener, useCatalogBeforeSchema);
    }

    public synchronized void start() {
        super.start();
        LOGGER.info("Starting RedisSchemaHistory");
        this.connect();
    }

    protected void storeRecord(HistoryRecord record) throws SchemaHistoryException {
        if (record != null) {
            String line;
            try {
                line = this.writer.write(record.document());
            } catch (IOException var8) {
                Loggings.logErrorAndTraceRecord(LOGGER, record, "Failed to convert record to string", var8);
                throw new SchemaHistoryException("Unable to write database schema history record");
            }

            DelayStrategy delayStrategy = DelayStrategy.exponential(this.initialRetryDelay, this.maxRetryDelay);
            boolean completedSuccessfully = false;

            while (!completedSuccessfully) {
                try {
                    if (this.client == null) {
                        this.connect();
                    }

                    this.client.xadd(this.config.getRedisKeyName(), Collections.singletonMap("schema", line));
                    LOGGER.trace("Record written to database schema history in Redis: " + line);
                    completedSuccessfully = true;
                } catch (RedisClientConnectionException var6) {
                    this.reconnect();
                } catch (Exception var7) {
                    LOGGER.warn("Writing to database schema history stream failed", var7);
                    LOGGER.warn("Will retry");
                }

                if (!completedSuccessfully) {
                    delayStrategy.sleepWhen(!completedSuccessfully);
                }
            }

        }
    }

    private void reconnect() {
        LOGGER.warn("Attempting to reconnect to Redis");

        try {
            if (this.client != null) {
                this.client.disconnect();
            }
        } catch (Exception var2) {
            LOGGER.info("Exception while disconnecting", var2);
        }

        this.client = null;
    }

    public void stop() {
        this.running.set(false);
        if (this.client != null) {
            this.client.disconnect();
        }

        super.stop();
    }

    protected synchronized void recoverRecords(Consumer<HistoryRecord> records) {
        DelayStrategy delayStrategy = DelayStrategy.exponential(this.initialRetryDelay, this.maxRetryDelay);
        boolean completedSuccessfully = false;
        List<Map<String, String>> entries = new ArrayList();

        while (!completedSuccessfully) {
            try {
                if (this.client == null) {
                    this.connect();
                }

                entries = this.client.xrange(this.config.getRedisKeyName());
                completedSuccessfully = true;
            } catch (RedisClientConnectionException var9) {
                this.reconnect();
            } catch (Exception var10) {
                LOGGER.warn("Reading from database schema history stream failed with " + var10);
                LOGGER.warn("Will retry");
            }

            if (!completedSuccessfully) {
                delayStrategy.sleepWhen(!completedSuccessfully);
            }
        }

        Iterator var5 = ((List) entries).iterator();

        while (var5.hasNext()) {
            Map<String, String> item = (Map) var5.next();

            try {
                records.accept(new HistoryRecord(this.reader.read((String) item.get("schema"))));
            } catch (IOException var8) {
                LOGGER.error("Failed to convert record to string: {}", item, var8);
                return;
            }
        }

    }

    public boolean storageExists() {
        return true;
    }

    public boolean exists() {
        return this.client != null && this.client.xlen(this.config.getRedisKeyName()) > 0L;
    }
}
