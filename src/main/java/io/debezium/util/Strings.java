package io.debezium.util;

import io.debezium.annotation.ThreadSafe;
import io.debezium.text.ParsingException;
import io.debezium.text.TokenStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ThreadSafe
public final class Strings {
    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]*):([0-9]*):([0-9]*)(\\.([0-9]*))?");
    private static final String CURLY_PREFIX = "${";
    private static final String CURLY_SUFFIX = "}";
    private static final String VAR_DELIM = ",";
    private static final String DEFAULT_DELIM = ":";

    public static <T> Set<T> setOf(String input, Function<String, String[]> splitter, Function<String, T> factory) {
        if (input == null) {
            return Collections.emptySet();
        } else {
            Set<T> matches = new HashSet();
            String[] var4 = (String[]) splitter.apply(input);
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String item = var4[var6];
                T obj = factory.apply(item);
                if (obj != null) {
                    matches.add(obj);
                }
            }

            return matches;
        }
    }

    public static <T> List<T> listOf(String input, Function<String, String[]> splitter, Function<String, T> factory) {
        if (input == null) {
            return Collections.emptyList();
        } else {
            List<T> matches = new ArrayList();
            String[] var4 = (String[]) splitter.apply(input);
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String item = var4[var6];
                T obj = factory.apply(item);
                if (obj != null) {
                    matches.add(obj);
                }
            }

            return matches;
        }
    }

    public static <T> Set<T> setOf(String input, char delimiter, Function<String, T> factory) {
        return setOf(input, (str) -> {
            return str.split("[" + delimiter + "]");
        }, factory);
    }

    public static <T> Set<T> setOf(String input, Function<String, T> factory) {
        return setOf(input, ',', factory);
    }

    public static Set<Pattern> setOfRegex(String input, int regexFlags) {
        return setOf(input, RegExSplitter::split, (str) -> {
            return Pattern.compile(str, regexFlags);
        });
    }

    public static Set<Pattern> setOfRegex(String input) {
        return setOf(input, RegExSplitter::split, Pattern::compile);
    }

    public static List<Pattern> listOfRegex(String input, int regexFlags) {
        return listOf(input, RegExSplitter::split, (str) -> {
            return Pattern.compile(str, regexFlags);
        });
    }

    public static List<String> splitLines(String content) {
        if (content != null && content.length() != 0) {
            String[] lines = content.split("[\\r]?\\n");
            return Arrays.asList(lines);
        } else {
            return Collections.emptyList();
        }
    }

    public static int compareTo(CharSequence str1, CharSequence str2) {
        if (str1 == str2) {
            return 0;
        } else if (str1 == null) {
            return -1;
        } else {
            return str2 == null ? 1 : str1.toString().compareTo(str2.toString());
        }
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        } else if (str1 == null) {
            return str2 == null;
        } else {
            return str1.equalsIgnoreCase(str2);
        }
    }

    public static String join(CharSequence delimiter, int[] values) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(values);
        if (values.length == 0) {
            return "";
        } else if (values.length == 1) {
            return Integer.toString(values[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(values[0]);

            for (int i = 1; i != values.length; ++i) {
                sb.append(delimiter);
                sb.append(values[i]);
            }

            return sb.toString();
        }
    }

    public static <T> String join(CharSequence delimiter, Iterable<T> values) {
        return join(delimiter, values, (v) -> {
            return v != null ? v.toString() : null;
        });
    }

    public static <T> String join(CharSequence delimiter, Iterable<T> values, Function<T, String> conversion) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(values);
        Iterator<T> iter = values.iterator();
        if (!iter.hasNext()) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            String first = (String) conversion.apply(iter.next());
            boolean delimit = false;
            if (first != null) {
                sb.append(first);
                delimit = true;
            }

            while (iter.hasNext()) {
                String next = (String) conversion.apply(iter.next());
                if (next != null) {
                    if (delimit) {
                        sb.append(delimiter);
                    }

                    sb.append(next);
                    delimit = true;
                }
            }

            return sb.toString();
        }
    }

    public static String trim(String str) {
        return trim(str, (c) -> {
            return c <= ' ';
        });
    }

    public static String trim(String str, CharacterPredicate predicate) {
        int len = str.length();
        if (len == 0) {
            return str;
        } else {
            int st;
            for (st = 0; st < len && predicate.test(str.charAt(st)); ++st) {
            }

            while (st < len && predicate.test(str.charAt(len - 1))) {
                --len;
            }

            return st <= 0 && len >= str.length() ? str : str.substring(st, len);
        }
    }

    public static String createString(char charToRepeat, int numberOfRepeats) {
        assert numberOfRepeats >= 0;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numberOfRepeats; ++i) {
            sb.append(charToRepeat);
        }

        return sb.toString();
    }

    public static String pad(String original, int length, char padChar) {
        if (original.length() >= length) {
            return original;
        } else {
            StringBuilder sb = new StringBuilder(original);

            while (sb.length() < length) {
                sb.append(padChar);
            }

            return sb.toString();
        }
    }

    public static String setLength(String original, int length, char padChar) {
        return justifyLeft(original, length, padChar, false);
    }

    public static String justify(Justify justify, String str, int width, char padWithChar) {
        switch (justify) {
            case LEFT:
                return justifyLeft(str, width, padWithChar);
            case RIGHT:
                return justifyRight(str, width, padWithChar);
            case CENTER:
                return justifyCenter(str, width, padWithChar);
            default:
                assert false;

                return null;
        }
    }

    public static String justifyRight(String str, int width, char padWithChar) {
        assert width > 0;

        str = str != null ? str.trim() : "";
        int length = str.length();
        int addChars = width - length;
        if (addChars < 0) {
            return str.subSequence(length - width, length).toString();
        } else {
            StringBuilder sb;
            for (sb = new StringBuilder(); addChars > 0; --addChars) {
                sb.append(padWithChar);
            }

            sb.append(str);
            return sb.toString();
        }
    }

    public static String justifyLeft(String str, int width, char padWithChar) {
        return justifyLeft(str, width, padWithChar, true);
    }

    protected static String justifyLeft(String str, int width, char padWithChar, boolean trimWhitespace) {
        str = str != null ? (trimWhitespace ? str.trim() : str) : "";
        int addChars = width - str.length();
        if (addChars < 0) {
            return str.subSequence(0, width).toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(str);

            while (addChars > 0) {
                sb.append(padWithChar);
                --addChars;
            }

            return sb.toString();
        }
    }

    public static String justifyCenter(String str, int width, char padWithChar) {
        str = str != null ? str.trim() : "";
        int addChars = width - str.length();
        if (addChars < 0) {
            return str.subSequence(0, width).toString();
        } else {
            int prependNumber = addChars / 2;
            int appendNumber = prependNumber;
            if (prependNumber + prependNumber != addChars) {
                ++prependNumber;
            }

            StringBuilder sb;
            for (sb = new StringBuilder(); prependNumber > 0; --prependNumber) {
                sb.append(padWithChar);
            }

            sb.append(str);

            while (appendNumber > 0) {
                sb.append(padWithChar);
                --appendNumber;
            }

            return sb.toString();
        }
    }

    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(bas);
            throwable.printStackTrace(pw);
            pw.close();
            return bas.toString();
        }
    }

    public static Number asNumber(String value) {
        return asNumber(value, (Supplier) null);
    }

    public static Number asNumber(String value, Supplier<Number> defaultValueProvider) {
        if (value != null) {
            try {
                return Short.valueOf(value);
            } catch (NumberFormatException var15) {
                try {
                    return Integer.valueOf(value);
                } catch (NumberFormatException var14) {
                    try {
                        return Long.valueOf(value);
                    } catch (NumberFormatException var13) {
                        try {
                            return Float.valueOf(value);
                        } catch (NumberFormatException var12) {
                            try {
                                return Double.valueOf(value);
                            } catch (NumberFormatException var11) {
                                try {
                                    return new BigInteger(value);
                                } catch (NumberFormatException var10) {
                                    try {
                                        return new BigDecimal(value);
                                    } catch (NumberFormatException var9) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return defaultValueProvider != null ? (Number) defaultValueProvider.get() : null;
    }

    public static int asInt(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException var3) {
            }
        }

        return defaultValue;
    }

    public static long asLong(String value, long defaultValue) {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException var4) {
            }
        }

        return defaultValue;
    }

    public static double asDouble(String value, double defaultValue) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException var4) {
            }
        }

        return defaultValue;
    }

    public static boolean asBoolean(String value, boolean defaultValue) {
        if (value != null) {
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException var3) {
            }
        }

        return defaultValue;
    }

    public static Duration asDuration(String timeString) {
        if (timeString == null) {
            return null;
        } else {
            Matcher matcher = TIME_PATTERN.matcher(timeString);
            if (!matcher.matches()) {
                throw new RuntimeException("Unexpected format for TIME column: " + timeString);
            } else {
                long hours = Long.parseLong(matcher.group(1));
                long minutes = Long.parseLong(matcher.group(2));
                long seconds = Long.parseLong(matcher.group(3));
                long nanoSeconds = 0L;
                String microSecondsString = matcher.group(5);
                if (microSecondsString != null) {
                    nanoSeconds = Long.parseLong(justifyLeft(microSecondsString, 9, '0'));
                }

                return hours >= 0L ? Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusNanos(nanoSeconds) : Duration.ofHours(hours).minusMinutes(minutes).minusSeconds(seconds).minusNanos(nanoSeconds);
            }
        }
    }

    public static String duration(long durationInMillis) {
        long seconds = durationInMillis / 1000L;
        long s = seconds % 60L;
        long m = seconds / 60L % 60L;
        long h = seconds / 3600L;
        long q = durationInMillis % 1000L;
        StringBuilder result = new StringBuilder(15);
        if (h < 10L) {
            result.append("0");
        }

        result.append(h).append(":");
        if (m < 10L) {
            result.append("0");
        }

        result.append(m).append(":");
        if (s < 10L) {
            result.append("0");
        }

        result.append(s).append(".");
        if (q == 0L) {
            result.append("0");
            return result.toString();
        } else {
            if (q < 10L) {
                result.append("00");
            } else if (q < 100L) {
                result.append("0");
            }

            result.append(q);
            int length = result.length();
            if (result.charAt(length - 1) == '0') {
                return result.charAt(length - 2) == '0' ? result.substring(0, length - 2) : result.substring(0, length - 1);
            } else {
                return result.toString();
            }
        }
    }

    public static Function<String, String> replaceVariablesWith(Function<String, String> replacementsByVariableName) {
        return (value) -> {
            return replaceVariables(value, replacementsByVariableName);
        };
    }

    public static String replaceVariables(String value, Function<String, String> replacementsByVariableName) {
        if (value != null && value.trim().length() != 0) {
            StringBuilder sb = new StringBuilder(value);
            int startName = sb.indexOf("${");
            if (startName == -1) {
                return value;
            } else {
                while (startName != -1) {
                    String defaultValue = null;
                    int endName = sb.indexOf("}", startName);
                    if (endName == -1) {
                        return sb.toString();
                    }

                    String varString = sb.substring(startName + 2, endName);
                    if (varString.indexOf(":") > -1) {
                        List<String> defaults = split(varString, ":");
                        varString = (String) defaults.get(0);
                        if (defaults.size() == 2) {
                            defaultValue = (String) defaults.get(1);
                        }
                    }

                    String constValue = null;
                    List<String> vars = split(varString, ",");
                    Iterator var9 = vars.iterator();

                    while (var9.hasNext()) {
                        String var = (String) var9.next();
                        constValue = (String) replacementsByVariableName.apply(var);
                        if (constValue != null) {
                            break;
                        }
                    }

                    if (constValue == null && defaultValue != null) {
                        constValue = defaultValue;
                    }

                    if (constValue != null) {
                        sb = sb.replace(startName, endName + 1, constValue);
                        startName = sb.indexOf("${");
                    } else {
                        startName = sb.indexOf("${", endName);
                    }
                }

                return sb.toString();
            }
        } else {
            return value;
        }
    }

    private static List<String> split(String str, String splitter) {
        StringTokenizer tokens = new StringTokenizer(str, splitter);
        ArrayList<String> l = new ArrayList(tokens.countTokens());

        while (tokens.hasMoreTokens()) {
            l.add(tokens.nextToken());
        }

        return l;
    }

    public static boolean isUuid(String str) {
        if (str == null) {
            return false;
        } else {
            try {
                UUID.fromString(str);
                return true;
            } catch (IllegalArgumentException var2) {
                return false;
            }
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNumeric(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        } else {
            int sz = str.length();

            for (int i = 0; i < sz; ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static String unquoteIdentifierPart(String identifierPart) {
        if (identifierPart != null && identifierPart.length() >= 2) {
            Character quotingChar = deriveQuotingChar(identifierPart);
            if (quotingChar != null) {
                identifierPart = identifierPart.substring(1, identifierPart.length() - 1);
                identifierPart = identifierPart.replace(quotingChar.toString() + quotingChar.toString(), quotingChar.toString());
            }

            return identifierPart;
        } else {
            return identifierPart;
        }
    }

    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            return null;
        } else {
            int length = hexString.length();
            byte[] bytes = new byte[length / 2];

            for (int i = 0; i < length; i += 2) {
                bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
            }

            return bytes;
        }
    }

    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return str != null && prefix != null && str.length() >= prefix.length() && str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static String getBegin(String str, int length) {
        if (str == null) {
            return null;
        } else {
            return str.length() < length ? str : str.substring(0, length);
        }
    }

    private static Character deriveQuotingChar(String identifierPart) {
        char first = identifierPart.charAt(0);
        char last = identifierPart.charAt(identifierPart.length() - 1);
        return first != last || first != '"' && first != '\'' && first != '`' ? null : first;
    }

    public static String mask(String original, String mask, String... sensitives) {
        return (String) Arrays.stream(sensitives).filter(Objects::nonNull).reduce(original, (masked, sensitive) -> {
            return masked.replace(sensitive, "***");
        });
    }

    private Strings() {
    }

    @FunctionalInterface
    public interface CharacterPredicate {
        boolean test(char var1);
    }

    public static enum Justify {
        LEFT,
        RIGHT,
        CENTER;

        // $FF: synthetic method
        private static Justify[] $values() {
            return new Justify[]{LEFT, RIGHT, CENTER};
        }
    }

    private static class RegExSplitter implements TokenStream.Tokenizer {
        public static String[] split(String identifier) {
            TokenStream stream = new TokenStream(identifier, new RegExSplitter(), true);
            stream.start();
            List<String> parts = new ArrayList();

            while (stream.hasNext()) {
                String part = stream.consume();
                if (part.length() != 0) {
                    parts.add(part.trim().replace("\\,", ","));
                }
            }

            return (String[]) parts.toArray(new String[parts.size()]);
        }

        public void tokenize(TokenStream.CharacterStream input, TokenStream.Tokens tokens) throws ParsingException {
            int tokenStart = 0;

            while (input.hasNext()) {
                char c = input.next();
                if (c == '\\') {
                    if (!input.hasNext()) {
                        throw new ParsingException(input.position(input.index()), "Unterminated escape sequence at the end of the string");
                    }

                    input.next();
                } else if (c == ',') {
                    tokens.addToken(input.position(tokenStart), tokenStart, input.index());
                    tokenStart = input.index() + 1;
                }
            }

            tokens.addToken(input.position(tokenStart), tokenStart, input.index() + 1);
        }
    }
}
