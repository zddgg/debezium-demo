package io.debezium.storage.redis;

public class RedisClientConnectionException extends RuntimeException {
    private static final long serialVersionUID = -4315965419500005492L;

    public RedisClientConnectionException(Throwable cause) {
        super(cause);
    }
}
