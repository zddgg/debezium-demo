package io.debezium.text;

import io.debezium.annotation.Immutable;

@Immutable
public final class Position {
    public static final Position EMPTY_CONTENT_POSITION = new Position(-1, 1, 0);
    private final int line;
    private final int column;
    private final int indexInContent;

    public Position(int indexInContent, int line, int column) {
        this.indexInContent = indexInContent < 0 ? -1 : indexInContent;
        this.line = line;
        this.column = column;

        assert this.indexInContent >= -1;

        assert this.line > 0;

        assert this.column >= 0;

        assert this.indexInContent >= 0 || this.line == 1 && this.column == 0;
    }

    public int index() {
        return this.indexInContent;
    }

    public int column() {
        return this.column;
    }

    public int line() {
        return this.line;
    }

    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public int hashCode() {
        return this.indexInContent;
    }

    public String toString() {
        return this.indexInContent + ":" + this.line + ":" + this.column;
    }

    public Position add(Position position) {
        if (this.index() < 0) {
            return position.index() < 0 ? EMPTY_CONTENT_POSITION : position;
        } else if (position.index() < 0) {
            return this;
        } else {
            int index = this.index() + position.index();
            int line = position.line() + this.line() - 1;
            int column = this.line() == 1 ? this.column() + position.column() : this.column();
            return new Position(index, line, column);
        }
    }
}
