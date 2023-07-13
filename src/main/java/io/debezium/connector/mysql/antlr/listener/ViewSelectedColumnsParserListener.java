package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.Column;
import io.debezium.relational.Table;
import io.debezium.relational.TableEditor;
import io.debezium.relational.TableId;
import io.debezium.relational.ddl.AbstractDdlParser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ViewSelectedColumnsParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;
    private final TableEditor tableEditor;
    private TableEditor selectTableEditor;
    private Map<TableId, Table> tableByAlias = new HashMap();

    public ViewSelectedColumnsParserListener(TableEditor tableEditor, MySqlAntlrDdlParser parser) {
        this.tableEditor = tableEditor;
        this.parser = parser;
    }

    public List<Column> getSelectedColumns() {
        return this.selectTableEditor.columns();
    }

    public void exitQuerySpecification(MySqlParser.QuerySpecificationContext ctx) {
        if (ctx.fromClause() != null) {
            this.parseQuerySpecification(ctx.selectElements());
        }

        super.exitQuerySpecification(ctx);
    }

    public void exitQuerySpecificationNointo(MySqlParser.QuerySpecificationNointoContext ctx) {
        if (ctx.fromClause() != null) {
            this.parseQuerySpecification(ctx.selectElements());
        }

        super.exitQuerySpecificationNointo(ctx);
    }

    public void exitAtomTableItem(MySqlParser.AtomTableItemContext ctx) {
        this.parser.runIfNotNull(() -> {
            this.parseAtomTableItem(ctx, this.tableByAlias);
        }, this.tableEditor);
        super.exitAtomTableItem(ctx);
    }

    public void exitSubqueryTableItem(MySqlParser.SubqueryTableItemContext ctx) {
        this.parser.runIfNotNull(() -> {
            String tableAlias = this.parser.parseName(ctx.uid());
            TableId aliasTableId = this.parser.resolveTableId(this.parser.currentSchema(), tableAlias);
            this.selectTableEditor.tableId(aliasTableId);
            this.tableByAlias.put(aliasTableId, this.selectTableEditor.create());
        }, this.tableEditor);
        super.exitSubqueryTableItem(ctx);
    }

    private void parseQuerySpecification(MySqlParser.SelectElementsContext selectElementsContext) {
        this.parser.runIfNotNull(() -> {
            this.selectTableEditor = this.parseSelectElements(selectElementsContext);
        }, this.tableEditor);
    }

    private void parseAtomTableItem(MySqlParser.TableSourceItemContext ctx, Map<TableId, Table> tableByAlias) {
        if (ctx instanceof MySqlParser.AtomTableItemContext) {
            MySqlParser.AtomTableItemContext atomTableItemContext = (MySqlParser.AtomTableItemContext) ctx;
            TableId tableId = this.parser.parseQualifiedTableId(atomTableItemContext.tableName().fullId());
            Table table = (Table) tableByAlias.get(tableId);
            if (table == null) {
                table = this.parser.databaseTables().forTable(tableId);
            }

            if (atomTableItemContext.alias != null) {
                TableId aliasTableId = this.parser.resolveTableId(tableId.catalog(), this.parser.parseName(atomTableItemContext.alias));
                tableByAlias.put(aliasTableId, table);
            } else {
                tableByAlias.put(tableId, table);
            }
        }

    }

    private TableEditor parseSelectElements(MySqlParser.SelectElementsContext ctx) {
        TableEditor table = Table.editor();
        if (ctx.star != null) {
            this.tableByAlias.keySet().forEach((tableId) -> {
                table.addColumns(((Table) this.tableByAlias.get(tableId)).columns());
            });
        } else {
            ctx.selectElement().forEach((selectElementContext) -> {
                if (selectElementContext instanceof MySqlParser.SelectStarElementContext) {
                    TableId tableId = this.parser.parseQualifiedTableId(((MySqlParser.SelectStarElementContext) selectElementContext).fullId());
                    Table selectedTable = (Table) this.tableByAlias.get(tableId);
                    table.addColumns(selectedTable.columns());
                } else if (selectElementContext instanceof MySqlParser.SelectColumnElementContext) {
                    MySqlParser.SelectColumnElementContext selectColumnElementContext = (MySqlParser.SelectColumnElementContext) selectElementContext;
                    MySqlParser.FullColumnNameContext fullColumnNameContext = selectColumnElementContext.fullColumnName();
                    String schemaName = this.parser.currentSchema();
                    String tableName = null;
                    String columnName = this.parser.parseName(fullColumnNameContext.uid());
                    if (fullColumnNameContext.dottedId(0) != null) {
                        tableName = columnName;
                        if (fullColumnNameContext.dottedId(1) != null) {
                            schemaName = columnName;
                            tableName = AbstractDdlParser.withoutQuotes(fullColumnNameContext.dottedId(0).getText().substring(1));
                            columnName = AbstractDdlParser.withoutQuotes(fullColumnNameContext.dottedId(1).getText().substring(1));
                        } else {
                            columnName = AbstractDdlParser.withoutQuotes(fullColumnNameContext.dottedId(0).getText().substring(1));
                        }
                    }

                    String alias = columnName;
                    if (selectColumnElementContext.uid() != null) {
                        alias = this.parser.parseName(selectColumnElementContext.uid());
                    }

                    if (tableName != null) {
                        Table selectedTablex = (Table) this.tableByAlias.get(this.parser.resolveTableId(schemaName, tableName));
                        this.addColumnFromTable(table, columnName, alias, selectedTablex);
                    } else {
                        Iterator var13 = this.tableByAlias.values().iterator();

                        while (var13.hasNext()) {
                            Table selectedTablexx = (Table) var13.next();
                            this.addColumnFromTable(table, columnName, alias, selectedTablexx);
                        }
                    }
                }

            });
        }

        this.tableByAlias.clear();
        return table;
    }

    private MySqlParser.TableSourceItemContext getTableSourceItemContext(MySqlParser.TableSourceContext tableSourceContext) {
        if (tableSourceContext instanceof MySqlParser.TableSourceBaseContext) {
            return ((MySqlParser.TableSourceBaseContext) tableSourceContext).tableSourceItem();
        } else {
            return tableSourceContext instanceof MySqlParser.TableSourceNestedContext ? ((MySqlParser.TableSourceNestedContext) tableSourceContext).tableSourceItem() : null;
        }
    }

    private void addColumnFromTable(TableEditor table, String columnName, String newColumnName, Table selectedTable) {
        Iterator var5 = selectedTable.columns().iterator();

        while (var5.hasNext()) {
            Column column = (Column) var5.next();
            if (column.name().equals(columnName)) {
                table.addColumn(column.edit().name(newColumnName).create());
                break;
            }
        }

    }
}
