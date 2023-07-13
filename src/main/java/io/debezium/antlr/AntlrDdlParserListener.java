package io.debezium.antlr;

import io.debezium.text.ParsingException;
import org.antlr.v4.runtime.tree.ParseTreeListener;

import java.util.Collection;

public interface AntlrDdlParserListener extends ParseTreeListener {
    Collection<ParsingException> getErrors();
}
