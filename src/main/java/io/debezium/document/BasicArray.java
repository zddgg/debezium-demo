package io.debezium.document;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.util.Iterators;
import io.debezium.util.MathOps;
import io.debezium.util.Sequences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

@NotThreadSafe
final class BasicArray implements Array {
    private static final BiFunction<Integer, Value, Entry> CONVERT_PAIR_TO_ENTRY = new BiFunction<Integer, Value, Entry>() {
        public Entry apply(Integer index, Value value) {
            return new BasicEntry(index, value);
        }
    };
    private final List<Value> values;

    BasicArray() {
        this.values = new ArrayList();
    }

    BasicArray(List<Value> values) {
        assert values != null;

        this.values = values;
    }

    BasicArray(Value[] values) {
        if (values != null && values.length != 0) {
            this.values = new ArrayList(values.length);
            Value[] var2 = values;
            int var3 = values.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                Value value = var2[var4];
                this.values.add(value != null ? value : Value.nullValue());
            }
        } else {
            this.values = new ArrayList();
        }

    }

    protected int indexFrom(CharSequence name) {
        return Integer.parseInt(name.toString());
    }

    protected boolean isValidIndex(int index) {
        return index >= 0 && index < this.size();
    }

    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public int compareTo(Array that) {
        if (that == null) {
            return 1;
        } else {
            int size = this.size();
            if (size != that.size()) {
                return size - that.size();
            } else {
                Array thatArray = that;

                for (int i = 0; i != size; ++i) {
                    Value thatValue = thatArray.get(i);
                    Value thisValue = this.get(i);
                    int diff = thatValue.compareTo(thisValue);
                    if (diff != 0) {
                        return diff;
                    }
                }

                return 0;
            }
        }
    }

    public Iterator<Entry> iterator() {
        return Iterators.around((Iterable) Sequences.infiniteIntegers(0), (Iterable) this.values, CONVERT_PAIR_TO_ENTRY);
    }

    public Value remove(int index) {
        return this.isValidIndex(index) ? (Value) this.values.remove(index) : null;
    }

    public Array removeAll() {
        this.values.clear();
        return this;
    }

    public boolean has(int index) {
        return this.isValidIndex(index);
    }

    public Value get(int index) {
        return this.isValidIndex(index) ? (Value) this.values.get(index) : null;
    }

    public Array setValue(int index, Value value) {
        if (value == null) {
            value = Value.nullValue();
        }

        if (this.isValidIndex(index)) {
            this.values.set(index, value);
        } else {
            if (!this.isValidIndex(index - 1)) {
                throw new IllegalArgumentException("The index " + index + " is too large for this array, which has only " + this.size() + " values");
            }

            this.values.add(value);
        }

        return this;
    }

    public Array expand(int desiredSize, Value value) {
        if (desiredSize <= this.values.size()) {
            return this;
        } else {
            if (value == null) {
                value = Value.nullValue();
            }

            for (int i = this.values.size(); i < desiredSize; ++i) {
                this.values.add(value);
            }

            return this;
        }
    }

    public Array increment(int index, Value increment) {
        if (!increment.isNumber()) {
            throw new IllegalArgumentException("The increment must be a number but is " + increment);
        } else {
            Value current = this.get(index);
            if (current.isNumber()) {
                Value updated = Value.create((Object) MathOps.add(current.asNumber(), increment.asNumber()));
                this.setValue(index, Value.create((Object) updated));
            }

            return this;
        }
    }

    public Array add(Value value) {
        if (value == null) {
            value = Value.nullValue();
        }

        this.values.add(value);
        return this;
    }

    public Iterable<Value> values() {
        return this.values;
    }

    public Array clone() {
        return (new BasicArray()).addAll(this.values);
    }

    public int hashCode() {
        return this.values.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof BasicArray) {
            BasicArray that = (BasicArray) obj;
            return this.values.equals(that.values);
        } else {
            return false;
        }
    }

    public String toString() {
        return this.values.toString();
    }
}
