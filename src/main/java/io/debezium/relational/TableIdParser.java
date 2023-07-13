package io.debezium.relational;

import io.debezium.text.ParsingException;
import io.debezium.text.TokenStream;

import java.util.ArrayList;
import java.util.List;

class TableIdParser {
    private static final char SEPARATOR = '.';
    private static final String SINGLE_QUOTES = "''";
    private static final String DOUBLE_QUOTES = "\"\"";
    private static final String BACKTICKS = "``";

    public static List<String> parse(String identifier) {
        return parse(identifier, new TableIdPredicates() {
        });
    }

    public static List<String> parse(String identifier, TableIdPredicates predicates) {
        TokenStream stream = new TokenStream(identifier, new TableIdTokenizer(identifier, predicates), true);
        stream.start();
        List<String> parts = new ArrayList(3);

        while (stream.hasNext()) {
            parts.add(stream.consume().replace("''", "'").replace("\"\"", "\"").replace("``", "`"));
        }

        return parts;
    }

    private static class TableIdTokenizer implements TokenStream.Tokenizer {
        private final String identifier;
        private final TableIdPredicates predicates;

        TableIdTokenizer(String identifier, TableIdPredicates predicates) {
            this.identifier = identifier;
            this.predicates = predicates;
        }

        public void tokenize(TokenStream.CharacterStream input, TokenStream.Tokens tokens) throws ParsingException {
            ParsingState previousState = null;
            ParsingState currentState = ParsingState.INITIAL;
            ParsingContext parsingContext = new ParsingContext(input, tokens, this.predicates);
            currentState.onEntry(parsingContext);

            while (input.hasNext()) {
                previousState = currentState;
                currentState = currentState.handleCharacter(input.next(), parsingContext);
                if (currentState != previousState) {
                    previousState.onExit(parsingContext);
                    currentState.onEntry(parsingContext);
                }
            }

            currentState.onExit(parsingContext);
            if (currentState != ParsingState.BEFORE_SEPARATOR && currentState != ParsingState.IN_IDENTIFIER) {
                throw new IllegalArgumentException("Invalid identifier: " + this.identifier);
            }
        }
    }

    private static class ParsingContext {
        final TokenStream.CharacterStream input;
        final TokenStream.Tokens tokens;
        final TableIdPredicates predicates;
        int startOfLastToken;
        int lastIdentifierEnd;
        boolean escaped;
        char quotingChar;

        ParsingContext(TokenStream.CharacterStream input, TokenStream.Tokens tokens, TableIdPredicates predicates) {
            this.input = input;
            this.tokens = tokens;
            this.predicates = predicates;
        }
    }

    private static enum ParsingState {
        INITIAL {
            ParsingState handleCharacter(char c, ParsingContext context) {
                if (Character.isWhitespace(c)) {
                    return INITIAL;
                } else if (c == '.') {
                    throw new IllegalArgumentException("Unexpected input: " + c);
                } else if (context.predicates.isQuotingChar(c)) {
                    context.quotingChar = c;
                    return IN_QUOTED_IDENTIFIER;
                } else {
                    return context.predicates.isStartDelimiter(c) ? IN_DELIMITED_IDENTIFIER : IN_IDENTIFIER;
                }
            }
        },
        IN_IDENTIFIER {
            void doOnEntry(ParsingContext context) {
                context.startOfLastToken = context.input.index();
                context.lastIdentifierEnd = context.input.index();
            }

            void doOnExit(ParsingContext context) {
                context.tokens.addToken(context.input.position(context.startOfLastToken), context.startOfLastToken, context.lastIdentifierEnd + 1);
            }

            ParsingState handleCharacter(char c, ParsingContext context) {
                if (Character.isWhitespace(c)) {
                    return BEFORE_SEPARATOR;
                } else if (c == '.') {
                    return AFTER_SEPARATOR;
                } else {
                    context.lastIdentifierEnd = context.input.index();
                    return IN_IDENTIFIER;
                }
            }
        },
        BEFORE_SEPARATOR {
            ParsingState handleCharacter(char c, ParsingContext context) {
                if (Character.isWhitespace(c)) {
                    return BEFORE_SEPARATOR;
                } else if (c == '.') {
                    return AFTER_SEPARATOR;
                } else {
                    throw new IllegalArgumentException("Unexpected input: " + c);
                }
            }
        },
        AFTER_SEPARATOR {
            ParsingState handleCharacter(char c, ParsingContext context) {
                if (Character.isWhitespace(c)) {
                    return AFTER_SEPARATOR;
                } else if (c == '.') {
                    throw new IllegalArgumentException("Unexpected input: " + c);
                } else if (context.predicates.isQuotingChar(c)) {
                    context.quotingChar = c;
                    return IN_QUOTED_IDENTIFIER;
                } else {
                    return context.predicates.isStartDelimiter(c) ? IN_DELIMITED_IDENTIFIER : IN_IDENTIFIER;
                }
            }
        },
        IN_QUOTED_IDENTIFIER {
            ParsingState handleCharacter(char c, ParsingContext context) {
                if (c == context.quotingChar) {
                    if (context.escaped) {
                        context.escaped = false;
                        return IN_QUOTED_IDENTIFIER;
                    } else if (context.input.isNext(context.quotingChar)) {
                        context.escaped = true;
                        return IN_QUOTED_IDENTIFIER;
                    } else {
                        context.lastIdentifierEnd = context.input.index();
                        return BEFORE_SEPARATOR;
                    }
                } else {
                    return IN_QUOTED_IDENTIFIER;
                }
            }

            void doOnEntry(ParsingContext context) {
                context.startOfLastToken = context.input.index();
            }

            void doOnExit(ParsingContext context) {
                context.quotingChar = 0;
                context.tokens.addToken(context.input.position(context.startOfLastToken + 1), context.startOfLastToken + 1, context.lastIdentifierEnd);
            }
        },
        IN_DELIMITED_IDENTIFIER {
            ParsingState handleCharacter(char c, ParsingContext context) {
                if (context.predicates.isEndDelimiter(c)) {
                    context.lastIdentifierEnd = context.input.index();
                    return BEFORE_SEPARATOR;
                } else {
                    return IN_DELIMITED_IDENTIFIER;
                }
            }

            void doOnEntry(ParsingContext context) {
                context.startOfLastToken = context.input.index();
            }

            void doOnExit(ParsingContext context) {
                context.tokens.addToken(context.input.position(context.startOfLastToken + 1), context.startOfLastToken + 1, context.lastIdentifierEnd);
            }
        };

        abstract ParsingState handleCharacter(char var1, ParsingContext var2);

        void onEntry(ParsingContext context) {
            this.doOnEntry(context);
        }

        void doOnEntry(ParsingContext context) {
        }

        void onExit(ParsingContext context) {
            this.doOnExit(context);
        }

        void doOnExit(ParsingContext context) {
        }

        // $FF: synthetic method
        private static ParsingState[] $values() {
            return new ParsingState[]{INITIAL, IN_IDENTIFIER, BEFORE_SEPARATOR, AFTER_SEPARATOR, IN_QUOTED_IDENTIFIER, IN_DELIMITED_IDENTIFIER};
        }
    }
}
