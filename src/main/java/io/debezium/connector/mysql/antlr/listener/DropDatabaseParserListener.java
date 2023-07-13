package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;

public class DropDatabaseParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;

    public DropDatabaseParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterDropDatabase(MySqlParser.DropDatabaseContext ctx) {
        String databaseName = this.parser.parseName(ctx.uid());
        this.parser.databaseTables().removeTablesForDatabase(databaseName);
        this.parser.charsetNameForDatabase().remove(databaseName);
        this.parser.signalDropDatabase(databaseName, ctx);
        super.enterDropDatabase(ctx);
    }
}
