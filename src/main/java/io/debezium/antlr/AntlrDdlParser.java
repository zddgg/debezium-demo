package io.debezium.antlr;

import io.debezium.ddl.parser.mysql.generated.MySqlParser;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables;
import io.debezium.relational.ddl.AbstractDdlParser;
import io.debezium.text.MultipleParsingExceptions;
import io.debezium.text.ParsingException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.Collection;

public abstract class AntlrDdlParser<L extends Lexer, P extends Parser> extends AbstractDdlParser {
    private final boolean throwErrorsFromTreeWalk;
    private AntlrDdlParserListener antlrDdlParserListener;
    protected Tables databaseTables;
    protected DataTypeResolver dataTypeResolver;

    public AntlrDdlParser(boolean throwErrorsFromTreeWalk, boolean includeViews, boolean includeComments) {
        super(includeViews, includeComments);
        this.throwErrorsFromTreeWalk = throwErrorsFromTreeWalk;
    }

    public void parse(String ddlContent, Tables databaseTables) {
        this.databaseTables = databaseTables;
        CodePointCharStream ddlContentCharStream = CharStreams.fromString(ddlContent);
        L lexer = this.createNewLexerInstance(new CaseChangingCharStream(ddlContentCharStream, this.isGrammarInUpperCase()));
        P parser = this.createNewParserInstance(new CommonTokenStream(lexer));
        this.dataTypeResolver = this.initializeDataTypeResolver();
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        ParsingErrorListener parsingErrorListener = new ParsingErrorListener(ddlContent, AbstractDdlParser::accumulateParsingFailure);
        parser.addErrorListener(parsingErrorListener);
        ParseTree parseTree = this.parseTree(parser);
        if (parsingErrorListener.getErrors().isEmpty()) {
            this.antlrDdlParserListener = this.createParseTreeWalkerListener();
            if (this.antlrDdlParserListener != null) {
                ParseTreeWalker.DEFAULT.walk(this.antlrDdlParserListener, parseTree);
                if (this.throwErrorsFromTreeWalk && !this.antlrDdlParserListener.getErrors().isEmpty()) {
                    this.throwParsingException(this.antlrDdlParserListener.getErrors());
                }
            }
        } else {
            this.throwParsingException(parsingErrorListener.getErrors());
        }

    }

    public Collection<ParsingException> getParsingExceptionsFromWalker() {
        return this.antlrDdlParserListener.getErrors();
    }

    protected abstract ParseTree parseTree(P var1);

    protected abstract AntlrDdlParserListener createParseTreeWalkerListener();

    protected abstract L createNewLexerInstance(CharStream var1);

    protected abstract P createNewParserInstance(CommonTokenStream var1);

    protected abstract boolean isGrammarInUpperCase();

    protected abstract DataTypeResolver initializeDataTypeResolver();

    public Tables databaseTables() {
        return this.databaseTables;
    }

    public DataTypeResolver dataTypeResolver() {
        return this.dataTypeResolver;
    }

    public static String getText(ParserRuleContext ctx) {
        return getText(ctx, ctx.start.getStartIndex(), ctx.stop.getStopIndex());
    }

    public static String getText(ParserRuleContext ctx, int start, int stop) {
        Interval interval = new Interval(start, stop);
        return ctx.start.getInputStream().getText(interval);
    }

    public boolean skipViews() {
        return this.skipViews;
    }

    public boolean skipComments() {
        return this.skipComments;
    }

    public void signalSetVariable(String variableName, String variableValue, int order, ParserRuleContext ctx) {
        this.signalSetVariable(variableName, variableValue, order, getText(ctx));
    }

    public void signalUseDatabase(ParserRuleContext ctx) {
        this.signalUseDatabase(getText(ctx));
    }

    public void signalCreateDatabase(String databaseName, ParserRuleContext ctx) {
        this.signalCreateDatabase(databaseName, getText(ctx));
    }

    public void signalAlterDatabase(String databaseName, String previousDatabaseName, ParserRuleContext ctx) {
        this.signalAlterDatabase(databaseName, previousDatabaseName, getText(ctx));
    }

    public void signalDropDatabase(String databaseName, ParserRuleContext ctx) {
        this.signalDropDatabase(databaseName, getText(ctx));
    }

    public void signalCreateTable(TableId id, ParserRuleContext ctx) {
        this.signalCreateTable(id, getText(ctx));
    }

    public void signalAlterTable(TableId id, TableId previousId, MySqlParser.RenameTableClauseContext ctx) {
        MySqlParser.RenameTableContext parent = (MySqlParser.RenameTableContext) ctx.getParent();
        Interval interval = new Interval(ctx.getParent().start.getStartIndex(), ((MySqlParser.RenameTableClauseContext) parent.renameTableClause().get(0)).start.getStartIndex() - 1);
        String prefix = ctx.getParent().start.getInputStream().getText(interval);
        this.signalAlterTable(id, previousId, (String) (prefix + getText(ctx)));
    }

    public void signalAlterTable(TableId id, TableId previousId, ParserRuleContext ctx) {
        this.signalAlterTable(id, previousId, (String) getText(ctx));
    }

    public void signalDropTable(TableId id, String statement) {
        super.signalDropTable(id, statement);
    }

    public void signalDropTable(TableId id, ParserRuleContext ctx) {
        this.signalDropTable(id, getText(ctx));
    }

    public void signalTruncateTable(TableId id, ParserRuleContext ctx) {
        this.signalTruncateTable(id, getText(ctx));
    }

    public void signalCreateView(TableId id, ParserRuleContext ctx) {
        this.signalCreateView(id, getText(ctx));
    }

    public void signalAlterView(TableId id, TableId previousId, ParserRuleContext ctx) {
        this.signalAlterView(id, previousId, getText(ctx));
    }

    public void signalDropView(TableId id, ParserRuleContext ctx) {
        this.signalDropView(id, getText(ctx));
    }

    public void signalCreateIndex(String indexName, TableId id, ParserRuleContext ctx) {
        this.signalCreateIndex(indexName, id, getText(ctx));
    }

    public void signalDropIndex(String indexName, TableId id, ParserRuleContext ctx) {
        this.signalDropIndex(indexName, id, getText(ctx));
    }

    public void debugParsed(ParserRuleContext ctx) {
        this.debugParsed(getText(ctx));
    }

    public void debugSkipped(ParserRuleContext ctx) {
        this.debugSkipped(getText(ctx));
    }

    public String withoutQuotes(ParserRuleContext ctx) {
        return withoutQuotes(ctx.getText());
    }

    private void throwParsingException(Collection<ParsingException> errors) {
        if (errors.size() == 1) {
            throw (ParsingException) errors.iterator().next();
        } else {
            throw new MultipleParsingExceptions(errors);
        }
    }
}
