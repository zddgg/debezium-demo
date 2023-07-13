package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.TableId;
import org.antlr.v4.runtime.misc.Interval;

public class DropTableParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;

    public DropTableParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterDropTable(MySqlParser.DropTableContext ctx) {
        Interval interval = new Interval(ctx.start.getStartIndex(), ctx.tables().start.getStartIndex() - 1);
        String prefix = ctx.start.getInputStream().getText(interval);
        ctx.tables().tableName().forEach((tableNameContext) -> {
            TableId tableId = this.parser.parseQualifiedTableId(tableNameContext.fullId());
            this.parser.databaseTables().removeTable(tableId);
            this.parser.signalDropTable(tableId, prefix + tableId.toQuotedString('`') + (ctx.dropType != null ? " " + ctx.dropType.getText() : ""));
        });
        super.enterDropTable(ctx);
    }
}
