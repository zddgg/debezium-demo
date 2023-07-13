package io.debezium.relational.ddl;

import io.debezium.annotation.Immutable;

import java.util.Arrays;

@Immutable
public final class DataType {
    private final String expression;
    private final String name;
    private final int jdbcType;
    private final long length;
    private final int scale;
    private final int[] arrayDimensions;

    public static DataType userDefinedType(String qualifiedName) {
        return new DataType(qualifiedName, qualifiedName, 1111, -1L, -1, (int[]) null, 0);
    }

    protected DataType(String expr, String name, int jdbcType, long length, int scale, int[] arrayDimensions, int arrayDimLength) {
        this.expression = expr;
        this.name = name;
        this.jdbcType = jdbcType;
        this.length = length;
        this.scale = scale;
        if (arrayDimensions != null && arrayDimLength != 0) {
            this.arrayDimensions = Arrays.copyOf(arrayDimensions, arrayDimLength);
        } else {
            this.arrayDimensions = null;
        }

    }

    public String expression() {
        return this.expression;
    }

    public String name() {
        return this.name;
    }

    public int jdbcType() {
        return this.jdbcType;
    }

    public long length() {
        return this.length;
    }

    public int scale() {
        return this.scale;
    }

    public int[] arrayDimensions() {
        return this.arrayDimensions;
    }

    public String toString() {
        return this.expression;
    }
}
