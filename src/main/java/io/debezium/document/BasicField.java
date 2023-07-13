package io.debezium.document;

import io.debezium.annotation.Immutable;
import io.debezium.util.Strings;

import java.util.Objects;

@Immutable
final class BasicField implements Document.Field, Comparable<Document.Field> {
    private final CharSequence name;
    private final Value value;

    BasicField(CharSequence name, Value value) {
        this.name = name;
        this.value = value;
    }

    public CharSequence getName() {
        return this.name;
    }

    public Value getValue() {
        return this.value;
    }

    public String toString() {
        return this.name + "=" + this.value;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Document.Field)) {
            return false;
        } else {
            Document.Field that = (Document.Field) obj;
            return this.getName().equals(that.getName()) && Objects.equals(this.getValue(), that.getValue());
        }
    }

    public int compareTo(Document.Field that) {
        if (this == that) {
            return 0;
        } else {
            int diff = Strings.compareTo(this.getName(), that.getName());
            return diff != 0 ? diff : Value.compareTo(this.getValue(), that.getValue());
        }
    }
}
