package io.debezium.jdbc;

public class DefaultMainConnectionProvidingConnectionFactory<T extends JdbcConnection> implements MainConnectionProvidingConnectionFactory<T> {
    private ConnectionFactory<T> delegate;
    private T mainConnection;

    public DefaultMainConnectionProvidingConnectionFactory(ConnectionFactory<T> delegate) {
        this.delegate = delegate;
        this.mainConnection = delegate.newConnection();
    }

    public T mainConnection() {
        return this.mainConnection;
    }

    public T newConnection() {
        return this.delegate.newConnection();
    }
}
