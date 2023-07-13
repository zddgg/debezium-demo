package io.debezium.text;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class MultipleParsingExceptions extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final Collection<ParsingException> errors;

    public MultipleParsingExceptions(Collection<ParsingException> errors) {
        this("Multiple parsing errors", errors);
    }

    public MultipleParsingExceptions(String message, Collection<ParsingException> errors) {
        super(message);
        this.errors = Collections.unmodifiableCollection(errors);
    }

    public Collection<ParsingException> getErrors() {
        return this.errors;
    }

    public void forEachError(Consumer<ParsingException> action) {
        this.errors.forEach(action);
    }

    public void printStackTrace() {
        this.forEachError(Throwable::printStackTrace);
    }

    public void printStackTrace(PrintStream s) {
        this.forEachError((e) -> {
            e.printStackTrace(s);
        });
    }

    public void printStackTrace(PrintWriter s) {
        this.forEachError((e) -> {
            e.printStackTrace(s);
        });
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.getMessage());
        this.forEachError((e) -> {
            sb.append(System.lineSeparator()).append(e.toString());
        });
        return sb.toString();
    }
}
