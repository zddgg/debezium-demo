package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.MySqlSystemVariables;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;

public class UseStatementParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;

    public UseStatementParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterUseStatement(MySqlParser.UseStatementContext ctx) {
        String dbName = this.parser.parseName(ctx.uid());
        this.parser.setCurrentSchema(dbName);
        String charsetForDb = (String) this.parser.charsetNameForDatabase().get(dbName);
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_database", charsetForDb);
        this.parser.signalUseDatabase(ctx);
        super.enterUseStatement(ctx);
    }
}
