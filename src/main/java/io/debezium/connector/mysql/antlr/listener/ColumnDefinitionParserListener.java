package io.debezium.connector.mysql.antlr.listener;

import io.debezium.antlr.AntlrDdlParser;
import io.debezium.antlr.DataTypeResolver;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.Column;
import io.debezium.relational.ColumnEditor;
import io.debezium.relational.TableEditor;
import io.debezium.relational.ddl.DataType;
import io.debezium.util.Strings;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColumnDefinitionParserListener extends MySqlParserBaseListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnDefinitionParserListener.class);
    private static final Pattern DOT = Pattern.compile("\\.");
    private final MySqlAntlrDdlParser parser;
    private final DataTypeResolver dataTypeResolver;
    private final TableEditor tableEditor;
    private ColumnEditor columnEditor;
    private boolean uniqueColumn;
    private AtomicReference<Boolean> optionalColumn = new AtomicReference();
    private DefaultValueParserListener defaultValueListener;
    private final List<ParseTreeListener> listeners;

    public ColumnDefinitionParserListener(TableEditor tableEditor, ColumnEditor columnEditor, MySqlAntlrDdlParser parser, List<ParseTreeListener> listeners) {
        this.tableEditor = tableEditor;
        this.columnEditor = columnEditor;
        this.parser = parser;
        this.dataTypeResolver = parser.dataTypeResolver();
        this.listeners = listeners;
    }

    public void setColumnEditor(ColumnEditor columnEditor) {
        this.columnEditor = columnEditor;
    }

    public ColumnEditor getColumnEditor() {
        return this.columnEditor;
    }

    public Column getColumn() {
        return this.columnEditor.create();
    }

    public void enterColumnDefinition(MySqlParser.ColumnDefinitionContext ctx) {
        this.uniqueColumn = false;
        this.optionalColumn = new AtomicReference();
        this.resolveColumnDataType(ctx.dataType());
        this.parser.runIfNotNull(() -> {
            this.defaultValueListener = new DefaultValueParserListener(this.columnEditor, this.optionalColumn);
            this.listeners.add(this.defaultValueListener);
        }, this.tableEditor);
        super.enterColumnDefinition(ctx);
    }

    public void exitColumnDefinition(MySqlParser.ColumnDefinitionContext ctx) {
        if (this.optionalColumn.get() != null) {
            this.columnEditor.optional((Boolean) this.optionalColumn.get());
        }

        if (this.uniqueColumn && !this.tableEditor.hasPrimaryKey()) {
            this.tableEditor.addColumn(this.columnEditor.create());
            this.tableEditor.setPrimaryKeyNames(new String[]{this.columnEditor.name()});
        }

        this.parser.runIfNotNull(() -> {
            this.defaultValueListener.exitDefaultValue(false);
            this.listeners.remove(this.defaultValueListener);
        }, this.tableEditor);
        super.exitColumnDefinition(ctx);
    }

    public void enterUniqueKeyColumnConstraint(MySqlParser.UniqueKeyColumnConstraintContext ctx) {
        this.uniqueColumn = true;
        super.enterUniqueKeyColumnConstraint(ctx);
    }

    public void enterPrimaryKeyColumnConstraint(MySqlParser.PrimaryKeyColumnConstraintContext ctx) {
        this.optionalColumn.set(Boolean.FALSE);
        this.tableEditor.addColumn(this.columnEditor.create());
        this.tableEditor.setPrimaryKeyNames(new String[]{this.columnEditor.name()});
        super.enterPrimaryKeyColumnConstraint(ctx);
    }

    public void enterCommentColumnConstraint(MySqlParser.CommentColumnConstraintContext ctx) {
        if (!this.parser.skipComments() && ctx.STRING_LITERAL() != null) {
            MySqlAntlrDdlParser var10001 = this.parser;
            this.columnEditor.comment(MySqlAntlrDdlParser.withoutQuotes(ctx.STRING_LITERAL().getText()));
        }

        super.enterCommentColumnConstraint(ctx);
    }

    public void enterNullNotnull(MySqlParser.NullNotnullContext ctx) {
        this.optionalColumn.set(ctx.NOT() == null);
        super.enterNullNotnull(ctx);
    }

    public void enterAutoIncrementColumnConstraint(MySqlParser.AutoIncrementColumnConstraintContext ctx) {
        this.columnEditor.autoIncremented(true);
        this.columnEditor.generated(true);
        super.enterAutoIncrementColumnConstraint(ctx);
    }

    public void enterSerialDefaultColumnConstraint(MySqlParser.SerialDefaultColumnConstraintContext ctx) {
        this.serialColumn();
        super.enterSerialDefaultColumnConstraint(ctx);
    }

    private void resolveColumnDataType(MySqlParser.DataTypeContext dataTypeContext) {
        String charsetName = null;
        DataType dataType = this.dataTypeResolver.resolveDataType(dataTypeContext);
        Integer length;
        int optionsSize;
        if (dataTypeContext instanceof MySqlParser.StringDataTypeContext) {
            MySqlParser.StringDataTypeContext stringDataTypeContext = (MySqlParser.StringDataTypeContext) dataTypeContext;
            if (stringDataTypeContext.lengthOneDimension() != null) {
                length = this.parseLength(stringDataTypeContext.lengthOneDimension().decimalLiteral().getText());
                this.columnEditor.length(length);
            }

            charsetName = this.parser.extractCharset(stringDataTypeContext.charsetName(), stringDataTypeContext.collationName());
        } else if (dataTypeContext instanceof MySqlParser.LongVarcharDataTypeContext) {
            MySqlParser.LongVarcharDataTypeContext longVarcharTypeContext = (MySqlParser.LongVarcharDataTypeContext) dataTypeContext;
            charsetName = this.parser.extractCharset(longVarcharTypeContext.charsetName(), longVarcharTypeContext.collationName());
        } else if (dataTypeContext instanceof MySqlParser.NationalStringDataTypeContext) {
            MySqlParser.NationalStringDataTypeContext nationalStringDataTypeContext = (MySqlParser.NationalStringDataTypeContext) dataTypeContext;
            if (nationalStringDataTypeContext.lengthOneDimension() != null) {
                length = this.parseLength(nationalStringDataTypeContext.lengthOneDimension().decimalLiteral().getText());
                this.columnEditor.length(length);
            }
        } else if (dataTypeContext instanceof MySqlParser.NationalVaryingStringDataTypeContext) {
            MySqlParser.NationalVaryingStringDataTypeContext nationalVaryingStringDataTypeContext = (MySqlParser.NationalVaryingStringDataTypeContext) dataTypeContext;
            if (nationalVaryingStringDataTypeContext.lengthOneDimension() != null) {
                length = this.parseLength(nationalVaryingStringDataTypeContext.lengthOneDimension().decimalLiteral().getText());
                this.columnEditor.length(length);
            }
        } else if (dataTypeContext instanceof MySqlParser.DimensionDataTypeContext) {
            MySqlParser.DimensionDataTypeContext dimensionDataTypeContext = (MySqlParser.DimensionDataTypeContext) dataTypeContext;
            length = null;
            Integer scale = null;
            if (dimensionDataTypeContext.lengthOneDimension() != null) {
                length = this.parseLength(dimensionDataTypeContext.lengthOneDimension().decimalLiteral().getText());
            }

            List decimalLiterals;
            if (dimensionDataTypeContext.lengthTwoDimension() != null) {
                decimalLiterals = dimensionDataTypeContext.lengthTwoDimension().decimalLiteral();
                length = this.parseLength(((MySqlParser.DecimalLiteralContext) decimalLiterals.get(0)).getText());
                scale = Integer.valueOf(((MySqlParser.DecimalLiteralContext) decimalLiterals.get(1)).getText());
            }

            if (dimensionDataTypeContext.lengthTwoOptionalDimension() != null) {
                decimalLiterals = dimensionDataTypeContext.lengthTwoOptionalDimension().decimalLiteral();
                if (((MySqlParser.DecimalLiteralContext) decimalLiterals.get(0)).REAL_LITERAL() != null) {
                    String[] digits = DOT.split(((MySqlParser.DecimalLiteralContext) decimalLiterals.get(0)).getText());
                    if (!Strings.isNullOrEmpty(digits[0]) && Integer.valueOf(digits[0]) != 0) {
                        length = this.parseLength(digits[0]);
                    } else {
                        length = 10;
                    }
                } else {
                    length = this.parseLength(((MySqlParser.DecimalLiteralContext) decimalLiterals.get(0)).getText());
                }

                if (decimalLiterals.size() > 1) {
                    scale = Integer.valueOf(((MySqlParser.DecimalLiteralContext) decimalLiterals.get(1)).getText());
                }
            }

            if (length != null) {
                this.columnEditor.length(length);
            }

            if (scale != null) {
                this.columnEditor.scale(scale);
            }
        } else if (dataTypeContext instanceof MySqlParser.CollectionDataTypeContext) {
            MySqlParser.CollectionDataTypeContext collectionDataTypeContext = (MySqlParser.CollectionDataTypeContext) dataTypeContext;
            if (collectionDataTypeContext.charsetName() != null) {
                charsetName = collectionDataTypeContext.charsetName().getText();
            }

            if (dataType.name().equalsIgnoreCase("SET")) {
                optionsSize = collectionDataTypeContext.collectionOptions().collectionOption().size();
                this.columnEditor.length(Math.max(0, optionsSize * 2 - 1));
            } else {
                this.columnEditor.length(1);
            }
        }

        String dataTypeName = dataType.name().toUpperCase();
        if (!dataTypeName.equals("ENUM") && !dataTypeName.equals("SET")) {
            if (dataTypeName.equals("SERIAL")) {
                this.columnEditor.type("BIGINT UNSIGNED");
                this.serialColumn();
            } else {
                this.columnEditor.type(dataTypeName);
            }
        } else {
            MySqlParser.CollectionDataTypeContext collectionDataTypeContext = (MySqlParser.CollectionDataTypeContext) dataTypeContext;
            List<String> collectionOptions = (List) collectionDataTypeContext.collectionOptions().collectionOption().stream().map(AntlrDdlParser::getText).collect(Collectors.toList());
            this.columnEditor.type(dataTypeName);
            this.columnEditor.enumValues(collectionOptions);
        }

        optionsSize = dataType.jdbcType();
        this.columnEditor.jdbcType(optionsSize);
        if (this.columnEditor.length() == -1) {
            this.columnEditor.length((int) dataType.length());
        }

        if (!this.columnEditor.scale().isPresent() && dataType.scale() != -1) {
            this.columnEditor.scale(dataType.scale());
        }

        if (-15 != optionsSize && -9 != optionsSize) {
            this.columnEditor.charsetName(charsetName);
        } else {
            this.columnEditor.charsetName("utf8");
            if (-15 == optionsSize && this.columnEditor.length() == -1) {
                this.columnEditor.length(1);
            }
        }

    }

    private Integer parseLength(String lengthStr) {
        Long length = Long.parseLong(lengthStr);
        if (length > 2147483647L) {
            LOGGER.warn("The length '{}' of the column `{}`.`{}` is too large to be supported, truncating it to '{}'", new Object[]{length, this.tableEditor.tableId(), this.columnEditor.name(), Integer.MAX_VALUE});
            length = 2147483647L;
        }

        return length.intValue();
    }

    private void serialColumn() {
        if (this.optionalColumn.get() == null) {
            this.optionalColumn.set(Boolean.FALSE);
        }

        this.uniqueColumn = true;
        this.columnEditor.autoIncremented(true);
        this.columnEditor.generated(true);
    }
}
