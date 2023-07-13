package io.debezium.function;

import io.debezium.util.Strings;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Predicates {
    private static final Pattern LITERAL_SEPARATOR_PATTERN = Pattern.compile(",");

    public static Predicate<String> includesUuids(String uuidPatterns) {
        return includesLiteralsOrPatterns(uuidPatterns, Strings::isUuid, (s) -> {
            return s;
        });
    }

    public static Predicate<String> excludesUuids(String uuidPatterns) {
        return includesUuids(uuidPatterns).negate();
    }

    public static <T> Predicate<T> includesLiteralsOrPatterns(String literalsOrPatterns, Predicate<String> isLiteral, Function<T, String> conversion) {
        Set<String> literals = new HashSet();
        List<Pattern> patterns = new ArrayList();
        String[] var5 = LITERAL_SEPARATOR_PATTERN.split(literalsOrPatterns);
        int var6 = var5.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            String literalOrPattern = var5[var7];
            if (isLiteral.test(literalOrPattern)) {
                literals.add(literalOrPattern.toLowerCase());
            } else {
                patterns.add(Pattern.compile(literalOrPattern, 2));
            }
        }

        Predicate<T> patternsPredicate = includedInPatterns(patterns, (Function) conversion);
        Predicate<T> literalsPredicate = includedInLiterals(literals, conversion);
        if (patterns.isEmpty()) {
            return literalsPredicate;
        } else if (literals.isEmpty()) {
            return patternsPredicate;
        } else {
            return literalsPredicate.or(patternsPredicate);
        }
    }

    public static <T> Predicate<T> excludesLiteralsOrPatterns(String patterns, Predicate<String> isLiteral, Function<T, String> conversion) {
        return includesLiteralsOrPatterns(patterns, isLiteral, conversion).negate();
    }

    public static Predicate<String> includesLiterals(String literals) {
        return includesLiterals(literals, (s) -> {
            return s;
        });
    }

    public static Predicate<String> excludesLiterals(String literals) {
        return includesLiterals(literals).negate();
    }

    public static <T> Predicate<T> includesLiterals(String literals, Function<T, String> conversion) {
        String[] literalValues = LITERAL_SEPARATOR_PATTERN.split(literals.toLowerCase());
        Set<String> literalSet = new HashSet(Arrays.asList(literalValues));
        return includedInLiterals(literalSet, conversion);
    }

    public static <T> Predicate<T> excludesLiterals(String literals, Function<T, String> conversion) {
        return includesLiterals(literals, conversion).negate();
    }

    public static Predicate<String> includes(String regexPatterns) {
        return includes(regexPatterns, (str) -> {
            return str;
        });
    }

    public static Predicate<String> includes(String regexPatterns, int regexFlags) {
        Set<Pattern> patterns = Strings.setOfRegex(regexPatterns, regexFlags);
        return includedInPatterns(patterns, (Function) ((str) -> {
            return str;
        }));
    }

    public static Predicate<String> excludes(String regexPatterns) {
        return includes(regexPatterns).negate();
    }

    public static Predicate<String> excludes(String regexPatterns, int regexFlags) {
        return includes(regexPatterns, regexFlags).negate();
    }

    public static <T> Predicate<T> includes(String regexPatterns, Function<T, String> conversion) {
        Set<Pattern> patterns = Strings.setOfRegex(regexPatterns, 2);
        return includedInPatterns(patterns, (Function) conversion);
    }

    public static <T, U> BiPredicate<T, U> includes(String regexPatterns, BiFunction<T, U, String> conversion) {
        Set<Pattern> patterns = Strings.setOfRegex(regexPatterns, 2);
        return includedInPatterns(patterns, (BiFunction) conversion);
    }

    protected static <T> Predicate<T> includedInPatterns(Collection<Pattern> patterns, Function<T, String> conversion) {
        return (t) -> {
            return ((Optional) matchedByPattern(patterns, conversion).apply(t)).isPresent();
        };
    }

    protected static <T, U> BiPredicate<T, U> includedInPatterns(Collection<Pattern> patterns, BiFunction<T, U, String> conversion) {
        return (t, u) -> {
            return ((Optional) matchedByPattern(patterns, conversion).apply(t, u)).isPresent();
        };
    }

    public static Function<String, Optional<Pattern>> matchedBy(String regexPatterns) {
        return matchedByPattern(Strings.setOfRegex(regexPatterns, 2), (Function) Function.identity());
    }

    protected static <T> Function<T, Optional<Pattern>> matchedByPattern(Collection<Pattern> patterns, Function<T, String> conversion) {
        return (t) -> {
            String str = (String) conversion.apply(t);
            if (str != null) {
                Iterator var4 = patterns.iterator();

                while (var4.hasNext()) {
                    Pattern p = (Pattern) var4.next();
                    if (p.matcher(str).matches()) {
                        return Optional.of(p);
                    }
                }
            }

            return Optional.empty();
        };
    }

    protected static <T, U> BiFunction<T, U, Optional<Pattern>> matchedByPattern(Collection<Pattern> patterns, BiFunction<T, U, String> conversion) {
        return (t, u) -> {
            String str = (String) conversion.apply(t, u);
            if (str != null) {
                Iterator var5 = patterns.iterator();

                while (var5.hasNext()) {
                    Pattern p = (Pattern) var5.next();
                    if (p.matcher(str).matches()) {
                        return Optional.of(p);
                    }
                }
            }

            return Optional.empty();
        };
    }

    protected static <T> Predicate<T> includedInLiterals(Collection<String> literals, Function<T, String> conversion) {
        return (s) -> {
            String str = ((String) conversion.apply(s)).toLowerCase();
            return literals.contains(str);
        };
    }

    public static <T> Predicate<T> excludes(String regexPatterns, Function<T, String> conversion) {
        return includes(regexPatterns, conversion).negate();
    }

    public static <T> Predicate<T> filter(Predicate<T> allowed, Predicate<T> disallowed) {
        return allowed != null ? allowed : (disallowed != null ? disallowed : (id) -> {
            return true;
        });
    }

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    public static <T> Predicate<T> notNull() {
        return new Predicate<T>() {
            public boolean test(T t) {
                return t != null;
            }
        };
    }

    private Predicates() {
    }
}
