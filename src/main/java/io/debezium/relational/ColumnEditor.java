package io.debezium.relational;

import io.debezium.annotation.NotThreadSafe;

import java.util.List;
import java.util.Optional;

@NotThreadSafe
public interface ColumnEditor {
    String name();

    int position();

    int jdbcType();

    int nativeType();

    String typeName();

    String typeExpression();

    String charsetName();

    String charsetNameOfTable();

    int length();

    Optional<Integer> scale();

    boolean isOptional();

    boolean isAutoIncremented();

    boolean isGenerated();

    Optional<String> defaultValueExpression();

    boolean hasDefaultValue();

    List<String> enumValues();

    String comment();

    ColumnEditor name(String var1);

    ColumnEditor type(String var1);

    ColumnEditor type(String var1, String var2);

    ColumnEditor jdbcType(int var1);

    ColumnEditor nativeType(int var1);

    ColumnEditor charsetName(String var1);

    ColumnEditor charsetNameOfTable(String var1);

    ColumnEditor length(int var1);

    ColumnEditor scale(Integer var1);

    ColumnEditor optional(boolean var1);

    ColumnEditor autoIncremented(boolean var1);

    ColumnEditor generated(boolean var1);

    ColumnEditor position(int var1);

    ColumnEditor defaultValueExpression(String var1);

    ColumnEditor enumValues(List<String> var1);

    ColumnEditor comment(String var1);

    ColumnEditor unsetDefaultValueExpression();

    Column create();
}
