package io.debezium.document;

import io.debezium.annotation.Immutable;

import java.util.Objects;

@Immutable
final class BasicEntry implements Array.Entry, Comparable<Array.Entry> {
    private final int index;
    private final Value value;

    BasicEntry(int index, Value value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return this.index;
    }

    public Value getValue() {
        return this.value;
    }

    public String toString() {
        return "@" + this.index + "=" + this.value;
    }

    public int hashCode() {
        return this.index;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Array.Entry)) {
            return false;
        } else {
            Array.Entry that = (Array.Entry) obj;
            return this.getIndex() == that.getIndex() && Objects.equals(this.getValue(), that.getValue());
        }
    }

    public int compareTo(Array.Entry that) {
        if (this == that) {
            return 0;
        } else {
            return this.getIndex() != that.getIndex() ? this.getIndex() - that.getIndex() : Value.compareTo(this.getValue(), that.getValue());
        }
    }
}
