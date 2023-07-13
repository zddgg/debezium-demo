package io.debezium.jdbc;

@FunctionalInterface
public interface ConnectionFactory<T extends JdbcConnection> {
    T newConnection();
}
