package io.debezium.connector.mysql.antlr.listener;

import io.debezium.antlr.AntlrDdlParser;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.relational.Column;
import io.debezium.relational.ColumnEditor;
import io.debezium.relational.TableId;
import io.debezium.text.ParsingException;
import io.debezium.text.Position;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AlterTableParserListener extends TableCommonParserListener {
    private static final int STARTING_INDEX = 1;
    private static final Logger LOG = LoggerFactory.getLogger(AlterTableParserListener.class);
    private ColumnEditor defaultValueColumnEditor;
    private DefaultValueParserListener defaultValueListener;
    private List<ColumnEditor> columnEditors;
    private int parsingColumnIndex = 1;

    public AlterTableParserListener(MySqlAntlrDdlParser parser, List<ParseTreeListener> listeners) {
        super(parser, listeners);
    }

    public void enterAlterTable(MySqlParser.AlterTableContext ctx) {
        TableId tableId = this.parser.parseQualifiedTableId(ctx.tableName().fullId());
        if (this.parser.databaseTables().forTable(tableId) == null) {
            LOG.debug("Ignoring ALTER TABLE statement for non-captured table {}", tableId);
        } else {
            this.tableEditor = this.parser.databaseTables().editTable(tableId);
            if (this.tableEditor == null) {
                String var10003 = tableId.toString();
                throw new ParsingException((Position) null, "Trying to alter table " + var10003 + ", which does not exist. Query: " + AntlrDdlParser.getText(ctx));
            } else {
                super.enterAlterTable(ctx);
            }
        }
    }

    public void exitAlterTable(MySqlParser.AlterTableContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.listeners.remove(this.columnDefinitionListener);
            this.parser.databaseTables().overwriteTable(this.tableEditor.create());
            this.parser.signalAlterTable(this.tableEditor.tableId(), (TableId) null, ctx.getParent());
        }, this.tableEditor);
        super.exitAlterTable(ctx);
        this.tableEditor = null;
    }

    public void enterAlterByAddColumn(MySqlParser.AlterByAddColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            String columnName = this.parser.parseName(ctx.uid(0));
            ColumnEditor columnEditor = Column.editor().name(columnName);
            this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, columnEditor, this.parser, this.listeners);
            this.listeners.add(this.columnDefinitionListener);
        }, this.tableEditor);
        super.exitAlterByAddColumn(ctx);
    }

    public void exitAlterByAddColumn(MySqlParser.AlterByAddColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            Column column = this.columnDefinitionListener.getColumn();
            this.tableEditor.addColumn(column);
            String columnName = column.name();
            if (ctx.FIRST() != null) {
                this.tableEditor.reorderColumn(columnName, (String) null);
            } else if (ctx.AFTER() != null) {
                String afterColumn = this.parser.parseName(ctx.uid(1));
                this.tableEditor.reorderColumn(columnName, afterColumn);
            }

            this.listeners.remove(this.columnDefinitionListener);
        }, this.tableEditor, this.columnDefinitionListener);
        super.exitAlterByAddColumn(ctx);
    }

    public void enterAlterByAddColumns(MySqlParser.AlterByAddColumnsContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.columnEditors = new ArrayList(ctx.uid().size());
            Iterator var2 = ctx.uid().iterator();

            while (var2.hasNext()) {
                MySqlParser.UidContext uidContext = (MySqlParser.UidContext) var2.next();
                String columnName = this.parser.parseName(uidContext);
                this.columnEditors.add(Column.editor().name(columnName));
            }

            this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, (ColumnEditor) this.columnEditors.get(0), this.parser, this.listeners);
            this.listeners.add(this.columnDefinitionListener);
        }, this.tableEditor);
        super.enterAlterByAddColumns(ctx);
    }

    public void exitColumnDefinition(MySqlParser.ColumnDefinitionContext ctx) {
        this.parser.runIfNotNull(() -> {
            if (this.columnEditors != null) {
                if (this.columnEditors.size() > this.parsingColumnIndex) {
                    this.columnDefinitionListener.setColumnEditor((ColumnEditor) this.columnEditors.get(this.parsingColumnIndex++));
                } else {
                    this.columnEditors.forEach((columnEditor) -> {
                        this.tableEditor.addColumn(columnEditor.create());
                    });
                    this.columnEditors = null;
                    this.parsingColumnIndex = 1;
                }
            }

        }, this.tableEditor, this.columnEditors);
        super.exitColumnDefinition(ctx);
    }

    public void exitAlterByAddColumns(MySqlParser.AlterByAddColumnsContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.columnEditors.forEach((columnEditor) -> {
                this.tableEditor.addColumn(columnEditor.create());
            });
            this.listeners.remove(this.columnDefinitionListener);
        }, this.tableEditor, this.columnEditors);
        super.exitAlterByAddColumns(ctx);
    }

    public void enterAlterByChangeColumn(MySqlParser.AlterByChangeColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            String oldColumnName = this.parser.parseName(ctx.oldColumn);
            Column existingColumn = this.tableEditor.columnWithName(oldColumnName);
            if (existingColumn != null) {
                ColumnEditor columnEditor = existingColumn.edit();
                columnEditor.unsetDefaultValueExpression();
                this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, columnEditor, this.parser, this.listeners);
                this.listeners.add(this.columnDefinitionListener);
            } else {
                throw new ParsingException((Position) null, "Trying to change column " + oldColumnName + " in " + this.tableEditor.tableId().toString() + " table, which does not exist. Query: " + AntlrDdlParser.getText(ctx));
            }
        }, this.tableEditor);
        super.enterAlterByChangeColumn(ctx);
    }

    public void exitAlterByChangeColumn(MySqlParser.AlterByChangeColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            Column column = this.columnDefinitionListener.getColumn();
            this.tableEditor.addColumn(column);
            String newColumnName = this.parser.parseName(ctx.newColumn);
            if (newColumnName != null && !column.name().equals(newColumnName)) {
                this.tableEditor.renameColumn(column.name(), newColumnName);
            }

            if (ctx.FIRST() != null) {
                this.tableEditor.reorderColumn(newColumnName, (String) null);
            } else if (ctx.afterColumn != null) {
                this.tableEditor.reorderColumn(newColumnName, this.parser.parseName(ctx.afterColumn));
            }

            this.listeners.remove(this.columnDefinitionListener);
        }, this.tableEditor, this.columnDefinitionListener);
        super.exitAlterByChangeColumn(ctx);
    }

    public void enterAlterByModifyColumn(MySqlParser.AlterByModifyColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            String columnName = this.parser.parseName(ctx.uid(0));
            Column existingColumn = this.tableEditor.columnWithName(columnName);
            if (existingColumn != null) {
                ColumnEditor columnEditor = existingColumn.edit();
                columnEditor.unsetDefaultValueExpression();
                this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, columnEditor, this.parser, this.listeners);
                this.listeners.add(this.columnDefinitionListener);
            } else {
                throw new ParsingException((Position) null, "Trying to change column " + columnName + " in " + this.tableEditor.tableId().toString() + " table, which does not exist. Query: " + AntlrDdlParser.getText(ctx));
            }
        }, this.tableEditor);
        super.enterAlterByModifyColumn(ctx);
    }

    public void exitAlterByModifyColumn(MySqlParser.AlterByModifyColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            Column column = this.columnDefinitionListener.getColumn();
            this.tableEditor.addColumn(column);
            if (ctx.FIRST() != null) {
                this.tableEditor.reorderColumn(column.name(), (String) null);
            } else if (ctx.AFTER() != null) {
                String afterColumn = this.parser.parseName(ctx.uid(1));
                this.tableEditor.reorderColumn(column.name(), afterColumn);
            }

            this.listeners.remove(this.columnDefinitionListener);
        }, this.tableEditor, this.columnDefinitionListener);
        super.exitAlterByModifyColumn(ctx);
    }

    public void enterAlterByDropColumn(MySqlParser.AlterByDropColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.tableEditor.removeColumn(this.parser.parseName(ctx.uid()));
        }, this.tableEditor);
        super.enterAlterByDropColumn(ctx);
    }

    public void enterAlterByRename(MySqlParser.AlterByRenameContext ctx) {
        this.parser.runIfNotNull(() -> {
            TableId newTableId = ctx.uid() != null ? this.parser.resolveTableId(this.parser.currentSchema(), this.parser.parseName(ctx.uid())) : this.parser.parseQualifiedTableId(ctx.fullId());
            this.parser.databaseTables().overwriteTable(this.tableEditor.create());
            this.parser.databaseTables().renameTable(this.tableEditor.tableId(), newTableId);
            this.tableEditor = this.parser.databaseTables().editTable(newTableId);
        }, this.tableEditor);
        super.enterAlterByRename(ctx);
    }

    public void enterAlterByChangeDefault(MySqlParser.AlterByChangeDefaultContext ctx) {
        this.parser.runIfNotNull(() -> {
            String columnName = this.parser.parseName(ctx.uid());
            Column column = this.tableEditor.columnWithName(columnName);
            if (column != null) {
                this.defaultValueColumnEditor = column.edit();
                if (ctx.SET() != null) {
                    this.defaultValueListener = new DefaultValueParserListener(this.defaultValueColumnEditor, new AtomicReference(column.isOptional()));
                    this.listeners.add(this.defaultValueListener);
                } else if (ctx.DROP() != null) {
                    this.defaultValueColumnEditor.unsetDefaultValueExpression();
                }
            }

        }, this.tableEditor);
        super.enterAlterByChangeDefault(ctx);
    }

    public void exitAlterByChangeDefault(MySqlParser.AlterByChangeDefaultContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.tableEditor.updateColumn(this.defaultValueColumnEditor.create());
            this.listeners.remove(this.defaultValueListener);
            this.defaultValueColumnEditor = null;
        }, this.defaultValueColumnEditor);
        super.exitAlterByChangeDefault(ctx);
    }

    public void enterAlterByAddPrimaryKey(MySqlParser.AlterByAddPrimaryKeyContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.parser.parsePrimaryIndexColumnNames(ctx.indexColumnNames(), this.tableEditor);
        }, this.tableEditor);
        super.enterAlterByAddPrimaryKey(ctx);
    }

    public void enterAlterByDropPrimaryKey(MySqlParser.AlterByDropPrimaryKeyContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.tableEditor.setPrimaryKeyNames(new ArrayList());
        }, this.tableEditor);
        super.enterAlterByDropPrimaryKey(ctx);
    }

    public void enterAlterByAddUniqueKey(MySqlParser.AlterByAddUniqueKeyContext ctx) {
        this.parser.runIfNotNull(() -> {
            if (!this.tableEditor.hasPrimaryKey() && this.parser.isTableUniqueIndexIncluded(ctx.indexColumnNames(), this.tableEditor)) {
                this.parser.parseUniqueIndexColumnNames(ctx.indexColumnNames(), this.tableEditor);
            }

        }, this.tableEditor);
        super.enterAlterByAddUniqueKey(ctx);
    }

    public void enterAlterByRenameColumn(MySqlParser.AlterByRenameColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            String oldColumnName = this.parser.parseName(ctx.oldColumn);
            Column existingColumn = this.tableEditor.columnWithName(oldColumnName);
            if (existingColumn != null) {
                ColumnEditor columnEditor = existingColumn.edit();
                this.columnDefinitionListener = new ColumnDefinitionParserListener(this.tableEditor, columnEditor, this.parser, this.listeners);
                this.listeners.add(this.columnDefinitionListener);
            } else {
                throw new ParsingException((Position) null, "Trying to change column " + oldColumnName + " in " + this.tableEditor.tableId().toString() + " table, which does not exist. Query: " + AntlrDdlParser.getText(ctx));
            }
        }, this.tableEditor);
        super.enterAlterByRenameColumn(ctx);
    }

    public void exitAlterByRenameColumn(MySqlParser.AlterByRenameColumnContext ctx) {
        this.parser.runIfNotNull(() -> {
            Column column = this.columnDefinitionListener.getColumn();
            this.tableEditor.addColumn(column);
            String newColumnName = this.parser.parseName(ctx.newColumn);
            if (newColumnName != null && !column.name().equals(newColumnName)) {
                this.tableEditor.renameColumn(column.name(), newColumnName);
            }

            this.listeners.remove(this.columnDefinitionListener);
        }, this.tableEditor, this.columnDefinitionListener);
        super.exitAlterByRenameColumn(ctx);
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
