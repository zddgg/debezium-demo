package io.debezium.connector.mysql;

import io.debezium.relational.Column;
import io.debezium.relational.Table;
import io.debezium.util.Collect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public abstract class AbstractMySqlFieldReader implements MySqlFieldReader {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Set<String> TEXT_DATATYPES = Collect.unmodifiableSet(new String[]{"CHAR", "VARCHAR", "TEXT"});
    private final MySqlConnectorConfig connectorConfig;

    protected AbstractMySqlFieldReader(MySqlConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public Object readField(ResultSet rs, int columnIndex, Column column, Table table) throws SQLException {
        if (column.jdbcType() == 92) {
            return this.readTimeField(rs, columnIndex);
        } else if (column.jdbcType() == 91) {
            try {
                return this.readDateField(rs, columnIndex, column, table);
            } catch (RuntimeException var7) {
                this.logger.warn("Failed to read date value: '{}'. Trying default ResultSet implementation.", var7.getMessage());
                return rs.getObject(columnIndex);
            }
        } else if (column.jdbcType() == 93) {
            try {
                return this.readTimestampField(rs, columnIndex, column, table);
            } catch (RuntimeException var8) {
                this.logger.warn("Failed to read timestamp value: '{}'. Trying default ResultSet implementation.", var8.getMessage());
                return rs.getObject(columnIndex);
            }
        } else if (column.jdbcType() != -6 && column.jdbcType() != 5) {
            boolean hasConverterForColumn = this.connectorConfig.customConverterRegistry().getValueConverter(table.id(), column).isPresent();
            if (hasConverterForColumn && TEXT_DATATYPES.contains(column.typeName())) {
                try {
                    String columnData = rs.getString(columnIndex);
                    if (columnData != null) {
                        return columnData.getBytes(MySqlConnection.getJavaEncodingForMysqlCharSet(column.charsetName()));
                    }
                } catch (UnsupportedEncodingException var9) {
                    this.logger.warn("Unsupported encoding '{}' for column '{}', sending value as String", var9.getMessage(), column.name());
                }
            }

            return rs.getObject(columnIndex);
        } else {
            return rs.getObject(columnIndex) == null ? null : rs.getInt(columnIndex);
        }
    }

    protected abstract Object readTimeField(ResultSet var1, int var2) throws SQLException;

    protected abstract Object readDateField(ResultSet var1, int var2, Column var3, Table var4) throws SQLException;

    protected abstract Object readTimestampField(ResultSet var1, int var2, Column var3, Table var4) throws SQLException;

    protected void logInvalidValue(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        String columnName = resultSet.getMetaData().getColumnName(columnIndex);
        this.logger.trace("Column '" + columnName + "', detected an invalid value of '" + value + "'");
    }
}
