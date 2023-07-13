package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.relational.TableId;

public class TruncateTableParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;

    public TruncateTableParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterTruncateTable(MySqlParser.TruncateTableContext ctx) {
        TableId tableId = this.parser.parseQualifiedTableId(ctx.tableName().fullId());
        this.parser.signalTruncateTable(tableId, ctx);
        super.enterTruncateTable(ctx);
    }
}
