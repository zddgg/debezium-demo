package io.debezium.connector.mysql.antlr.listener;

import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.ColumnEditor;

import java.util.concurrent.atomic.AtomicReference;

public class DefaultValueParserListener extends MySqlParserBaseListener {
    private final ColumnEditor columnEditor;
    private final AtomicReference<Boolean> optionalColumn;
    private boolean converted;

    public DefaultValueParserListener(ColumnEditor columnEditor, AtomicReference<Boolean> optionalColumn) {
        this.columnEditor = columnEditor;
        this.optionalColumn = optionalColumn;
        this.converted = false;
    }

    public void enterDefaultValue(MySqlParser.DefaultValueContext ctx) {
        String sign = "";
        if (ctx.NULL_LITERAL() == null) {
            if (ctx.unaryOperator() != null) {
                sign = ctx.unaryOperator().getText();
            }

            if (ctx.constant() != null) {
                if (ctx.constant().stringLiteral() != null) {
                    if (ctx.constant().stringLiteral().COLLATE() == null) {
                        this.columnEditor.defaultValueExpression(sign + this.unquote(ctx.constant().stringLiteral().getText()));
                    } else {
                        this.columnEditor.defaultValueExpression(sign + this.unquote(ctx.constant().stringLiteral().STRING_LITERAL(0).getText()));
                    }
                } else if (ctx.constant().decimalLiteral() != null) {
                    this.columnEditor.defaultValueExpression(sign + ctx.constant().decimalLiteral().getText());
                } else if (ctx.constant().BIT_STRING() != null) {
                    this.columnEditor.defaultValueExpression(this.unquoteBinary(ctx.constant().BIT_STRING().getText()));
                } else if (ctx.constant().booleanLiteral() != null) {
                    this.columnEditor.defaultValueExpression(ctx.constant().booleanLiteral().getText());
                } else if (ctx.constant().REAL_LITERAL() != null) {
                    this.columnEditor.defaultValueExpression(ctx.constant().REAL_LITERAL().getText());
                }
            } else if (ctx.currentTimestamp() != null && !ctx.currentTimestamp().isEmpty()) {
                if (ctx.currentTimestamp().size() > 1 || ctx.ON() == null && ctx.UPDATE() == null) {
                    MySqlParser.CurrentTimestampContext currentTimestamp = ctx.currentTimestamp(0);
                    if (currentTimestamp.CURRENT_TIMESTAMP() == null && currentTimestamp.NOW() == null) {
                        this.columnEditor.defaultValueExpression(currentTimestamp.getText());
                    } else {
                        this.columnEditor.defaultValueExpression("1970-01-01 00:00:00");
                    }
                }
            } else if (ctx.expression() != null) {
                this.columnEditor.defaultValueExpression((String) null);
            }

            this.exitDefaultValue(true);
            super.enterDefaultValue(ctx);
        }
    }

    public void exitDefaultValue(boolean skipIfUnknownOptional) {
        boolean isOptionalColumn = this.optionalColumn.get() != null;
        if (!this.converted && (isOptionalColumn || !skipIfUnknownOptional)) {
            if (isOptionalColumn) {
                this.columnEditor.optional((Boolean) this.optionalColumn.get());
            }

            this.converted = true;
        }

    }

    private String unquote(String stringLiteral) {
        return stringLiteral == null || (!stringLiteral.startsWith("'") || !stringLiteral.endsWith("'")) && (!stringLiteral.startsWith("\"") || !stringLiteral.endsWith("\"")) ? stringLiteral : stringLiteral.substring(1, stringLiteral.length() - 1);
    }

    private String unquoteBinary(String stringLiteral) {
        return stringLiteral.substring(2, stringLiteral.length() - 1);
    }
}
