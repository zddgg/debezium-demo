package io.debezium.relational;

import java.util.List;
import java.util.Optional;

final class ColumnEditorImpl implements ColumnEditor {
    private String name;
    private int jdbcType = 4;
    private int nativeType = -1;
    private String typeName;
    private String typeExpression;
    private String charsetName;
    private String tableCharsetName;
    private int length = -1;
    private Integer scale;
    private int position = 1;
    private boolean optional = true;
    private boolean autoIncremented = false;
    private boolean generated = false;
    private String defaultValueExpression = null;
    private boolean hasDefaultValue = false;
    private List<String> enumValues;
    private String comment;

    protected ColumnEditorImpl() {
    }

    public String name() {
        return this.name;
    }

    public String typeName() {
        return this.typeName;
    }

    public String typeExpression() {
        return this.typeExpression;
    }

    public int jdbcType() {
        return this.jdbcType;
    }

    public int nativeType() {
        return this.nativeType;
    }

    public String charsetName() {
        return this.charsetName;
    }

    public String charsetNameOfTable() {
        return this.tableCharsetName;
    }

    public int length() {
        return this.length;
    }

    public Optional<Integer> scale() {
        return Optional.ofNullable(this.scale);
    }

    public int position() {
        return this.position;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean isAutoIncremented() {
        return this.autoIncremented;
    }

    public boolean isGenerated() {
        return this.generated;
    }

    public Optional<String> defaultValueExpression() {
        return Optional.ofNullable(this.defaultValueExpression);
    }

    public boolean hasDefaultValue() {
        return this.hasDefaultValue;
    }

    public String comment() {
        return this.comment;
    }

    public ColumnEditorImpl name(String name) {
        this.name = name;
        return this;
    }

    public List<String> enumValues() {
        return this.enumValues;
    }

    public ColumnEditorImpl type(String typeName) {
        this.typeName = typeName;
        this.typeExpression = typeName;
        return this;
    }

    public ColumnEditor type(String typeName, String typeExpression) {
        this.typeName = typeName;
        this.typeExpression = typeExpression != null ? typeExpression : typeName;
        return this;
    }

    public ColumnEditorImpl jdbcType(int jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    public ColumnEditorImpl nativeType(int nativeType) {
        this.nativeType = nativeType;
        return this;
    }

    public ColumnEditor charsetName(String charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    public ColumnEditor charsetNameOfTable(String charsetName) {
        this.tableCharsetName = charsetName;
        return this;
    }

    public ColumnEditorImpl length(int length) {
        assert length >= -1;

        this.length = length;
        return this;
    }

    public ColumnEditorImpl scale(Integer scale) {
        this.scale = scale;
        return this;
    }

    public ColumnEditorImpl optional(boolean optional) {
        this.optional = optional;
        if (optional && !this.hasDefaultValue()) {
            this.defaultValueExpression((String) null);
        }

        return this;
    }

    public ColumnEditorImpl autoIncremented(boolean autoIncremented) {
        this.autoIncremented = autoIncremented;
        return this;
    }

    public ColumnEditorImpl generated(boolean generated) {
        this.generated = generated;
        return this;
    }

    public ColumnEditorImpl position(int position) {
        this.position = position;
        return this;
    }

    public ColumnEditor defaultValueExpression(String defaultValueExpression) {
        this.hasDefaultValue = true;
        this.defaultValueExpression = defaultValueExpression;
        return this;
    }

    public ColumnEditor unsetDefaultValueExpression() {
        this.hasDefaultValue = false;
        this.defaultValueExpression = null;
        return this;
    }

    public ColumnEditor enumValues(List<String> enumValues) {
        this.enumValues = enumValues;
        return this;
    }

    public ColumnEditor comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Column create() {
        return new ColumnImpl(this.name, this.position, this.jdbcType, this.nativeType, this.typeName, this.typeExpression, this.charsetName, this.tableCharsetName, this.length, this.scale, this.enumValues, this.optional, this.autoIncremented, this.generated, this.defaultValueExpression, this.hasDefaultValue, this.comment);
    }

    public String toString() {
        return this.create().toString();
    }
}
