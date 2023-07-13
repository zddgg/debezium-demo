package io.debezium.connector.mysql;

import io.debezium.relational.SystemVariables;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

public class MySqlSystemVariables extends SystemVariables {
    public static final String CHARSET_NAME_SERVER = "character_set_server";
    public static final String CHARSET_NAME_DATABASE = "character_set_database";
    public static final String CHARSET_NAME_CLIENT = "character_set_client";
    public static final String CHARSET_NAME_RESULT = "character_set_results";
    public static final String CHARSET_NAME_CONNECTION = "character_set_connection";
    public static final String LOWER_CASE_TABLE_NAMES = "lower_case_table_names";

    public MySqlSystemVariables() {
        super(Arrays.asList(MySqlScope.SESSION, MySqlScope.GLOBAL));
    }

    protected ConcurrentMap<String, String> forScope(Scope scope) {
        if (scope == MySqlScope.LOCAL) {
            scope = MySqlScope.SESSION;
        }

        return super.forScope((Scope) scope);
    }

    public static enum MySqlScope implements Scope {
        GLOBAL(2),
        SESSION(1),
        LOCAL(1);

        private int priority;

        private MySqlScope(int priority) {
            this.priority = priority;
        }

        public int priority() {
            return this.priority;
        }

        // $FF: synthetic method
        private static MySqlScope[] $values() {
            return new MySqlScope[]{GLOBAL, SESSION, LOCAL};
        }
    }
}
