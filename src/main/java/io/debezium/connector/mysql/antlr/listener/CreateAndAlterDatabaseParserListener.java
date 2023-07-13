package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;

public class CreateAndAlterDatabaseParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;
    private String databaseName;

    public CreateAndAlterDatabaseParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterCreateDatabase(MySqlParser.CreateDatabaseContext ctx) {
        this.databaseName = this.parser.parseName(ctx.uid());
        super.enterCreateDatabase(ctx);
    }

    public void exitCreateDatabase(MySqlParser.CreateDatabaseContext ctx) {
        this.parser.signalCreateDatabase(this.databaseName, ctx);
        super.exitCreateDatabase(ctx);
    }

    public void enterAlterSimpleDatabase(MySqlParser.AlterSimpleDatabaseContext ctx) {
        this.databaseName = ctx.uid() == null ? this.parser.currentSchema() : this.parser.parseName(ctx.uid());
        super.enterAlterSimpleDatabase(ctx);
    }

    public void enterCreateDatabaseOption(MySqlParser.CreateDatabaseOptionContext ctx) {
        String charsetName = this.parser.extractCharset(ctx.charsetName(), ctx.collationName());
        if (ctx.charsetName() != null) {
            if ("DEFAULT".equalsIgnoreCase(charsetName)) {
                charsetName = this.parser.systemVariables().getVariable("character_set_server");
            }

            this.parser.charsetNameForDatabase().put(this.databaseName, charsetName);
        } else if (ctx.charsetName() != null && !this.parser.charsetNameForDatabase().containsKey(charsetName)) {
            this.parser.charsetNameForDatabase().put(this.databaseName, charsetName);
        }

        super.enterCreateDatabaseOption(ctx);
    }
}
