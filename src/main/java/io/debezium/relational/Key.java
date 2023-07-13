package io.debezium.relational;

import io.debezium.annotation.Immutable;
import io.debezium.function.Predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Immutable
public class Key {
    private final Table table;
    private final KeyMapper keyMapper;

    private Key(Table table, KeyMapper keyMapper) {
        this.table = table;
        this.keyMapper = keyMapper;
    }

    public List<Column> keyColumns() {
        return this.keyMapper.getKeyKolumns(this.table);
    }

    @FunctionalInterface
    public interface KeyMapper {
        List<Column> getKeyKolumns(Table var1);
    }

    public static class CustomKeyMapper {
        public static final Pattern MSG_KEY_COLUMNS_PATTERN = Pattern.compile("^\\s*([^\\s:]+):([^:\\s]+)\\s*$");
        public static final Pattern PATTERN_SPLIT = Pattern.compile(";");
        private static final Pattern TABLE_SPLIT = Pattern.compile(":");
        private static final Pattern COLUMN_SPLIT = Pattern.compile(",");

        public static KeyMapper getInstance(String fullyQualifiedColumnNames, Selectors.TableIdToStringMapper tableIdMapper) {
            if (fullyQualifiedColumnNames == null) {
                return null;
            } else {
                String regexes = (String) ((ArrayList) Arrays.stream(PATTERN_SPLIT.split(fullyQualifiedColumnNames)).map((s) -> {
                    return TABLE_SPLIT.split(s);
                }).collect(ArrayList::new, (m, p) -> {
                    Arrays.asList(COLUMN_SPLIT.split(p[1])).forEach((c) -> {
                        m.add(p[0] + "." + c);
                    });
                }, ArrayList::addAll)).stream().collect(Collectors.joining(","));
                Predicate<ColumnId> delegate = Predicates.includes(regexes, ColumnId::toString);
                return (table) -> {
                    List<Column> candidates = (List) table.columns().stream().filter((c) -> {
                        TableId tableId = table.id();
                        if (tableIdMapper == null) {
                            return delegate.test(new ColumnId(tableId.catalog(), tableId.schema(), tableId.table(), c.name()));
                        } else {
                            return delegate.test(new ColumnId(tableId.catalog(), tableId.schema(), tableId.table(), c.name())) || delegate.test(new ColumnId(new TableId(tableId.catalog(), tableId.schema(), tableId.table(), tableIdMapper), c.name()));
                        }
                    }).collect(Collectors.toList());
                    return candidates.isEmpty() ? table.primaryKeyColumns() : candidates;
                };
            }
        }
    }

    private static class IdentityKeyMapper {
        public static KeyMapper getInstance() {
            return (table) -> {
                return table.primaryKeyColumns();
            };
        }
    }

    public static class Builder {
        private final Table table;
        private KeyMapper keyMapper = IdentityKeyMapper.getInstance();

        public Builder(Table table) {
            this.table = table;
        }

        public Builder customKeyMapper(KeyMapper customKeyMapper) {
            if (customKeyMapper != null) {
                this.keyMapper = customKeyMapper;
            }

            return this;
        }

        public Key build() {
            return new Key(this.table, this.keyMapper);
        }
    }
}
