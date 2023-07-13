package io.debezium.relational;

public enum ColumnFilterMode {
    CATALOG {
        public TableId getTableIdForFilter(String catalog, String schema, String table) {
            return new TableId(catalog, (String) null, table);
        }
    },
    SCHEMA {
        public TableId getTableIdForFilter(String catalog, String schema, String table) {
            return new TableId((String) null, schema, table);
        }
    };

    public abstract TableId getTableIdForFilter(String var1, String var2, String var3);

    // $FF: synthetic method
    private static ColumnFilterMode[] $values() {
        return new ColumnFilterMode[]{CATALOG, SCHEMA};
    }
}
