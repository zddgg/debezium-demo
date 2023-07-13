package io.debezium.connector.mysql;

public class Module {
    public static String version() {
        return "2.3.0.Final";
    }

    public static String name() {
        return "mysql";
    }

    public static String contextName() {
        return "MySQL";
    }
}
