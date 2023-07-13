package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.Column;
import io.debezium.relational.ColumnEditor;
import io.debezium.relational.TableEditor;
import org.antlr.v4.runtime.tree.ParseTreeListener;

import java.util.List;

public class TableCommonParserListener extends MySqlParserBaseListener {
    protected final List<ParseTreeListener> listeners;
    protected final MySqlAntlrDdlParser parser;
    protected TableEditor tableEditor;
    protected ColumnDefinitionParserListener columnDefinitionListener;

    public TableCommonParserListener(MySqlAntlrDdlParser parser, List<ParseTreeListener> listeners) {
        this.parser = parser;
        this.listeners = listeners;
    }

    public void enterColumnDeclaration(MySqlParser.ColumnDeclarationContext ctx) {
        this.parser.runIfNotNull(() -> {
            MySqlParser.FullColumnNameContext fullColumnNameContext = ctx.fullColumnName();
            List<MySqlParser.DottedIdContext> dottedIdContextList = fullColumnNameContext.dottedId();
            MySqlParser.UidContext uidContext = fullColumnNameContext.uid();
            if (!dottedIdContextList.isEmpty()) {
                uidContext = ((MySqlParser.DottedIdContext) dottedIdContextList.get(dottedIdContextList.size() - 1)).uid();
            }

            String columnName = this.parser.parseName(uidContext);
            ColumnEditor columnEditor = Column.editor().name(columnName);
            if (this.columnDefinitionListener == null) {
                this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, columnEditor, this.parser, this.listeners);
                this.listeners.add(this.columnDefinitionListener);
            } else {
                this.columnDefinitionListener.setColumnEditor(columnEditor);
            }

        }, this.tableEditor);
        super.enterColumnDeclaration(ctx);
    }

    public void exitColumnDeclaration(MySqlParser.ColumnDeclarationContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.tableEditor.addColumn(this.columnDefinitionListener.getColumn());
        }, this.tableEditor, this.columnDefinitionListener);
        super.exitColumnDeclaration(ctx);
    }

    public void enterPrimaryKeyTableConstraint(MySqlParser.PrimaryKeyTableConstraintContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.parser.parsePrimaryIndexColumnNames(ctx.indexColumnNames(), this.tableEditor);
        }, this.tableEditor);
        super.enterPrimaryKeyTableConstraint(ctx);
    }

    public void enterUniqueKeyTableConstraint(MySqlParser.UniqueKeyTableConstraintContext ctx) {
        this.parser.runIfNotNull(() -> {
            if (!this.tableEditor.hasPrimaryKey() && this.parser.isTableUniqueIndexIncluded(ctx.indexColumnNames(), this.tableEditor)) {
                this.parser.parseUniqueIndexColumnNames(ctx.indexColumnNames(), this.tableEditor);
            }

        }, this.tableEditor);
        super.enterUniqueKeyTableConstraint(ctx);
    }
}
