package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.relational.ColumnEditor;
import io.debezium.relational.Table;
import io.debezium.relational.TableId;
import org.antlr.v4.runtime.tree.ParseTreeListener;

import java.util.List;
import java.util.stream.Collectors;

public class CreateTableParserListener extends TableCommonParserListener {
    public CreateTableParserListener(MySqlAntlrDdlParser parser, List<ParseTreeListener> listeners) {
        super(parser, listeners);
    }

    public void enterColumnCreateTable(MySqlParser.ColumnCreateTableContext ctx) {
        TableId tableId = this.parser.parseQualifiedTableId(ctx.tableName().fullId());
        if (this.parser.databaseTables().forTable(tableId) == null) {
            this.tableEditor = this.parser.databaseTables().editOrCreateTable(tableId);
            super.enterColumnCreateTable(ctx);
        }

    }

    public void exitColumnCreateTable(MySqlParser.ColumnCreateTableContext ctx) {
        this.parser.runIfNotNull(() -> {
            if (!this.tableEditor.hasDefaultCharsetName()) {
                this.tableEditor.setDefaultCharsetName(this.parser.charsetForTable(this.tableEditor.tableId()));
            }

            this.listeners.remove(this.columnDefinitionListener);
            this.columnDefinitionListener = null;
            String defaultCharsetName = this.tableEditor.create().defaultCharsetName();
            this.tableEditor.setColumns((Iterable) this.tableEditor.columns().stream().map((column) -> {
                ColumnEditor columnEditor = column.edit();
                if (columnEditor.charsetNameOfTable() == null) {
                    columnEditor.charsetNameOfTable(defaultCharsetName);
                }

                return columnEditor;
            }).map(ColumnEditor::create).collect(Collectors.toList()));
            this.parser.databaseTables().overwriteTable(this.tableEditor.create());
            this.parser.signalCreateTable(this.tableEditor.tableId(), ctx);
        }, this.tableEditor);
        super.exitColumnCreateTable(ctx);
    }

    public void exitCopyCreateTable(MySqlParser.CopyCreateTableContext ctx) {
        TableId tableId = this.parser.parseQualifiedTableId(ctx.tableName(0).fullId());
        TableId originalTableId = this.parser.parseQualifiedTableId(ctx.tableName(1).fullId());
        Table original = this.parser.databaseTables().forTable(originalTableId);
        if (original != null) {
            this.parser.databaseTables().overwriteTable(tableId, original.columns(), original.primaryKeyColumnNames(), original.defaultCharsetName(), original.attributes());
            this.parser.signalCreateTable(tableId, ctx);
        }

        super.exitCopyCreateTable(ctx);
    }

    public void enterTableOptionCharset(MySqlParser.TableOptionCharsetContext ctx) {
        this.parser.runIfNotNull(() -> {
            if (ctx.charsetName() != null) {
                this.tableEditor.setDefaultCharsetName(this.parser.withoutQuotes(ctx.charsetName()));
            }

        }, this.tableEditor);
        super.enterTableOptionCharset(ctx);
    }

    public void enterTableOptionComment(MySqlParser.TableOptionCommentContext ctx) {
        if (!this.parser.skipComments()) {
            this.parser.runIfNotNull(() -> {
                if (ctx.COMMENT() != null) {
                    MySqlAntlrDdlParser var10001 = this.parser;
                    this.tableEditor.setComment(MySqlAntlrDdlParser.withoutQuotes(ctx.STRING_LITERAL().getText()));
                }

            }, this.tableEditor);
        }

        super.enterTableOptionComment(ctx);
    }
}
