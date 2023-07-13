package io.debezium.connector.mysql.antlr.listener;

import io.debezium.antlr.AntlrDdlParser;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.TableEditor;
import io.debezium.relational.TableId;
import io.debezium.text.ParsingException;
import io.debezium.text.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUniqueIndexParserListener extends MySqlParserBaseListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlterTableParserListener.class);
    private final MySqlAntlrDdlParser parser;

    public CreateUniqueIndexParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterCreateIndex(MySqlParser.CreateIndexContext ctx) {
        if (ctx.UNIQUE() != null) {
            TableId tableId = this.parser.parseQualifiedTableId(ctx.tableName().fullId());
            if (!this.parser.getTableFilter().isIncluded(tableId)) {
                LOG.debug("{} is not monitored, no need to process unique index", tableId);
                return;
            }

            TableEditor tableEditor = this.parser.databaseTables().editTable(tableId);
            if (tableEditor == null) {
                String var10003 = tableId.toString();
                throw new ParsingException((Position) null, "Trying to create index on non existing table " + var10003 + ".Query: " + AntlrDdlParser.getText(ctx));
            }

            if (!tableEditor.hasPrimaryKey() && this.parser.isTableUniqueIndexIncluded(ctx.indexColumnNames(), tableEditor)) {
                this.parser.parseUniqueIndexColumnNames(ctx.indexColumnNames(), tableEditor);
                this.parser.databaseTables().overwriteTable(tableEditor.create());
                this.parser.signalCreateIndex(this.parser.parseName(ctx.uid()), tableId, ctx);
            }
        }

        super.enterCreateIndex(ctx);
    }
}
