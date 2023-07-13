package io.debezium.connector.mysql;

import io.debezium.schema.SchemaFactory;

public class MySQLSchemaFactory extends SchemaFactory {
    private static final MySQLSchemaFactory mysqlSchemaFactoryObject = new MySQLSchemaFactory();

    public static MySQLSchemaFactory get() {
        return mysqlSchemaFactoryObject;
    }
}
