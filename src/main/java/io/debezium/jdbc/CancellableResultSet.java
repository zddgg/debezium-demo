package io.debezium.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

public class CancellableResultSet implements ResultSet {
    private final ResultSet resultSet;

    private CancellableResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public static CancellableResultSet from(ResultSet resultSet) {
        return new CancellableResultSet(resultSet);
    }

    public boolean next() throws SQLException {
        return this.resultSet.next();
    }

    public void close() throws SQLException {
        try {
            if (!this.isClosed() && !this.isAfterLast()) {
                this.getStatement().cancel();
            }
        } catch (Exception var2) {
        }

        this.resultSet.close();
    }

    public boolean wasNull() throws SQLException {
        return this.resultSet.wasNull();
    }

    public String getString(int columnIndex) throws SQLException {
        return this.resultSet.getString(columnIndex);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return this.resultSet.getBoolean(columnIndex);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return this.resultSet.getByte(columnIndex);
    }

    public short getShort(int columnIndex) throws SQLException {
        return this.resultSet.getShort(columnIndex);
    }

    public int getInt(int columnIndex) throws SQLException {
        return this.resultSet.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return this.resultSet.getLong(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return this.resultSet.getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return this.resultSet.getDouble(columnIndex);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return this.resultSet.getBigDecimal(columnIndex, scale);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return this.resultSet.getBytes(columnIndex);
    }

    public Date getDate(int columnIndex) throws SQLException {
        return this.resultSet.getDate(columnIndex);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return this.resultSet.getTime(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return this.resultSet.getTimestamp(columnIndex);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return this.resultSet.getAsciiStream(columnIndex);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return this.resultSet.getUnicodeStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return this.resultSet.getBinaryStream(columnIndex);
    }

    public String getString(String columnLabel) throws SQLException {
        return this.resultSet.getString(columnLabel);
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        return this.resultSet.getBoolean(columnLabel);
    }

    public byte getByte(String columnLabel) throws SQLException {
        return this.resultSet.getByte(columnLabel);
    }

    public short getShort(String columnLabel) throws SQLException {
        return this.resultSet.getShort(columnLabel);
    }

    public int getInt(String columnLabel) throws SQLException {
        return this.resultSet.getInt(columnLabel);
    }

    public long getLong(String columnLabel) throws SQLException {
        return this.resultSet.getLong(columnLabel);
    }

    public float getFloat(String columnLabel) throws SQLException {
        return this.resultSet.getFloat(columnLabel);
    }

    public double getDouble(String columnLabel) throws SQLException {
        return this.resultSet.getDouble(columnLabel);
    }

    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return this.resultSet.getBigDecimal(columnLabel, scale);
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        return this.resultSet.getBytes(columnLabel);
    }

    public Date getDate(String columnLabel) throws SQLException {
        return this.resultSet.getDate(columnLabel);
    }

    public Time getTime(String columnLabel) throws SQLException {
        return this.resultSet.getTime(columnLabel);
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return this.resultSet.getTimestamp(columnLabel);
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return this.resultSet.getAsciiStream(columnLabel);
    }

    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return this.resultSet.getUnicodeStream(columnLabel);
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return this.resultSet.getBinaryStream(columnLabel);
    }

    public SQLWarning getWarnings() throws SQLException {
        return this.resultSet.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        this.resultSet.clearWarnings();
    }

    public String getCursorName() throws SQLException {
        return this.resultSet.getCursorName();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return this.resultSet.getMetaData();
    }

    public Object getObject(int columnIndex) throws SQLException {
        return this.resultSet.getObject(columnIndex);
    }

    public Object getObject(String columnLabel) throws SQLException {
        return this.resultSet.getObject(columnLabel);
    }

    public int findColumn(String columnLabel) throws SQLException {
        return this.resultSet.findColumn(columnLabel);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return this.resultSet.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return this.resultSet.getCharacterStream(columnLabel);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return this.resultSet.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return this.resultSet.getBigDecimal(columnLabel);
    }

    public boolean isBeforeFirst() throws SQLException {
        return this.resultSet.isBeforeFirst();
    }

    public boolean isAfterLast() throws SQLException {
        return this.resultSet.isAfterLast();
    }

    public boolean isFirst() throws SQLException {
        return this.resultSet.isFirst();
    }

    public boolean isLast() throws SQLException {
        return this.resultSet.isLast();
    }

    public void beforeFirst() throws SQLException {
        this.resultSet.beforeFirst();
    }

    public void afterLast() throws SQLException {
        this.resultSet.afterLast();
    }

    public boolean first() throws SQLException {
        return this.resultSet.first();
    }

    public boolean last() throws SQLException {
        return this.resultSet.last();
    }

    public int getRow() throws SQLException {
        return this.resultSet.getRow();
    }

    public boolean absolute(int row) throws SQLException {
        return this.resultSet.absolute(row);
    }

    public boolean relative(int rows) throws SQLException {
        return this.resultSet.relative(rows);
    }

    public boolean previous() throws SQLException {
        return this.resultSet.previous();
    }

    public void setFetchDirection(int direction) throws SQLException {
        this.resultSet.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return this.resultSet.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        this.resultSet.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return this.resultSet.getFetchSize();
    }

    public int getType() throws SQLException {
        return this.resultSet.getType();
    }

    public int getConcurrency() throws SQLException {
        return this.resultSet.getConcurrency();
    }

    public boolean rowUpdated() throws SQLException {
        return this.resultSet.rowUpdated();
    }

    public boolean rowInserted() throws SQLException {
        return this.resultSet.rowInserted();
    }

    public boolean rowDeleted() throws SQLException {
        return this.resultSet.rowDeleted();
    }

    public void updateNull(int columnIndex) throws SQLException {
        this.resultSet.updateNull(columnIndex);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        this.resultSet.updateBoolean(columnIndex, x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        this.resultSet.updateByte(columnIndex, x);
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        this.resultSet.updateShort(columnIndex, x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        this.resultSet.updateInt(columnIndex, x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        this.resultSet.updateLong(columnIndex, x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        this.resultSet.updateFloat(columnIndex, x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        this.resultSet.updateDouble(columnIndex, x);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        this.resultSet.updateBigDecimal(columnIndex, x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        this.resultSet.updateString(columnIndex, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        this.resultSet.updateBytes(columnIndex, x);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        this.resultSet.updateDate(columnIndex, x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        this.resultSet.updateTime(columnIndex, x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        this.resultSet.updateTimestamp(columnIndex, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.resultSet.updateAsciiStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        this.resultSet.updateBinaryStream(columnIndex, x, length);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        this.resultSet.updateCharacterStream(columnIndex, x, length);
    }

    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        this.resultSet.updateObject(columnIndex, x, scaleOrLength);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        this.resultSet.updateObject(columnIndex, x);
    }

    public void updateNull(String columnLabel) throws SQLException {
        this.resultSet.updateNull(columnLabel);
    }

    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        this.resultSet.updateBoolean(columnLabel, x);
    }

    public void updateByte(String columnLabel, byte x) throws SQLException {
        this.resultSet.updateByte(columnLabel, x);
    }

    public void updateShort(String columnLabel, short x) throws SQLException {
        this.resultSet.updateShort(columnLabel, x);
    }

    public void updateInt(String columnLabel, int x) throws SQLException {
        this.resultSet.updateInt(columnLabel, x);
    }

    public void updateLong(String columnLabel, long x) throws SQLException {
        this.resultSet.updateLong(columnLabel, x);
    }

    public void updateFloat(String columnLabel, float x) throws SQLException {
        this.resultSet.updateFloat(columnLabel, x);
    }

    public void updateDouble(String columnLabel, double x) throws SQLException {
        this.resultSet.updateDouble(columnLabel, x);
    }

    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        this.resultSet.updateBigDecimal(columnLabel, x);
    }

    public void updateString(String columnLabel, String x) throws SQLException {
        this.resultSet.updateString(columnLabel, x);
    }

    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        this.resultSet.updateBytes(columnLabel, x);
    }

    public void updateDate(String columnLabel, Date x) throws SQLException {
        this.resultSet.updateDate(columnLabel, x);
    }

    public void updateTime(String columnLabel, Time x) throws SQLException {
        this.resultSet.updateTime(columnLabel, x);
    }

    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        this.resultSet.updateTimestamp(columnLabel, x);
    }

    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.resultSet.updateAsciiStream(columnLabel, x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        this.resultSet.updateBinaryStream(columnLabel, x, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        this.resultSet.updateCharacterStream(columnLabel, reader, length);
    }

    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        this.resultSet.updateObject(columnLabel, x, scaleOrLength);
    }

    public void updateObject(String columnLabel, Object x) throws SQLException {
        this.resultSet.updateObject(columnLabel, x);
    }

    public void insertRow() throws SQLException {
        this.resultSet.insertRow();
    }

    public void updateRow() throws SQLException {
        this.resultSet.updateRow();
    }

    public void deleteRow() throws SQLException {
        this.resultSet.deleteRow();
    }

    public void refreshRow() throws SQLException {
        this.resultSet.refreshRow();
    }

    public void cancelRowUpdates() throws SQLException {
        this.resultSet.cancelRowUpdates();
    }

    public void moveToInsertRow() throws SQLException {
        this.resultSet.moveToInsertRow();
    }

    public void moveToCurrentRow() throws SQLException {
        this.resultSet.moveToCurrentRow();
    }

    public Statement getStatement() throws SQLException {
        return this.resultSet.getStatement();
    }

    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return this.resultSet.getObject(columnIndex, map);
    }

    public Ref getRef(int columnIndex) throws SQLException {
        return this.resultSet.getRef(columnIndex);
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return this.resultSet.getBlob(columnIndex);
    }

    public Clob getClob(int columnIndex) throws SQLException {
        return this.resultSet.getClob(columnIndex);
    }

    public Array getArray(int columnIndex) throws SQLException {
        return this.resultSet.getArray(columnIndex);
    }

    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return this.resultSet.getObject(columnLabel, map);
    }

    public Ref getRef(String columnLabel) throws SQLException {
        return this.resultSet.getRef(columnLabel);
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        return this.resultSet.getBlob(columnLabel);
    }

    public Clob getClob(String columnLabel) throws SQLException {
        return this.resultSet.getClob(columnLabel);
    }

    public Array getArray(String columnLabel) throws SQLException {
        return this.resultSet.getArray(columnLabel);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return this.resultSet.getDate(columnIndex, cal);
    }

    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return this.resultSet.getDate(columnLabel, cal);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return this.resultSet.getTime(columnIndex, cal);
    }

    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return this.resultSet.getTime(columnLabel, cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return this.resultSet.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return this.resultSet.getTimestamp(columnLabel, cal);
    }

    public URL getURL(int columnIndex) throws SQLException {
        return this.resultSet.getURL(columnIndex);
    }

    public URL getURL(String columnLabel) throws SQLException {
        return this.resultSet.getURL(columnLabel);
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        this.resultSet.updateRef(columnIndex, x);
    }

    public void updateRef(String columnLabel, Ref x) throws SQLException {
        this.resultSet.updateRef(columnLabel, x);
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        this.resultSet.updateBlob(columnIndex, x);
    }

    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        this.resultSet.updateBlob(columnLabel, x);
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        this.resultSet.updateClob(columnIndex, x);
    }

    public void updateClob(String columnLabel, Clob x) throws SQLException {
        this.resultSet.updateClob(columnLabel, x);
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        this.resultSet.updateArray(columnIndex, x);
    }

    public void updateArray(String columnLabel, Array x) throws SQLException {
        this.resultSet.updateArray(columnLabel, x);
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        return this.resultSet.getRowId(columnIndex);
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        return this.resultSet.getRowId(columnLabel);
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        this.resultSet.updateRowId(columnIndex, x);
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        this.resultSet.updateRowId(columnLabel, x);
    }

    public int getHoldability() throws SQLException {
        return this.resultSet.getHoldability();
    }

    public boolean isClosed() throws SQLException {
        return this.resultSet.isClosed();
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
        this.resultSet.updateNString(columnIndex, nString);
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
        this.resultSet.updateNString(columnLabel, nString);
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        this.resultSet.updateNClob(columnIndex, nClob);
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        this.resultSet.updateNClob(columnLabel, nClob);
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        return this.resultSet.getNClob(columnIndex);
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        return this.resultSet.getNClob(columnLabel);
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return this.resultSet.getSQLXML(columnIndex);
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return this.resultSet.getSQLXML(columnLabel);
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        this.resultSet.updateSQLXML(columnIndex, xmlObject);
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        this.resultSet.updateSQLXML(columnLabel, xmlObject);
    }

    public String getNString(int columnIndex) throws SQLException {
        return this.resultSet.getNString(columnIndex);
    }

    public String getNString(String columnLabel) throws SQLException {
        return this.resultSet.getNString(columnLabel);
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return this.resultSet.getNCharacterStream(columnIndex);
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return this.resultSet.getNCharacterStream(columnLabel);
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        this.resultSet.updateNCharacterStream(columnIndex, x, length);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.resultSet.updateNCharacterStream(columnLabel, reader, length);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        this.resultSet.updateAsciiStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        this.resultSet.updateBinaryStream(columnIndex, x, length);
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        this.resultSet.updateCharacterStream(columnIndex, x, length);
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.resultSet.updateAsciiStream(columnLabel, x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        this.resultSet.updateBinaryStream(columnLabel, x, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        this.resultSet.updateCharacterStream(columnLabel, reader, length);
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        this.resultSet.updateBlob(columnIndex, inputStream, length);
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        this.resultSet.updateBlob(columnLabel, inputStream, length);
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.resultSet.updateClob(columnIndex, reader, length);
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.resultSet.updateClob(columnLabel, reader, length);
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        this.resultSet.updateNClob(columnIndex, reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        this.resultSet.updateNClob(columnLabel, reader, length);
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.resultSet.updateNCharacterStream(columnIndex, x);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.resultSet.updateNCharacterStream(columnLabel, reader);
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        this.resultSet.updateAsciiStream(columnIndex, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        this.resultSet.updateBinaryStream(columnIndex, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        this.resultSet.updateCharacterStream(columnIndex, x);
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        this.resultSet.updateAsciiStream(columnLabel, x);
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        this.resultSet.updateBinaryStream(columnLabel, x);
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        this.resultSet.updateCharacterStream(columnLabel, reader);
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        this.resultSet.updateBlob(columnIndex, inputStream);
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        this.resultSet.updateBlob(columnLabel, inputStream);
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        this.resultSet.updateClob(columnIndex, reader);
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        this.resultSet.updateClob(columnLabel, reader);
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        this.resultSet.updateNClob(columnIndex, reader);
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        this.resultSet.updateNClob(columnLabel, reader);
    }

    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return this.resultSet.getObject(columnIndex, type);
    }

    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return this.resultSet.getObject(columnLabel, type);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        this.resultSet.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        this.resultSet.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }

    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        this.resultSet.updateObject(columnIndex, x, targetSqlType);
    }

    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        this.resultSet.updateObject(columnLabel, x, targetSqlType);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.resultSet.unwrap(iface);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.resultSet.isWrapperFor(iface);
    }
}
