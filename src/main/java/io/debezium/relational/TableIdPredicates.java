package io.debezium.relational;

public interface TableIdPredicates {
    default boolean isQuotingChar(char c) {
        return c == '"' || c == '\'' || c == '`';
    }

    default boolean isStartDelimiter(char c) {
        return false;
    }

    default boolean isEndDelimiter(char c) {
        return false;
    }
}
