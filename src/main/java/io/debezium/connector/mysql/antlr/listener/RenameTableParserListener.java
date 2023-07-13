package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenameTableParserListener extends MySqlParserBaseListener {
    private static final Logger LOG = LoggerFactory.getLogger(RenameTableParserListener.class);
    private final MySqlAntlrDdlParser parser;

    public RenameTableParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterRenameTableClause(MySqlParser.RenameTableClauseContext ctx) {
        TableId oldTable = this.parser.parseQualifiedTableId(ctx.tableName(0).fullId());
        TableId newTable = this.parser.parseQualifiedTableId(ctx.tableName(1).fullId());
        if (this.parser.getTableFilter().isIncluded(oldTable) && !this.parser.getTableFilter().isIncluded(newTable)) {
            LOG.warn("Renaming included table {} to non-included table {}, this can lead to schema inconsistency", oldTable, newTable);
        } else if (!this.parser.getTableFilter().isIncluded(oldTable) && this.parser.getTableFilter().isIncluded(newTable)) {
            LOG.warn("Renaming non-included table {} to included table {}, this can lead to schema inconsistency", oldTable, newTable);
        }

        this.parser.databaseTables().renameTable(oldTable, newTable);
        this.parser.signalAlterTable(newTable, oldTable, ctx);
        super.enterRenameTableClause(ctx);
    }
}
