package io.debezium.connector.mysql;

import io.debezium.relational.Column;
import io.debezium.relational.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface MySqlFieldReader {
    Object readField(ResultSet var1, int var2, Column var3, Table var4) throws SQLException;
}
