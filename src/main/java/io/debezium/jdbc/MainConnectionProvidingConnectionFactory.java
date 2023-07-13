package io.debezium.jdbc;

public interface MainConnectionProvidingConnectionFactory<T extends JdbcConnection> extends ConnectionFactory<T> {
    T mainConnection();
}
