package io.debezium.connector.mysql.antlr.listener;

import io.debezium.connector.mysql.MySqlSystemVariables;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;

public class SetStatementParserListener extends MySqlParserBaseListener {
    private final MySqlAntlrDdlParser parser;

    public SetStatementParserListener(MySqlAntlrDdlParser parser) {
        this.parser = parser;
    }

    public void enterSetVariable(MySqlParser.SetVariableContext ctx) {
        MySqlSystemVariables.MySqlScope scope = null;

        for (int i = 0; i < ctx.variableClause().size(); ++i) {
            MySqlParser.VariableClauseContext variableClauseContext = ctx.variableClause(i);
            String variableName;
            String variableIdentifier;
            if (variableClauseContext.uid() == null) {
                if (variableClauseContext.GLOBAL_ID() == null) {
                    continue;
                }

                variableIdentifier = variableClauseContext.GLOBAL_ID().getText();
                if (variableIdentifier.startsWith("@@global.")) {
                    scope = MySqlSystemVariables.MySqlScope.GLOBAL;
                    variableName = variableIdentifier.substring("@@global.".length());
                } else if (variableIdentifier.startsWith("@@session.")) {
                    scope = MySqlSystemVariables.MySqlScope.SESSION;
                    variableName = variableIdentifier.substring("@@session.".length());
                } else if (variableIdentifier.startsWith("@@local.")) {
                    scope = MySqlSystemVariables.MySqlScope.LOCAL;
                    variableName = variableIdentifier.substring("@@local.".length());
                } else {
                    scope = MySqlSystemVariables.MySqlScope.SESSION;
                    variableName = variableIdentifier.substring("@@".length());
                }
            } else {
                if (variableClauseContext.GLOBAL() != null) {
                    scope = MySqlSystemVariables.MySqlScope.GLOBAL;
                } else if (variableClauseContext.SESSION() != null) {
                    scope = MySqlSystemVariables.MySqlScope.SESSION;
                } else if (variableClauseContext.LOCAL() != null) {
                    scope = MySqlSystemVariables.MySqlScope.LOCAL;
                }

                variableName = this.parser.parseName(variableClauseContext.uid());
            }

            variableIdentifier = this.parser.withoutQuotes(ctx.expression(i));
            this.parser.systemVariables().setVariable(scope, variableName, variableIdentifier);
            if ("character_set_database".equalsIgnoreCase(variableName)) {
                String currentDatabaseName = this.parser.currentSchema();
                if (currentDatabaseName != null) {
                    this.parser.charsetNameForDatabase().put(currentDatabaseName, variableIdentifier);
                }
            }

            this.parser.signalSetVariable(variableName, variableIdentifier, i, ctx);
        }

        super.enterSetVariable(ctx);
    }

    public void enterSetCharset(MySqlParser.SetCharsetContext ctx) {
        String charsetName = ctx.charsetName() != null ? this.parser.withoutQuotes(ctx.charsetName()) : this.parser.currentDatabaseCharset();
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_client", charsetName);
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_results", charsetName);
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_connection", this.parser.systemVariables().getVariable("character_set_database"));
        super.enterSetCharset(ctx);
    }

    public void enterSetNames(MySqlParser.SetNamesContext ctx) {
        String charsetName = ctx.charsetName() != null ? this.parser.withoutQuotes(ctx.charsetName()) : this.parser.currentDatabaseCharset();
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_client", charsetName);
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_results", charsetName);
        this.parser.systemVariables().setVariable(MySqlSystemVariables.MySqlScope.SESSION, "character_set_connection", charsetName);
        super.enterSetNames(ctx);
    }
}
