package io.debezium.schema;

import io.debezium.annotation.ThreadSafe;
import io.debezium.spi.common.ReplacementFunction;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@FunctionalInterface
@ThreadSafe
public interface SchemaNameAdjuster {
    Logger LOGGER = LoggerFactory.getLogger(SchemaNameAdjuster.class);
    SchemaNameAdjuster NO_OP = (proposedName) -> {
        return proposedName;
    };
    SchemaNameAdjuster AVRO = create();
    SchemaNameAdjuster AVRO_UNICODE = create(new UnicodeReplacementFunction());
    SchemaNameAdjuster AVRO_FIELD_NAMER = create(new FieldNameUnderscoreReplacementFunction());
    SchemaNameAdjuster AVRO_UNICODE_FIELD_NAMER = create(new FieldNameUnicodeReplacementFunction());

    String adjust(String var1);

    static SchemaNameAdjuster create() {
        return create(ReplacementFunction.UNDERSCORE_REPLACEMENT);
    }

    static SchemaNameAdjuster create(ReplacementFunction function) {
        return create(function, (original, replacement, conflict) -> {
            String msg = "The Kafka Connect schema name '" + original + "' is not a valid Avro schema name and its replacement '" + replacement + "' conflicts with another different schema '" + conflict + "'";
            throw new ConnectException(msg);
        });
    }

    static SchemaNameAdjuster create(ReplacementFunction function, ReplacementOccurred uponConflict) {
        ReplacementOccurred handler = (original, replacement, conflictsWith) -> {
            if (conflictsWith != null) {
                LOGGER.error("The Kafka Connect schema name '{}' is not a valid Avro schema name and its replacement '{}' conflicts with another different schema '{}'", new Object[]{original, replacement, conflictsWith});
                if (uponConflict != null) {
                    uponConflict.accept(original, replacement, conflictsWith);
                }
            } else {
                LOGGER.warn("The Kafka Connect schema name '{}' is not a valid Avro schema name, so replacing with '{}'", original, replacement);
            }

        };
        return (original) -> {
            return validFullname(original, function, handler.firstTimeOnly());
        };
    }

    static SchemaNameAdjuster create(char replacement, ReplacementOccurred uponReplacement) {
        String replacementStr = "" + replacement;
        return (original) -> {
            return validFullname(original, (c) -> {
                return replacementStr;
            }, uponReplacement);
        };
    }

    static SchemaNameAdjuster create(String replacement, ReplacementOccurred uponReplacement) {
        return (original) -> {
            return validFullname(original, (c) -> {
                return replacement;
            }, uponReplacement);
        };
    }

    static boolean isValidFullname(String fullname) {
        if (fullname.length() == 0) {
            return true;
        } else {
            char c = fullname.charAt(0);
            if (!ReplacementFunction.UNDERSCORE_REPLACEMENT.isValidFirstCharacter(c)) {
                return false;
            } else {
                for (int i = 1; i != fullname.length(); ++i) {
                    c = fullname.charAt(i);
                    if (!ReplacementFunction.UNDERSCORE_REPLACEMENT.isValidNonFirstCharacter(c)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    static String validFullname(String proposedName) {
        return validFullname(proposedName, "_");
    }

    static String validFullname(String proposedName, String replacement) {
        return validFullname(proposedName, (c) -> {
            return replacement;
        });
    }

    static String validFullname(String proposedName, ReplacementFunction replacement) {
        return validFullname(proposedName, replacement, (ReplacementOccurred) null);
    }

    static String validFullname(String proposedName, ReplacementFunction replacement, ReplacementOccurred uponReplacement) {
        if (proposedName.length() == 0) {
            return proposedName;
        } else {
            StringBuilder sb = new StringBuilder();
            char c = proposedName.charAt(0);
            boolean changed = false;
            if (replacement.isValidFirstCharacter(c)) {
                sb.append(c);
            } else {
                sb.append(replacement.replace(c));
                if (Character.isDigit(c) && (replacement == ReplacementFunction.UNDERSCORE_REPLACEMENT || replacement instanceof FieldNameUnderscoreReplacementFunction)) {
                    sb.append(c);
                }

                changed = true;
            }

            for (int i = 1; i != proposedName.length(); ++i) {
                c = proposedName.charAt(i);
                if (replacement.isValidNonFirstCharacter(c)) {
                    sb.append(c);
                } else {
                    sb.append(replacement.replace(c));
                    changed = true;
                }
            }

            if (!changed) {
                return proposedName;
            } else {
                String result = sb.toString();
                if (uponReplacement != null) {
                    uponReplacement.accept(proposedName, result, (String) null);
                }

                return result;
            }
        }
    }

    @FunctionalInterface
    @ThreadSafe
    public interface ReplacementOccurred {
        void accept(String var1, String var2, String var3);

        default ReplacementOccurred firstTimeOnly() {
            Set<String> alreadySeen = Collections.newSetFromMap(new ConcurrentHashMap());
            Map<String, String> originalByReplacement = new ConcurrentHashMap();
            return (original, replacement, conflictsWith) -> {
                if (alreadySeen.add(original)) {
                    String replacementsOriginal = (String) originalByReplacement.put(replacement, original);
                    if (replacementsOriginal != null && !original.equals(replacementsOriginal)) {
                        this.accept(original, replacement, replacementsOriginal);
                    } else {
                        this.accept(original, replacement, (String) null);
                    }
                }

            };
        }

        default ReplacementOccurred andThen(ReplacementOccurred next) {
            return next == null ? this : (original, replacement, conflictsWith) -> {
                this.accept(original, replacement, conflictsWith);
                next.accept(original, replacement, conflictsWith);
            };
        }
    }
}
