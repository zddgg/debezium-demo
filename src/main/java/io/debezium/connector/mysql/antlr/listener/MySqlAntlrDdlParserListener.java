package io.debezium.connector.mysql.antlr.listener;

import io.debezium.antlr.AntlrDdlParserListener;
import io.debezium.antlr.ProxyParseTreeListenerUtil;
import io.debezium.connector.mysql.antlr.MySqlAntlrDdlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.ddl.parser.mysql.generated.MySqlParserBaseListener;
import io.debezium.text.ParsingException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MySqlAntlrDdlParserListener extends MySqlParserBaseListener implements AntlrDdlParserListener {
    private final List<ParseTreeListener> listeners = new CopyOnWriteArrayList();
    private boolean skipNodes;
    private int skippedNodesCount = 0;
    private final Collection<ParsingException> errors = new ArrayList();

    public MySqlAntlrDdlParserListener(MySqlAntlrDdlParser parser) {
        this.listeners.add(new CreateAndAlterDatabaseParserListener(parser));
        this.listeners.add(new DropDatabaseParserListener(parser));
        this.listeners.add(new CreateTableParserListener(parser, this.listeners));
        this.listeners.add(new AlterTableParserListener(parser, this.listeners));
        this.listeners.add(new DropTableParserListener(parser));
        this.listeners.add(new RenameTableParserListener(parser));
        this.listeners.add(new TruncateTableParserListener(parser));
        this.listeners.add(new CreateViewParserListener(parser, this.listeners));
        this.listeners.add(new AlterViewParserListener(parser, this.listeners));
        this.listeners.add(new DropViewParserListener(parser));
        this.listeners.add(new CreateUniqueIndexParserListener(parser));
        this.listeners.add(new SetStatementParserListener(parser));
        this.listeners.add(new UseStatementParserListener(parser));
    }

    public Collection<ParsingException> getErrors() {
        return this.errors;
    }

    public void enterEveryRule(ParserRuleContext ctx) {
        if (this.skipNodes) {
            ++this.skippedNodesCount;
        } else {
            ProxyParseTreeListenerUtil.delegateEnterRule(ctx, this.listeners, this.errors);
        }

    }

    public void exitEveryRule(ParserRuleContext ctx) {
        if (this.skipNodes) {
            if (this.skippedNodesCount == 0) {
                this.skipNodes = false;
            } else {
                --this.skippedNodesCount;
            }
        } else {
            ProxyParseTreeListenerUtil.delegateExitRule(ctx, this.listeners, this.errors);
        }

    }

    public void visitErrorNode(ErrorNode node) {
        ProxyParseTreeListenerUtil.visitErrorNode(node, this.listeners, this.errors);
    }

    public void visitTerminal(TerminalNode node) {
        ProxyParseTreeListenerUtil.visitTerminal(node, this.listeners, this.errors);
    }

    public void enterRoutineBody(MySqlParser.RoutineBodyContext ctx) {
        this.skipNodes = true;
    }
}
