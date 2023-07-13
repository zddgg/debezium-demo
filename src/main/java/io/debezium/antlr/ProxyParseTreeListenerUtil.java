package io.debezium.antlr;

import io.debezium.text.ParsingException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collection;
import java.util.Iterator;

public class ProxyParseTreeListenerUtil {
    private ProxyParseTreeListenerUtil() {
    }

    public static void delegateEnterRule(ParserRuleContext ctx, Collection<ParseTreeListener> listeners, Collection<ParsingException> errors) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ParseTreeListener listener = (ParseTreeListener) var3.next();

            try {
                listener.enterEveryRule(ctx);
                ctx.enterRule(listener);
            } catch (ParsingException var6) {
                AntlrDdlParser.accumulateParsingFailure(var6, errors);
            }
        }

    }

    public static void delegateExitRule(ParserRuleContext ctx, Collection<ParseTreeListener> listeners, Collection<ParsingException> errors) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ParseTreeListener listener = (ParseTreeListener) var3.next();

            try {
                ctx.exitRule(listener);
                listener.exitEveryRule(ctx);
            } catch (ParsingException var6) {
                AntlrDdlParser.accumulateParsingFailure(var6, errors);
            }
        }

    }

    public static void visitErrorNode(ErrorNode node, Collection<ParseTreeListener> listeners, Collection<ParsingException> errors) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ParseTreeListener listener = (ParseTreeListener) var3.next();

            try {
                listener.visitErrorNode(node);
            } catch (ParsingException var6) {
                AntlrDdlParser.accumulateParsingFailure(var6, errors);
            }
        }

    }

    public static void visitTerminal(TerminalNode node, Collection<ParseTreeListener> listeners, Collection<ParsingException> errors) {
        Iterator var3 = listeners.iterator();

        while (var3.hasNext()) {
            ParseTreeListener listener = (ParseTreeListener) var3.next();

            try {
                listener.visitTerminal(node);
            } catch (ParsingException var6) {
                AntlrDdlParser.accumulateParsingFailure(var6, errors);
            }
        }

    }
}
