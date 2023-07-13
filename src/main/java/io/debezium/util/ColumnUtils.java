package io.debezium.util;

import io.debezium.relational.Column;
import io.debezium.relational.Table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ColumnUtils {
    public static MappedColumns toMap(Table table) {
        Map<String, Column> sourceTableColumns = new HashMap();
        int greatestColumnPosition = 0;

        Column column;
        for (Iterator var3 = table.columns().iterator(); var3.hasNext(); greatestColumnPosition = greatestColumnPosition < column.position() ? column.position() : greatestColumnPosition) {
            column = (Column) var3.next();
            sourceTableColumns.put(column.name(), column);
        }

        return new MappedColumns(sourceTableColumns, greatestColumnPosition);
    }

    public static ColumnArray toArray(ResultSet resultSet, Table table) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Column[] columns = new Column[metaData.getColumnCount()];
        int greatestColumnPosition = 0;

        for (int i = 0; i < columns.length; ++i) {
            String columnName = metaData.getColumnName(i + 1);
            columns[i] = table.columnWithName(columnName);
            if (columns[i] == null) {
                String[] resultSetColumns = new String[metaData.getColumnCount()];

                for (int j = 0; j < metaData.getColumnCount(); ++j) {
                    resultSetColumns[j] = metaData.getColumnName(j + 1);
                }

                throw new IllegalArgumentException("Column '" + columnName + "' not found in result set '" + String.join(", ", resultSetColumns) + "' for table '" + table.id() + "', " + table + ". This might be caused by DBZ-4350");
            }

            greatestColumnPosition = greatestColumnPosition < columns[i].position() ? columns[i].position() : greatestColumnPosition;
        }

        return new ColumnArray(columns, greatestColumnPosition);
    }

    private ColumnUtils() {
    }

    public static class MappedColumns {
        private Map<String, Column> sourceTableColumns;
        private int greatestColumnPosition;

        public MappedColumns(Map<String, Column> sourceTableColumns, int greatestColumnPosition) {
            this.sourceTableColumns = sourceTableColumns;
            this.greatestColumnPosition = greatestColumnPosition;
        }

        public Map<String, Column> getSourceTableColumns() {
            return this.sourceTableColumns;
        }

        public int getGreatestColumnPosition() {
            return this.greatestColumnPosition;
        }
    }

    public static class ColumnArray {
        private Column[] columns;
        private int greatestColumnPosition;

        public ColumnArray(Column[] columns, int greatestColumnPosition) {
            this.columns = columns;
            this.greatestColumnPosition = greatestColumnPosition;
        }

        public Column[] getColumns() {
            return this.columns;
        }

        public int getGreatestColumnPosition() {
            return this.greatestColumnPosition;
        }
    }
}
