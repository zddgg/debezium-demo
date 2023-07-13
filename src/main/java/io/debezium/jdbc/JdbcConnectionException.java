package io.debezium.jdbc;

import java.sql.SQLException;

public final class JdbcConnectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String sqlState;
    private final int errorCode;

    public JdbcConnectionException(SQLException e) {
        this(e.getMessage(), e);
    }

    public JdbcConnectionException(String message, SQLException e) {
        super(message, e);
        this.sqlState = e.getSQLState();
        this.errorCode = e.getErrorCode();
    }

    public String getSqlState() {
        return this.sqlState;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
