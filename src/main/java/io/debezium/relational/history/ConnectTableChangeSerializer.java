package io.debezium.relational.history;

import io.debezium.relational.Column;
import io.debezium.relational.Table;
import io.debezium.schema.SchemaFactory;
import io.debezium.schema.SchemaNameAdjuster;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConnectTableChangeSerializer implements TableChanges.TableChangesSerializer<List<Struct>> {
    public static final String ID_KEY = "id";
    public static final String TYPE_KEY = "type";
    public static final String TABLE_KEY = "table";
    public static final String DEFAULT_CHARSET_NAME_KEY = "defaultCharsetName";
    public static final String PRIMARY_KEY_COLUMN_NAMES_KEY = "primaryKeyColumnNames";
    public static final String COLUMNS_KEY = "columns";
    public static final String NAME_KEY = "name";
    public static final String JDBC_TYPE_KEY = "jdbcType";
    public static final String NATIVE_TYPE_KEY = "nativeType";
    public static final String TYPE_NAME_KEY = "typeName";
    public static final String TYPE_EXPRESSION_KEY = "typeExpression";
    public static final String CHARSET_NAME_KEY = "charsetName";
    public static final String LENGTH_KEY = "length";
    public static final String SCALE_KEY = "scale";
    public static final String POSITION_KEY = "position";
    public static final String OPTIONAL_KEY = "optional";
    public static final String AUTO_INCREMENTED_KEY = "autoIncremented";
    public static final String GENERATED_KEY = "generated";
    public static final String COMMENT_KEY = "comment";
    public static final String DEFAULT_VALUE_EXPRESSION = "defaultValueExpression";
    public static final String ENUM_VALUES = "enumValues";
    private final Schema columnSchema;
    private final Schema tableSchema;
    private final Schema changeSchema;

    public ConnectTableChangeSerializer(SchemaNameAdjuster schemaNameAdjuster) {
        this.columnSchema = SchemaFactory.get().schemaHistoryColumnSchema(schemaNameAdjuster);
        this.tableSchema = SchemaFactory.get().schemaHistoryTableSchema(schemaNameAdjuster);
        this.changeSchema = SchemaFactory.get().schemaHistoryChangeSchema(schemaNameAdjuster);
    }

    public Schema getChangeSchema() {
        return this.changeSchema;
    }

    public List<Struct> serialize(TableChanges tableChanges) {
        return (List) StreamSupport.stream(tableChanges.spliterator(), false).map(this::toStruct).collect(Collectors.toList());
    }

    public Struct toStruct(TableChanges.TableChange tableChange) {
        Struct struct = new Struct(this.changeSchema);
        struct.put("type", tableChange.getType().name());
        struct.put("id", tableChange.getId().toDoubleQuotedString());
        struct.put("table", this.toStruct(tableChange.getTable()));
        return struct;
    }

    private Struct toStruct(Table table) {
        Struct struct = new Struct(this.tableSchema);
        struct.put("defaultCharsetName", table.defaultCharsetName());
        struct.put("primaryKeyColumnNames", table.primaryKeyColumnNames());
        List<Struct> columns = (List) table.columns().stream().map(this::toStruct).collect(Collectors.toList());
        struct.put("columns", columns);
        struct.put("comment", table.comment());
        return struct;
    }

    private Struct toStruct(Column column) {
        Struct struct = new Struct(this.columnSchema);
        struct.put("name", column.name());
        struct.put("jdbcType", column.jdbcType());
        if (column.nativeType() != -1) {
            struct.put("nativeType", column.nativeType());
        }

        struct.put("typeName", column.typeName());
        struct.put("typeExpression", column.typeExpression());
        struct.put("charsetName", column.charsetName());
        if (column.length() != -1) {
            struct.put("length", column.length());
        }

        column.scale().ifPresent((s) -> {
            struct.put("scale", s);
        });
        struct.put("position", column.position());
        struct.put("optional", column.isOptional());
        struct.put("autoIncremented", column.isAutoIncremented());
        struct.put("generated", column.isGenerated());
        struct.put("comment", column.comment());
        column.defaultValueExpression().ifPresent((d) -> {
            struct.put("defaultValueExpression", d);
        });
        if (column.enumValues() != null && !column.enumValues().isEmpty()) {
            struct.put("enumValues", column.enumValues());
        }

        return struct;
    }

    public TableChanges deserialize(List<Struct> data, boolean useCatalogBeforeSchema) {
        throw new UnsupportedOperationException("Deserialization from Connect Struct is not supported");
    }
}
