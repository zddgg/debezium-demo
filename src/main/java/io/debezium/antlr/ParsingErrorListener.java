package io.debezium.antlr;

import io.debezium.text.ParsingException;
import io.debezium.text.Position;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;

public class ParsingErrorListener extends BaseErrorListener {
    private Collection<ParsingException> errors = new ArrayList();
    private final BiFunction<ParsingException, Collection<ParsingException>, Collection<ParsingException>> accumulateError;
    private final String parsedDdl;

    public ParsingErrorListener(String parsedDdl, BiFunction<ParsingException, Collection<ParsingException>, Collection<ParsingException>> accumulateError) {
        this.accumulateError = accumulateError;
        this.parsedDdl = parsedDdl;
    }

    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        String errorMessage = "DDL statement couldn't be parsed. Please open a Jira issue with the statement '" + this.parsedDdl + "'\n" + msg;
        this.accumulateError.apply(new ParsingException(new Position(0, line, charPositionInLine), errorMessage, e), this.errors);
    }

    public Collection<ParsingException> getErrors() {
        return this.errors;
    }
}
