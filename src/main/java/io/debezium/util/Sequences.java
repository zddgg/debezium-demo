package io.debezium.util;

import io.debezium.annotation.Immutable;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Immutable
public class Sequences {
    public static IntStream times(int number) {
        return IntStream.range(0, number);
    }

    public static Iterable<Integer> infiniteIntegers() {
        return infiniteIntegers(0);
    }

    public static Iterable<Integer> infiniteIntegers(final int startingAt) {
        return Iterators.around(new Iterator<Integer>() {
            private int counter = startingAt;

            public boolean hasNext() {
                return true;
            }

            public Integer next() {
                return this.counter++;
            }
        });
    }

    @SafeVarargs
    public static <T> Supplier<T> randomlySelect(T first, T... additional) {
        if (additional != null && additional.length != 0) {
            Random rng = new Random(System.currentTimeMillis());
            int max = additional.length + 1;
            return () -> {
                int index = rng.nextInt(max);
                return index == 0 ? first : additional[index - 1];
            };
        } else {
            return () -> {
                return first;
            };
        }
    }

    @SafeVarargs
    public static <T> Supplier<T> randomlySelect(T... values) {
        if (values != null && values.length != 0) {
            Random rng = new Random(System.currentTimeMillis());
            return () -> {
                return values[rng.nextInt(values.length)];
            };
        } else {
            throw new IllegalArgumentException("The values array may not be null or empty");
        }
    }
}
