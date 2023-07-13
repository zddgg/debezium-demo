package io.debezium.connector.mysql;

import io.debezium.relational.Column;
import io.debezium.relational.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

public class MySqlTextProtocolFieldReader extends AbstractMySqlFieldReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlTextProtocolFieldReader.class);

    public MySqlTextProtocolFieldReader(MySqlConnectorConfig config) {
        super(config);
    }

    protected Object readTimeField(ResultSet rs, int columnIndex) throws SQLException {
        Blob b = rs.getBlob(columnIndex);
        if (b == null) {
            return null;
        } else if (b.length() == 0L) {
            LOGGER.warn("Encountered a zero length blob for column index {}", columnIndex);
            return null;
        } else {
            try {
                return MySqlValueConverters.stringToDuration(new String(b.getBytes(1L, (int) b.length()), "UTF-8"));
            } catch (UnsupportedEncodingException var5) {
                this.logInvalidValue(rs, columnIndex, b);
                this.logger.error("Could not read MySQL TIME value as UTF-8. Enable TRACE logging to log the problematic column and its value.");
                throw new RuntimeException(var5);
            }
        }
    }

    protected Object readDateField(ResultSet rs, int columnIndex, Column column, Table table) throws SQLException {
        Blob b = rs.getBlob(columnIndex);
        if (b == null) {
            return null;
        } else {
            try {
                return MySqlValueConverters.stringToLocalDate(new String(b.getBytes(1L, (int) b.length()), "UTF-8"), column, table);
            } catch (UnsupportedEncodingException var7) {
                this.logInvalidValue(rs, columnIndex, b);
                this.logger.error("Could not read MySQL DATE value as UTF-8. Enable TRACE logging to log the problematic column and its value.");
                throw new RuntimeException(var7);
            }
        }
    }

    protected Object readTimestampField(ResultSet rs, int columnIndex, Column column, Table table) throws SQLException {
        Blob b = rs.getBlob(columnIndex);
        if (b == null) {
            return null;
        } else if (b.length() == 0L) {
            LOGGER.warn("Encountered a zero length blob for column index {}", columnIndex);
            return null;
        } else {
            try {
                return MySqlValueConverters.containsZeroValuesInDatePart(new String(b.getBytes(1L, (int) b.length()), "UTF-8"), column, table) ? null : rs.getTimestamp(columnIndex, Calendar.getInstance());
            } catch (UnsupportedEncodingException var7) {
                this.logInvalidValue(rs, columnIndex, b);
                this.logger.error("Could not read MySQL DATETIME value as UTF-8. Enable TRACE logging to log the problematic column and its value.");
                throw new RuntimeException(var7);
            }
        }
    }
}
