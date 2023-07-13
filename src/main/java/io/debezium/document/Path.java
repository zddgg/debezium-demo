package io.debezium.document;

import io.debezium.annotation.Immutable;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;

@Immutable
public interface Path extends Iterable<String> {
    static Path root() {
        return Paths.RootPath.INSTANCE;
    }

    static Optional<Path> optionalRoot() {
        return Paths.RootPath.OPTIONAL_OF_ROOT;
    }

    static Path parse(String path) {
        return Paths.parse(path, true);
    }

    static Path parse(String path, boolean resolveJsonPointerEscapes) {
        return Paths.parse(path, resolveJsonPointerEscapes);
    }

    default boolean isRoot() {
        return this.size() == 0;
    }

    default boolean isSingle() {
        return this.size() == 1;
    }

    default boolean isMultiple() {
        return this.size() > 1;
    }

    int size();

    Optional<Path> parent();

    Optional<String> lastSegment();

    Path subpath(int var1);

    String segment(int var1);

    default Path append(String relPath) {
        return this.append(parse(relPath));
    }

    Path append(Path var1);

    String toRelativePath();

    default void fromRoot(Consumer<Path> consumer) {
        Path path = root();
        Iterator var3 = this.iterator();

        while (var3.hasNext()) {
            String segment = (String) var3.next();
            path = path.append(segment);
            consumer.accept(path);
        }

    }

    public interface Segments {
        static boolean isAfterLastIndex(String segment) {
            return "-".equals(segment);
        }

        static boolean isArrayIndex(String segment) {
            return isAfterLastIndex(segment) || asInteger(segment).isPresent();
        }

        static boolean isFieldName(String segment) {
            return !isArrayIndex(segment);
        }

        static Optional<Integer> asInteger(String segment) {
            try {
                return Optional.of(Integer.valueOf(segment));
            } catch (NumberFormatException var2) {
                return Optional.empty();
            }
        }

        static Optional<Integer> asInteger(Optional<String> segment) {
            return segment.isPresent() ? asInteger((String) segment.get()) : Optional.empty();
        }
    }
}
