package io.debezium.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;

public class CaseChangingCharStream implements CharStream {
    final CharStream stream;
    final boolean upper;

    public CaseChangingCharStream(CharStream stream, boolean upper) {
        this.stream = stream;
        this.upper = upper;
    }

    public String getText(Interval interval) {
        return this.stream.getText(interval);
    }

    public void consume() {
        this.stream.consume();
    }

    public int LA(int i) {
        int c = this.stream.LA(i);
        if (c <= 0) {
            return c;
        } else {
            return this.upper ? Character.toUpperCase(c) : Character.toLowerCase(c);
        }
    }

    public int mark() {
        return this.stream.mark();
    }

    public void release(int marker) {
        this.stream.release(marker);
    }

    public int index() {
        return this.stream.index();
    }

    public void seek(int index) {
        this.stream.seek(index);
    }

    public int size() {
        return this.stream.size();
    }

    public String getSourceName() {
        return this.stream.getSourceName();
    }
}
