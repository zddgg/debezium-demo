package io.debezium.util;

import io.debezium.annotation.Immutable;

import java.util.Iterator;
import java.util.StringJoiner;

@Immutable
public final class Joiner {
    private final StringJoiner joiner;

    public static Joiner on(CharSequence delimiter) {
        return new Joiner(new StringJoiner(delimiter));
    }

    public static Joiner on(CharSequence prefix, CharSequence delimiter) {
        return new Joiner(new StringJoiner(delimiter, prefix, ""));
    }

    public static Joiner on(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return new Joiner(new StringJoiner(delimiter, prefix, suffix));
    }

    private Joiner(StringJoiner joiner) {
        this.joiner = joiner;
    }

    public String join(Object[] values) {
        Object[] var2 = values;
        int var3 = values.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Object value = var2[var4];
            if (value != null) {
                this.joiner.add(value.toString());
            }
        }

        return this.joiner.toString();
    }

    public String join(CharSequence firstValue, CharSequence... additionalValues) {
        if (firstValue != null) {
            this.joiner.add(firstValue);
        }

        CharSequence[] var3 = additionalValues;
        int var4 = additionalValues.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            CharSequence value = var3[var5];
            if (value != null) {
                this.joiner.add(value);
            }
        }

        return this.joiner.toString();
    }

    public String join(Iterable<?> values) {
        Iterator var2 = values.iterator();

        while (var2.hasNext()) {
            Object value = var2.next();
            if (value != null) {
                this.joiner.add(value.toString());
            }
        }

        return this.joiner.toString();
    }

    public String join(Iterable<?> values, CharSequence nextValue, CharSequence... additionalValues) {
        Iterator var4 = values.iterator();

        while (var4.hasNext()) {
            Object value = var4.next();
            if (value != null) {
                this.joiner.add(value.toString());
            }
        }

        if (nextValue != null) {
            this.joiner.add(nextValue);
        }

        CharSequence[] var8 = additionalValues;
        int var9 = additionalValues.length;

        for (int var6 = 0; var6 < var9; ++var6) {
            CharSequence value = var8[var6];
            if (value != null) {
                this.joiner.add(value);
            }
        }

        return this.joiner.toString();
    }

    public String join(Iterator<?> values) {
        while (values.hasNext()) {
            Object value = values.next();
            if (value != null) {
                this.joiner.add(value.toString());
            }
        }

        return this.joiner.toString();
    }
}
