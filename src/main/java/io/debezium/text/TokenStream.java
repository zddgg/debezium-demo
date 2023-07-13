package io.debezium.text;

import io.debezium.annotation.Immutable;
import io.debezium.annotation.NotThreadSafe;
import io.debezium.function.BooleanConsumer;
import io.debezium.util.Strings;

import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

@NotThreadSafe
public class TokenStream {
    public static final String ANY_VALUE = "any value";
    public static final int ANY_TYPE = Integer.MIN_VALUE;
    protected final String inputString;
    private final char[] inputContent;
    private final boolean caseSensitive;
    private final Tokenizer tokenizer;
    private List<Token> tokens;
    private ListIterator<Token> tokenIterator;
    private Token currentToken;
    private boolean completed;

    public TokenStream(String content, Tokenizer tokenizer, boolean caseSensitive) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(tokenizer, "tokenizer");
        this.inputString = content;
        this.inputContent = content.toCharArray();
        this.caseSensitive = caseSensitive;
        this.tokenizer = tokenizer;
    }

    public TokenStream start() throws ParsingException {
        if (this.tokens == null) {
            TokenFactory tokenFactory = this.caseSensitive ? new CaseSensitiveTokenFactory() : new CaseInsensitiveTokenFactory();
            CharacterStream characterStream = new CharacterArrayStream(this.inputContent);
            this.tokenizer.tokenize(characterStream, (Tokens) tokenFactory);
            this.tokens = this.initializeTokens(((TokenFactory) tokenFactory).getTokens());
        }

        this.tokenIterator = this.tokens.listIterator();
        this.moveToNextToken();
        return this;
    }

    protected List<Token> initializeTokens(List<Token> tokens) {
        return tokens;
    }

    public void rewind() {
        this.tokenIterator = this.tokens.listIterator();
        this.completed = false;
        this.currentToken = null;
        this.moveToNextToken();
    }

    public Marker mark() {
        if (this.completed) {
            return new Marker((Position) null, this.tokenIterator.previousIndex());
        } else {
            Token currentToken = this.currentToken();
            Position currentPosition = currentToken != null ? currentToken.position() : null;
            return new Marker(currentPosition, this.tokenIterator.previousIndex());
        }
    }

    public boolean rewind(Marker marker) {
        if (marker.tokenIndex >= 0 && marker.tokenIndex <= this.tokenIterator.nextIndex()) {
            this.completed = false;
            this.currentToken = null;
            this.tokenIterator = this.tokens.listIterator(marker.tokenIndex);
            this.moveToNextToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean advance(Marker marker) {
        if (marker.tokenIndex >= 0 && marker.tokenIndex >= this.tokenIterator.nextIndex()) {
            this.completed = false;
            this.currentToken = null;
            this.tokenIterator = this.tokens.listIterator(marker.tokenIndex);
            this.moveToNextToken();
            return true;
        } else {
            return false;
        }
    }

    public Position previousPosition() {
        return this.previousPosition(1);
    }

    public Position previousPosition(int count) {
        return this.previousToken(1).position();
    }

    public Position nextPosition() {
        return this.currentToken().position();
    }

    public String consume() throws ParsingException, IllegalStateException {
        if (this.completed) {
            this.throwNoMoreContent();
        }

        String result = this.currentToken().value();
        this.moveToNextToken();
        return result;
    }

    protected void throwNoMoreContent() throws ParsingException {
        Position pos = this.tokens.isEmpty() ? new Position(-1, 1, 0) : ((Token) this.tokens.get(this.tokens.size() - 1)).position();
        throw new ParsingException(pos, "No more content");
    }

    public String peek() throws IllegalStateException {
        if (this.completed) {
            this.throwNoMoreContent();
        }

        return this.currentToken().value();
    }

    public TokenStream consume(String expected) throws ParsingException, IllegalStateException {
        if (this.completed) {
            throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting token " + expected);
        } else if (expected != "any value" && !this.currentToken().matches(expected)) {
            String found = this.currentToken().value();
            Position pos = this.currentToken().position();
            String fragment = this.generateFragment();
            String msg = "Expecting " + expected + " at line " + pos.line() + ", column " + pos.column() + " but found '" + found + "': " + fragment;
            throw new ParsingException(pos, msg);
        } else {
            this.moveToNextToken();
            return this;
        }
    }

    public TokenStream consume(char expected) throws ParsingException, IllegalStateException {
        if (this.completed) {
            throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting '" + expected + "'");
        } else if (!this.currentToken().matches(expected)) {
            String found = this.currentToken().value();
            Position pos = this.currentToken().position();
            String fragment = this.generateFragment();
            String msg = "Expecting '" + expected + "' at line " + pos.line() + ", column " + pos.column() + " but found '" + found + "': " + fragment;
            throw new ParsingException(pos, msg);
        } else {
            this.moveToNextToken();
            return this;
        }
    }

    public TokenStream consume(int expectedType) throws ParsingException, IllegalStateException {
        if (this.completed) {
            throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting token of type " + expectedType);
        } else if (expectedType != Integer.MIN_VALUE && (this.currentToken().type() & expectedType) != expectedType) {
            String found = this.currentToken().value();
            Position pos = this.currentToken().position();
            String fragment = this.generateFragment();
            String msg = "Expecting token type " + expectedType + " at line " + pos.line() + ", column " + pos.column() + " but found '" + found + "': " + fragment;
            throw new ParsingException(pos, msg);
        } else {
            this.moveToNextToken();
            return this;
        }
    }

    public TokenStream consume(String expected, String... expectedForNextTokens) throws ParsingException, IllegalStateException {
        this.consume(expected);
        String[] var3 = expectedForNextTokens;
        int var4 = expectedForNextTokens.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            String nextExpected = var3[var5];
            this.consume(nextExpected);
        }

        return this;
    }

    public TokenStream consume(String[] nextTokens) throws ParsingException, IllegalStateException {
        String[] var2 = nextTokens;
        int var3 = nextTokens.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String nextExpected = var2[var4];
            this.consume(nextExpected);
        }

        return this;
    }

    public TokenStream consume(Iterable<String> nextTokens) throws ParsingException, IllegalStateException {
        Iterator var2 = nextTokens.iterator();

        while (var2.hasNext()) {
            String nextExpected = (String) var2.next();
            this.consume(nextExpected);
        }

        return this;
    }

    public String consumeAnyOf(int... typeOptions) throws IllegalStateException {
        if (this.completed) {
            throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting one token of type " + Strings.join("|", (int[]) typeOptions));
        } else {
            int[] var2 = typeOptions;
            int var3 = typeOptions.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                int typeOption = var2[var4];
                if (typeOption == Integer.MIN_VALUE || this.matches(typeOption)) {
                    return this.consume();
                }
            }

            String found = this.currentToken().value();
            Position pos = this.currentToken().position();
            String fragment = this.generateFragment();
            String msg = "Expecting " + Strings.join("|", (int[]) typeOptions) + " at line " + pos.line() + ", column " + pos.column() + " but found '" + found + "': " + fragment;
            throw new ParsingException(pos, msg);
        }
    }

    public String consumeAnyOf(String... options) throws IllegalStateException {
        if (this.completed) {
            throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting one token of " + String.join("|", options));
        } else {
            String[] var2 = options;
            int var3 = options.length;

            String option;
            for (int var4 = 0; var4 < var3; ++var4) {
                option = var2[var4];
                if (option == "any value" || this.matches(option)) {
                    return this.consume();
                }
            }

            String found = this.currentToken().value();
            Position pos = this.currentToken().position();
            String fragment = this.generateFragment();
            option = "Expecting " + String.join("|", options) + " at line " + pos.line() + ", column " + pos.column() + " but found '" + found + "': " + fragment;
            throw new ParsingException(pos, option);
        }
    }

    public TokenStream consumeThrough(char expected) throws ParsingException, IllegalStateException {
        return this.consumeThrough(String.valueOf(expected), (String) null);
    }

    public TokenStream consumeThrough(char expected, char skipMatchingTokens) throws ParsingException, IllegalStateException {
        return this.consumeThrough(String.valueOf(expected), String.valueOf(skipMatchingTokens));
    }

    public TokenStream consumeThrough(String expected) throws ParsingException, IllegalStateException {
        return this.consumeThrough(expected, (String) null);
    }

    public TokenStream consumeThrough(String expected, String skipMatchingTokens) throws ParsingException, IllegalStateException {
        if ("any value" == expected) {
            this.consume();
            return this;
        } else {
            this.consumeUntil(expected, skipMatchingTokens);
            this.consume(expected);
            return this;
        }
    }

    public TokenStream consumeUntil(char expected) throws ParsingException, IllegalStateException {
        return this.consumeUntil(String.valueOf(expected), (String[]) null);
    }

    public TokenStream consumeUntil(char expected, char skipMatchingTokens) throws ParsingException, IllegalStateException {
        return this.consumeUntil(String.valueOf(expected), String.valueOf(skipMatchingTokens));
    }

    public TokenStream consumeUntil(String expected) throws ParsingException, IllegalStateException {
        return this.consumeUntil(expected, (String[]) null);
    }

    public TokenStream consumeUntil(String expected, String... skipMatchingTokens) throws ParsingException, IllegalStateException {
        if ("any value" == expected) {
            this.consume();
            return this;
        } else {
            Marker start = this.mark();

            for (int remaining = 0; this.hasNext(); this.consume()) {
                if (skipMatchingTokens != null && this.matchesAnyOf(skipMatchingTokens)) {
                    ++remaining;
                }

                if (this.matches(expected)) {
                    if (remaining == 0) {
                        break;
                    }

                    --remaining;
                }
            }

            if (this.completed) {
                this.rewind(start);
                throw new ParsingException(((Token) this.tokens.get(this.tokens.size() - 1)).position(), "No more content but was expecting to find " + expected);
            } else {
                return this;
            }
        }
    }

    public TokenStream consumeUntilEndOrOneOf(String... stopTokens) throws ParsingException, IllegalStateException {
        while (this.hasNext() && !this.matchesAnyOf(stopTokens)) {
            this.consume();
        }

        return this;
    }

    public boolean canConsumeInteger(IntConsumer consumer) throws IllegalStateException {
        if (this.completed) {
            this.throwNoMoreContent();
        }

        String value = this.currentToken().value();

        try {
            int result = Integer.parseInt(value);
            this.moveToNextToken();
            consumer.accept(result);
            return true;
        } catch (NumberFormatException var4) {
            return false;
        }
    }

    public boolean canConsumeBoolean(BooleanConsumer consumer) throws IllegalStateException {
        if (this.completed) {
            this.throwNoMoreContent();
        }

        String value = this.currentToken().value();

        try {
            boolean result = Boolean.parseBoolean(value);
            this.moveToNextToken();
            consumer.accept(result);
            return true;
        } catch (NumberFormatException var4) {
            return false;
        }
    }

    public boolean canConsumeLong(LongConsumer consumer) throws IllegalStateException {
        if (this.completed) {
            this.throwNoMoreContent();
        }

        String value = this.currentToken().value();

        try {
            long result = Long.parseLong(value);
            this.moveToNextToken();
            consumer.accept(result);
            return true;
        } catch (NumberFormatException var5) {
            return false;
        }
    }

    public boolean canConsume(String expected) throws IllegalStateException {
        return this.canConsume(Integer.MIN_VALUE, expected);
    }

    public boolean canConsume(int type, String expected) throws IllegalStateException {
        if (this.matches(expected) && this.matches(type)) {
            this.moveToNextToken();
            return true;
        } else {
            return false;
        }
    }

    public boolean canConsumeWord(String expected) throws IllegalStateException {
        return this.canConsume(1, expected);
    }

    public boolean canConsume(char expected) throws IllegalStateException {
        if (!this.matches(expected)) {
            return false;
        } else {
            this.moveToNextToken();
            return true;
        }
    }

    public boolean canConsume(int expectedType) throws IllegalStateException {
        if (!this.matches(expectedType)) {
            return false;
        } else {
            this.moveToNextToken();
            return true;
        }
    }

    public boolean canConsume(String currentExpected, String... expectedForNextTokens) throws IllegalStateException {
        return this.canConsume(Integer.MIN_VALUE, currentExpected, expectedForNextTokens);
    }

    public boolean canConsume(int type, String currentExpected, String... expectedForNextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            if (!iter.hasNext()) {
                return false;
            } else {
                Token token = (Token) iter.next();
                if (currentExpected != "any value" && !token.matches(type, currentExpected)) {
                    return false;
                } else {
                    String[] var6 = expectedForNextTokens;
                    int var7 = expectedForNextTokens.length;

                    for (int var8 = 0; var8 < var7; ++var8) {
                        String nextExpected = var6[var8];
                        if (!iter.hasNext()) {
                            return false;
                        }

                        token = (Token) iter.next();
                        if (nextExpected != "any value" && !token.matches(type, nextExpected)) {
                            return false;
                        }
                    }

                    this.tokenIterator = iter;
                    this.currentToken = this.tokenIterator.hasNext() ? (Token) this.tokenIterator.next() : null;
                    this.completed = this.currentToken == null;
                    return true;
                }
            }
        }
    }

    public boolean canConsumeWords(String currentExpected, String... expectedForNextTokens) throws IllegalStateException {
        return this.canConsume(1, currentExpected, expectedForNextTokens);
    }

    public boolean canConsume(String[] nextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            Token token = null;
            String[] var4 = nextTokens;
            int var5 = nextTokens.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String nextExpected = var4[var6];
                if (!iter.hasNext()) {
                    return false;
                }

                token = (Token) iter.next();
                if (nextExpected != "any value" && !token.matches(nextExpected)) {
                    return false;
                }
            }

            this.tokenIterator = iter;
            this.currentToken = this.tokenIterator.hasNext() ? (Token) this.tokenIterator.next() : null;
            this.completed = this.currentToken == null;
            return true;
        }
    }

    public boolean canConsume(Iterable<String> nextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            Token token = null;
            Iterator var4 = nextTokens.iterator();

            String nextExpected;
            do {
                if (!var4.hasNext()) {
                    this.tokenIterator = iter;
                    this.currentToken = this.tokenIterator.hasNext() ? (Token) this.tokenIterator.next() : null;
                    this.completed = this.currentToken == null;
                    return true;
                }

                nextExpected = (String) var4.next();
                if (!iter.hasNext()) {
                    return false;
                }

                token = (Token) iter.next();
            } while (nextExpected == "any value" || token.matches(nextExpected));

            return false;
        }
    }

    public boolean canConsumeAnyOf(String firstOption, String... additionalOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else if (this.canConsume(firstOption)) {
            return true;
        } else {
            String[] var3 = additionalOptions;
            int var4 = additionalOptions.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String nextOption = var3[var5];
                if (this.canConsume(nextOption)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean canConsumeAnyOf(String[] options) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            String[] var2 = options;
            int var3 = options.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String option = var2[var4];
                if (this.canConsume(option)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean canConsumeAnyOf(Iterable<String> options) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Iterator var2 = options.iterator();

            String option;
            do {
                if (!var2.hasNext()) {
                    return false;
                }

                option = (String) var2.next();
            } while (!this.canConsume(option));

            return true;
        }
    }

    public boolean canConsumeAnyOf(int firstTypeOption, int... additionalTypeOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else if (this.canConsume(firstTypeOption)) {
            return true;
        } else {
            int[] var3 = additionalTypeOptions;
            int var4 = additionalTypeOptions.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                int nextTypeOption = var3[var5];
                if (this.canConsume(nextTypeOption)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean canConsumeAnyOf(int[] typeOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            int[] var2 = typeOptions;
            int var3 = typeOptions.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                int nextTypeOption = var2[var4];
                if (this.canConsume(nextTypeOption)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean matches(String expected) throws IllegalStateException {
        return this.matches(Integer.MIN_VALUE, expected);
    }

    public boolean matches(int type, String expected) throws IllegalStateException {
        return !this.completed && (expected == "any value" || this.currentToken().matches(expected)) && this.currentToken().matches(type);
    }

    public boolean matchesWord(String expected) throws IllegalStateException {
        return this.matches(1, (String) expected);
    }

    public boolean matches(char expected) throws IllegalStateException {
        return !this.completed && this.currentToken().matches(expected);
    }

    public boolean matches(int expectedType) throws IllegalStateException {
        return !this.completed && this.currentToken().matches(expectedType);
    }

    public boolean matches(String currentExpected, String... expectedForNextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            if (!iter.hasNext()) {
                return false;
            } else {
                Token token = (Token) iter.next();
                if (currentExpected != "any value" && !token.matches(currentExpected)) {
                    return false;
                } else {
                    String[] var5 = expectedForNextTokens;
                    int var6 = expectedForNextTokens.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        String nextExpected = var5[var7];
                        if (!iter.hasNext()) {
                            return false;
                        }

                        token = (Token) iter.next();
                        if (nextExpected != "any value" && !token.matches(nextExpected)) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public boolean matches(String[] nextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            Token token = null;
            String[] var4 = nextTokens;
            int var5 = nextTokens.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String nextExpected = var4[var6];
                if (!iter.hasNext()) {
                    return false;
                }

                token = (Token) iter.next();
                if (nextExpected != "any value" && !token.matches(nextExpected)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean matches(Iterable<String> nextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            Token token = null;
            Iterator var4 = nextTokens.iterator();

            String nextExpected;
            do {
                if (!var4.hasNext()) {
                    return true;
                }

                nextExpected = (String) var4.next();
                if (!iter.hasNext()) {
                    return false;
                }

                token = (Token) iter.next();
            } while (nextExpected == "any value" || token.matches(nextExpected));

            return false;
        }
    }

    public boolean matches(int currentExpectedType, int... expectedTypeForNextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            if (!iter.hasNext()) {
                return false;
            } else {
                Token token = (Token) iter.next();
                if (currentExpectedType != Integer.MIN_VALUE && (this.currentToken().type() & currentExpectedType) != currentExpectedType) {
                    return false;
                } else {
                    int[] var5 = expectedTypeForNextTokens;
                    int var6 = expectedTypeForNextTokens.length;

                    for (int var7 = 0; var7 < var6; ++var7) {
                        int nextExpectedType = var5[var7];
                        if (!iter.hasNext()) {
                            return false;
                        }

                        token = (Token) iter.next();
                        if (nextExpectedType != Integer.MIN_VALUE && (token.type() & nextExpectedType) != nextExpectedType) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }
    }

    public boolean matches(int[] typesForNextTokens) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
            Token token = null;
            int[] var4 = typesForNextTokens;
            int var5 = typesForNextTokens.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                int nextExpectedType = var4[var6];
                if (!iter.hasNext()) {
                    return false;
                }

                token = (Token) iter.next();
                if (nextExpectedType != Integer.MIN_VALUE && !token.matches(nextExpectedType)) {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean matchesAnyOf(String firstOption, String... additionalOptions) throws IllegalStateException {
        return this.matchesAnyOf(Integer.MIN_VALUE, firstOption, additionalOptions);
    }

    public boolean matchesAnyOf(int type, String firstOption, String... additionalOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Token current = this.currentToken();
            if (current.matches(type, firstOption)) {
                return true;
            } else {
                String[] var5 = additionalOptions;
                int var6 = additionalOptions.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    String nextOption = var5[var7];
                    if (current.matches(type, nextOption)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public boolean matchesAnyWordOf(String firstOption, String... additionalOptions) throws IllegalStateException {
        return this.matchesAnyOf(1, firstOption, additionalOptions);
    }

    public boolean matchesAnyOf(String[] options) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Token current = this.currentToken();
            String[] var3 = options;
            int var4 = options.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String option = var3[var5];
                if (current.matches(option)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean matchesAnyOf(Iterable<String> options) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Token current = this.currentToken();
            Iterator var3 = options.iterator();

            String option;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                option = (String) var3.next();
            } while (!current.matches(option));

            return true;
        }
    }

    public boolean matchesAnyOf(int firstTypeOption, int... additionalTypeOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Token current = this.currentToken();
            if (current.matches(firstTypeOption)) {
                return true;
            } else {
                int[] var4 = additionalTypeOptions;
                int var5 = additionalTypeOptions.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    int nextTypeOption = var4[var6];
                    if (current.matches(nextTypeOption)) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public boolean matchesAnyOf(int[] typeOptions) throws IllegalStateException {
        if (this.completed) {
            return false;
        } else {
            Token current = this.currentToken();
            int[] var3 = typeOptions;
            int var4 = typeOptions.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                int nextTypeOption = var3[var5];
                if (current.matches(nextTypeOption)) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean hasNext() {
        if (this.tokenIterator == null) {
            throw new IllegalStateException("start() method must be called before hasNext()");
        } else {
            return !this.completed;
        }
    }

    public String toString() {
        ListIterator<Token> iter = this.tokens.listIterator(this.tokenIterator.previousIndex());
        StringBuilder sb = new StringBuilder();
        if (iter.hasNext()) {
            sb.append(iter.next());
            int count = 1;

            while (iter.hasNext()) {
                if (count > 20) {
                    sb.append(" ...");
                    break;
                }

                sb.append("  ");
                ++count;
                sb.append(iter.next());
            }
        }

        return sb.toString();
    }

    private void moveToNextToken(List<Token> newTokens) {
        if (newTokens != null && !newTokens.isEmpty()) {
            Iterator var2 = newTokens.iterator();

            while (var2.hasNext()) {
                Token t = (Token) var2.next();
                this.tokenIterator.add(t);
            }

            for (int i = 0; i < newTokens.size() - 1; ++i) {
                this.tokenIterator.previous();
            }

            this.currentToken = (Token) newTokens.get(0);
        } else {
            if (!this.tokenIterator.hasNext()) {
                this.completed = true;
                this.currentToken = null;
            } else {
                this.currentToken = (Token) this.tokenIterator.next();
            }

        }
    }

    private void moveToNextToken() {
        this.moveToNextToken((List) null);
    }

    final Token currentToken() throws IllegalStateException, NoSuchElementException {
        if (this.currentToken == null) {
            if (this.completed) {
                throw new NoSuchElementException("No more content");
            } else {
                throw new IllegalStateException("start() method must be called before consuming or matching");
            }
        } else {
            assert this.currentToken != null;

            return this.currentToken;
        }
    }

    public String getContentFrom(Marker starting) {
        Objects.requireNonNull(starting, "starting");
        int startIndex = starting.position != null ? starting.position.index() : 0;
        return this.getContentBetween(startIndex, this.hasNext() ? this.nextPosition() : null);
    }

    public String getContentBetween(Marker starting, Position end) {
        Objects.requireNonNull(starting, "starting");
        int startIndex = starting.position != null ? starting.position.index() : 0;
        return this.getContentBetween(startIndex, end);
    }

    public String getContentBetween(Position starting, Position end) {
        Objects.requireNonNull(starting, "starting");
        return this.getContentBetween(starting.index(), end);
    }

    protected String getContentBetween(int startIndex, Position end) {
        int endIndex = end != null ? end.index() : this.inputString.length();
        if (startIndex >= endIndex) {
            throw new IllegalArgumentException("The starting position " + startIndex + " must be before the end position " + end);
        } else {
            return this.inputString.substring(startIndex, endIndex);
        }
    }

    public final Token previousToken(int count) throws IllegalStateException, NoSuchElementException {
        if (count < 1) {
            throw new IllegalArgumentException("The count must be positive");
        } else if (this.currentToken == null) {
            if (this.completed) {
                if (this.tokens.isEmpty()) {
                    throw new NoSuchElementException("No more content");
                } else {
                    return (Token) this.tokens.get(this.tokens.size() - 1);
                }
            } else {
                throw new IllegalStateException("start() method must be called before consuming or matching");
            }
        } else {
            int index = this.tokenIterator.previousIndex() - count;
            if (index < 0) {
                throw new NoSuchElementException("No more content");
            } else {
                return (Token) this.tokens.get(this.tokenIterator.previousIndex() - count);
            }
        }
    }

    String generateFragment() {
        assert this.currentToken != null;

        int startIndex = this.currentToken.startIndex();
        return generateFragment(this.inputString, startIndex, 20, " ===>> ");
    }

    static String generateFragment(String content, int indexOfProblem, int charactersToIncludeBeforeAndAfter, String highlightText) {
        assert content != null;

        assert indexOfProblem < content.length();

        int beforeStart = Math.max(0, indexOfProblem - charactersToIncludeBeforeAndAfter);
        String before = content.substring(beforeStart, indexOfProblem);
        int afterEnd = Math.min(indexOfProblem + charactersToIncludeBeforeAndAfter, content.length());
        String after = content.substring(indexOfProblem, afterEnd);
        return before + (highlightText != null ? highlightText : "") + after;
    }

    public static BasicTokenizer basicTokenizer(boolean includeComments) {
        return new BasicTokenizer(includeComments);
    }

    public String getInputString() {
        return this.inputString;
    }

    public interface Tokenizer {
        void tokenize(CharacterStream var1, Tokens var2) throws ParsingException;
    }

    public class CaseSensitiveTokenFactory extends TokenFactory {
        public CaseSensitiveTokenFactory() {
            super();
        }

        public void addToken(Position position, int startIndex, int endIndex, int type) {
            this.tokens.add(TokenStream.this.new CaseSensitiveToken(startIndex, endIndex, type, position));
        }
    }

    public class CaseInsensitiveTokenFactory extends TokenFactory {
        public CaseInsensitiveTokenFactory() {
            super();
        }

        public void addToken(Position position, int startIndex, int endIndex, int type) {
            this.tokens.add(TokenStream.this.new CaseInsensitiveToken(startIndex, endIndex, type, position));
        }
    }

    public static final class CharacterArrayStream implements CharacterStream {
        private final char[] content;
        private int lastIndex = -1;
        private final int maxIndex;
        private int lineNumber = 1;
        private int columnNumber = 0;
        private boolean nextCharMayBeLineFeed;

        public CharacterArrayStream(char[] content) {
            this.content = content;
            this.maxIndex = content.length - 1;
        }

        public boolean hasNext() {
            return this.lastIndex < this.maxIndex;
        }

        public int index() {
            return this.lastIndex;
        }

        public Position position(int startIndex) {
            return new Position(startIndex, this.lineNumber, this.columnNumber);
        }

        public String substring(int startIndex, int endIndex) {
            return new String(this.content, startIndex, endIndex - startIndex);
        }

        public char next() {
            if (this.lastIndex >= this.maxIndex) {
                throw new NoSuchElementException();
            } else {
                char result = this.content[++this.lastIndex];
                ++this.columnNumber;
                if (result == '\r') {
                    this.nextCharMayBeLineFeed = true;
                    ++this.lineNumber;
                    this.columnNumber = 0;
                } else if (result == '\n') {
                    if (!this.nextCharMayBeLineFeed) {
                        ++this.lineNumber;
                    }

                    this.columnNumber = 0;
                } else if (this.nextCharMayBeLineFeed) {
                    this.nextCharMayBeLineFeed = false;
                }

                return result;
            }
        }

        public boolean isNext(char c) {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && this.content[nextIndex] == c;
        }

        public boolean isNext(char nextChar1, char nextChar2) {
            int nextIndex1 = this.lastIndex + 1;
            int nextIndex2 = this.lastIndex + 2;
            return nextIndex2 <= this.maxIndex && this.content[nextIndex1] == nextChar1 && this.content[nextIndex2] == nextChar2;
        }

        public boolean isNext(char nextChar1, char nextChar2, char nextChar3) {
            int nextIndex1 = this.lastIndex + 1;
            int nextIndex2 = this.lastIndex + 2;
            int nextIndex3 = this.lastIndex + 3;
            return nextIndex3 <= this.maxIndex && this.content[nextIndex1] == nextChar1 && this.content[nextIndex2] == nextChar2 && this.content[nextIndex3] == nextChar3;
        }

        public boolean isNextAnyOf(char[] characters) {
            int nextIndex = this.lastIndex + 1;
            if (nextIndex <= this.maxIndex) {
                char nextChar = this.content[this.lastIndex + 1];
                char[] var4 = characters;
                int var5 = characters.length;

                for (int var6 = 0; var6 < var5; ++var6) {
                    char c = var4[var6];
                    if (c == nextChar) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean isNextAnyOf(String characters) {
            int nextIndex = this.lastIndex + 1;
            if (nextIndex <= this.maxIndex) {
                char nextChar = this.content[this.lastIndex + 1];
                if (characters.indexOf(nextChar) != -1) {
                    return true;
                }
            }

            return false;
        }

        public boolean isNextWhitespace() {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && Character.isWhitespace(this.content[nextIndex]);
        }

        public boolean isNextLetterOrDigit() {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && Character.isLetterOrDigit(this.content[nextIndex]);
        }

        public boolean isNextValidXmlCharacter() {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && XmlCharacters.isValid(this.content[nextIndex]);
        }

        public boolean isNextValidXmlNameCharacter() {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && XmlCharacters.isValidName(this.content[nextIndex]);
        }

        public boolean isNextValidXmlNcNameCharacter() {
            int nextIndex = this.lastIndex + 1;
            return nextIndex <= this.maxIndex && XmlCharacters.isValidNcName(this.content[nextIndex]);
        }
    }

    public interface CharacterStream {
        boolean hasNext();

        char next();

        int index();

        Position position(int var1);

        String substring(int var1, int var2);

        boolean isNextWhitespace();

        boolean isNextLetterOrDigit();

        boolean isNextValidXmlCharacter();

        boolean isNextValidXmlNameCharacter();

        boolean isNextValidXmlNcNameCharacter();

        boolean isNext(char var1);

        boolean isNext(char var1, char var2);

        boolean isNext(char var1, char var2, char var3);

        boolean isNextAnyOf(char[] var1);

        boolean isNextAnyOf(String var1);
    }

    public interface Tokens {
        default void addToken(Position position, int index) {
            this.addToken(position, index, index + 1, 0);
        }

        default void addToken(Position position, int startIndex, int endIndex) {
            this.addToken(position, startIndex, endIndex, 0);
        }

        void addToken(Position var1, int var2, int var3, int var4);
    }

    protected abstract class TokenFactory implements Tokens {
        protected final List<Token> tokens = new ArrayList();

        public List<Token> getTokens() {
            return this.tokens;
        }
    }

    @Immutable
    public interface Token {
        String value();

        boolean matches(String var1);

        default boolean matches(int expectedType, String expected) {
            return this.matches(expectedType) && this.matches(expected);
        }

        boolean matches(char var1);

        boolean matches(int var1);

        int type();

        int startIndex();

        int endIndex();

        int length();

        Position position();

        Token withType(int var1);
    }

    public static final class Marker implements Comparable<Marker> {
        protected final int tokenIndex;
        protected final Position position;

        protected Marker(Position position, int index) {
            this.position = position;
            this.tokenIndex = index;
        }

        public Position position() {
            return this.position;
        }

        public int compareTo(Marker that) {
            return this == that ? 0 : this.tokenIndex - that.tokenIndex;
        }

        public String toString() {
            return Integer.toString(this.tokenIndex);
        }
    }

    public static class BasicTokenizer implements Tokenizer {
        public static final int WORD = 1;
        public static final int SYMBOL = 2;
        public static final int DECIMAL = 4;
        public static final int SINGLE_QUOTED_STRING = 8;
        public static final int DOUBLE_QUOTED_STRING = 16;
        public static final int COMMENT = 32;
        private final boolean useComments;

        protected BasicTokenizer(boolean useComments) {
            this.useComments = useComments;
        }

        public void tokenize(CharacterStream input, Tokens tokens) throws ParsingException {
            while (input.hasNext()) {
                char c = input.next();
                int var10000;
                int startIndex;
                Position startingPosition;
                boolean foundClosingQuote;
                int endIndex;
                switch (c) {
                    case '\t':
                    case '\n':
                    case '\r':
                    case ' ':
                        break;
                    case '\u000b':
                    case '\f':
                    case '\u000e':
                    case '\u000f':
                    case '\u0010':
                    case '\u0011':
                    case '\u0012':
                    case '\u0013':
                    case '\u0014':
                    case '\u0015':
                    case '\u0016':
                    case '\u0017':
                    case '\u0018':
                    case '\u0019':
                    case '\u001a':
                    case '\u001b':
                    case '\u001c':
                    case '\u001d':
                    case '\u001e':
                    case '\u001f':
                    case '#':
                    case '&':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '@':
                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case '\\':
                    case '^':
                    case '_':
                    case '`':
                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'g':
                    case 'h':
                    case 'i':
                    case 'j':
                    case 'k':
                    case 'l':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'r':
                    case 's':
                    case 't':
                    case 'u':
                    case 'v':
                    case 'w':
                    case 'x':
                    case 'y':
                    case 'z':
                    default:
                        startIndex = input.index();

                        for (startingPosition = input.position(startIndex); input.hasNext() && !input.isNextWhitespace() && !input.isNextAnyOf("/.-(){}*,;+%?$[]!<>|=:"); c = input.next()) {
                        }

                        endIndex = input.index() + 1;
                        tokens.addToken(startingPosition, startIndex, endIndex, 1);
                        break;
                    case '!':
                    case '$':
                    case '%':
                    case '(':
                    case ')':
                    case '*':
                    case '+':
                    case ',':
                    case '-':
                    case ':':
                    case ';':
                    case '<':
                    case '=':
                    case '>':
                    case '?':
                    case '[':
                    case ']':
                    case '{':
                    case '|':
                    case '}':
                        tokens.addToken(input.position(input.index()), input.index(), input.index() + 1, 2);
                        break;
                    case '"':
                        startIndex = input.index();
                        startingPosition = input.position(startIndex);
                        foundClosingQuote = false;

                        while (input.hasNext()) {
                            c = input.next();
                            if (c == '\\' && input.isNext('"')) {
                                c = input.next();
                            } else if (c == '"' && input.isNext('"')) {
                                c = input.next();
                            } else if (c == '"') {
                                foundClosingQuote = true;
                                break;
                            }
                        }

                        if (!foundClosingQuote) {
                            var10000 = startingPosition.line();
                            String msg = "No matching double quote found at line " + var10000 + ", column " + startingPosition.column();
                            throw new ParsingException(startingPosition, msg);
                        }

                        endIndex = input.index() + 1;
                        tokens.addToken(startingPosition, startIndex, endIndex, 16);
                        break;
                    case '\'':
                        startIndex = input.index();
                        startingPosition = input.position(startIndex);
                        foundClosingQuote = false;

                        while (input.hasNext()) {
                            c = input.next();
                            if (c == '\\' && input.isNext('\'')) {
                                c = input.next();
                            } else if (c == '\'' && input.isNext('\'')) {
                                c = input.next();
                            } else if (c == '\'') {
                                foundClosingQuote = true;
                                break;
                            }
                        }

                        if (!foundClosingQuote) {
                            var10000 = startingPosition.line();
                            String msg = "No matching single quote found at line " + var10000 + ", column " + startingPosition.column();
                            throw new ParsingException(startingPosition, msg);
                        }

                        endIndex = input.index() + 1;
                        tokens.addToken(startingPosition, startIndex, endIndex, 8);
                        break;
                    case '.':
                        tokens.addToken(input.position(input.index()), input.index(), input.index() + 1, 4);
                        break;
                    case '/':
                        startIndex = input.index();
                        startingPosition = input.position(startIndex);
                        if (input.isNext('/')) {
                            boolean foundLineTerminator = false;

                            while (input.hasNext()) {
                                c = input.next();
                                if (c == '\n' || c == '\r') {
                                    foundLineTerminator = true;
                                    break;
                                }
                            }

                            endIndex = input.index();
                            if (!foundLineTerminator) {
                                ++endIndex;
                            }

                            if (c == '\r' && input.isNext('\n')) {
                                input.next();
                            }

                            if (this.useComments) {
                                tokens.addToken(startingPosition, startIndex, endIndex, 32);
                            }
                        } else if (!input.isNext('*')) {
                            tokens.addToken(startingPosition, startIndex, startIndex + 1, 2);
                        } else {
                            while (input.hasNext() && !input.isNext('*', '/')) {
                                c = input.next();
                            }

                            if (input.hasNext()) {
                                input.next();
                            }

                            if (input.hasNext()) {
                                input.next();
                            }

                            if (this.useComments) {
                                endIndex = input.index() + 1;
                                tokens.addToken(startingPosition, startIndex, endIndex, 32);
                            }
                        }
                }
            }

        }
    }

    @Immutable
    protected class CaseInsensitiveToken extends CaseSensitiveToken {
        public CaseInsensitiveToken(int startIndex, int endIndex, int type, Position position) {
            super(startIndex, endIndex, type, position);
        }

        public boolean matches(String expected) {
            return this.matchString().substring(this.startIndex(), this.endIndex()).toUpperCase().equals(expected);
        }

        public Token withType(int typeMask) {
            int type = this.type() | typeMask;
            return TokenStream.this.new CaseInsensitiveToken(this.startIndex(), this.endIndex(), type, this.position());
        }
    }

    @Immutable
    protected class CaseSensitiveToken implements Token {
        private final int startIndex;
        private final int endIndex;
        private final int type;
        private final Position position;

        public CaseSensitiveToken(int startIndex, int endIndex, int type, Position position) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.type = type;
            this.position = position;
        }

        public Token withType(int typeMask) {
            int type = this.type | typeMask;
            return TokenStream.this.new CaseSensitiveToken(this.startIndex, this.endIndex, type, this.position);
        }

        public final int type() {
            return this.type;
        }

        public final int startIndex() {
            return this.startIndex;
        }

        public final int endIndex() {
            return this.endIndex;
        }

        public final int length() {
            return this.endIndex - this.startIndex;
        }

        public final boolean matches(char expected) {
            return this.length() == 1 && this.matchString().charAt(this.startIndex) == expected;
        }

        public boolean matches(String expected) {
            return this.matchString().substring(this.startIndex, this.endIndex).equals(expected);
        }

        public final boolean matches(int expectedType) {
            return expectedType == Integer.MIN_VALUE || (TokenStream.this.currentToken().type() & expectedType) == expectedType;
        }

        public final String value() {
            return TokenStream.this.inputString.substring(this.startIndex, this.endIndex);
        }

        public Position position() {
            return this.position;
        }

        protected String matchString() {
            return TokenStream.this.inputString;
        }

        public String toString() {
            return this.value();
        }
    }
}
