package io.debezium.relational;

import io.debezium.annotation.Immutable;

import java.util.List;
import java.util.Optional;

@Immutable
public interface Column extends Comparable<Column> {
    int UNSET_INT_VALUE = -1;

    static ColumnEditor editor() {
        return new ColumnEditorImpl();
    }

    String name();

    int position();

    int jdbcType();

    int nativeType();

    String typeName();

    String typeExpression();

    String charsetName();

    int length();

    Optional<Integer> scale();

    boolean isOptional();

    default boolean isRequired() {
        return !this.isOptional();
    }

    boolean isAutoIncremented();

    boolean isGenerated();

    Optional<String> defaultValueExpression();

    boolean hasDefaultValue();

    List<String> enumValues();

    String comment();

    default int compareTo(Column that) {
        return this == that ? 0 : this.position() - that.position();
    }

    ColumnEditor edit();

    default boolean typeUsesCharset() {
        switch (this.jdbcType()) {
            case -16:
            case -15:
            case -9:
            case -1:
            case 1:
            case 12:
            case 70:
            case 2005:
            case 2009:
            case 2011:
                return true;
            default:
                return false;
        }
    }
}
