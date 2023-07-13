package io.debezium.relational;

import io.debezium.annotation.Immutable;
import io.debezium.function.Predicates;

import java.util.function.Predicate;

@Immutable
public class Selectors {
    public static DatabaseSelectionPredicateBuilder databaseSelector() {
        return new DatabaseSelectionPredicateBuilder();
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static TableSelectionPredicateBuilder tableSelector() {
        return new TableSelectionPredicateBuilder();
    }

    public static class DatabaseSelectionPredicateBuilder {
        private Predicate<String> dbInclusions;
        private Predicate<String> dbExclusions;

        public DatabaseSelectionPredicateBuilder includeDatabases(String databaseNames) {
            if (databaseNames != null && !databaseNames.trim().isEmpty()) {
                this.dbInclusions = Predicates.includes(databaseNames);
            } else {
                this.dbInclusions = null;
            }

            return this;
        }

        public DatabaseSelectionPredicateBuilder excludeDatabases(String databaseNames) {
            if (databaseNames != null && !databaseNames.trim().isEmpty()) {
                this.dbExclusions = Predicates.excludes(databaseNames);
            } else {
                this.dbExclusions = null;
            }

            return this;
        }

        public Predicate<String> build() {
            Predicate<String> dbFilter = this.dbInclusions != null ? this.dbInclusions : this.dbExclusions;
            return dbFilter != null ? dbFilter : (id) -> {
                return true;
            };
        }
    }

    public static class TableSelectionPredicateBuilder {
        private Predicate<String> dbInclusions;
        private Predicate<String> dbExclusions;
        private Predicate<String> schemaInclusions;
        private Predicate<String> schemaExclusions;
        private Predicate<TableId> tableInclusions;
        private Predicate<TableId> tableExclusions;

        public TableSelectionPredicateBuilder includeDatabases(String databaseNames) {
            if (Selectors.isEmpty(databaseNames)) {
                this.dbInclusions = null;
            } else {
                this.dbInclusions = Predicates.includes(databaseNames);
            }

            return this;
        }

        public TableSelectionPredicateBuilder excludeDatabases(String databaseNames) {
            if (Selectors.isEmpty(databaseNames)) {
                this.dbExclusions = null;
            } else {
                this.dbExclusions = Predicates.excludes(databaseNames);
            }

            return this;
        }

        public TableSelectionPredicateBuilder includeSchemas(String schemaNames) {
            if (Selectors.isEmpty(schemaNames)) {
                this.schemaInclusions = null;
            } else {
                this.schemaInclusions = Predicates.includes(schemaNames);
            }

            return this;
        }

        public TableSelectionPredicateBuilder excludeSchemas(String schemaNames) {
            if (Selectors.isEmpty(schemaNames)) {
                this.schemaExclusions = null;
            } else {
                this.schemaExclusions = Predicates.excludes(schemaNames);
            }

            return this;
        }

        public TableSelectionPredicateBuilder includeTables(String fullyQualifiedTableNames, TableIdToStringMapper tableIdMapper) {
            if (Selectors.isEmpty(fullyQualifiedTableNames)) {
                this.tableInclusions = null;
            } else {
                this.tableInclusions = Predicates.includes(fullyQualifiedTableNames, (tableId) -> {
                    return tableIdMapper.toString(tableId);
                });
            }

            return this;
        }

        public TableSelectionPredicateBuilder includeTables(String fullyQualifiedTableNames) {
            return this.includeTables(fullyQualifiedTableNames, TableId::toString);
        }

        public TableSelectionPredicateBuilder excludeTables(String fullyQualifiedTableNames, TableIdToStringMapper tableIdMapper) {
            if (Selectors.isEmpty(fullyQualifiedTableNames)) {
                this.tableExclusions = null;
            } else {
                this.tableExclusions = Predicates.excludes(fullyQualifiedTableNames, (tableId) -> {
                    return tableIdMapper.toString(tableId);
                });
            }

            return this;
        }

        public TableSelectionPredicateBuilder excludeTables(String fullyQualifiedTableNames) {
            return this.excludeTables(fullyQualifiedTableNames, TableId::toString);
        }

        public Predicate<TableId> build() {
            Predicate<TableId> tableFilter = this.tableInclusions != null ? this.tableInclusions : this.tableExclusions;
            Predicate<String> dbFilter = this.dbInclusions != null ? this.dbInclusions : this.dbExclusions;
            Predicate<String> schemaFilter = this.schemaInclusions != null ? this.schemaInclusions : this.schemaExclusions;
            if (dbFilter != null) {
                return this.buildStartingFromDbFilter(dbFilter, schemaFilter, tableFilter);
            } else if (schemaFilter != null) {
                return this.buildStartingFromSchemaFilter(schemaFilter, tableFilter);
            } else {
                return tableFilter != null ? tableFilter : (id) -> {
                    return true;
                };
            }
        }

        private Predicate<TableId> buildStartingFromSchemaFilter(Predicate<String> schemaFilter, Predicate<TableId> tableFilter) {
            assert schemaFilter != null;

            return tableFilter != null ? (id) -> {
                return schemaFilter.test(id.schema()) && tableFilter.test(id);
            } : (id) -> {
                return schemaFilter.test(id.schema());
            };
        }

        private Predicate<TableId> buildStartingFromDbFilter(Predicate<String> dbFilter, Predicate<String> schemaFilter, Predicate<TableId> tableFilter) {
            assert dbFilter != null;

            if (schemaFilter != null) {
                return tableFilter != null ? (id) -> {
                    return dbFilter.test(id.catalog()) && schemaFilter.test(id.schema()) && tableFilter.test(id);
                } : (id) -> {
                    return schemaFilter.test(id.schema());
                };
            } else {
                return tableFilter != null ? (id) -> {
                    return dbFilter.test(id.catalog()) && tableFilter.test(id);
                } : (id) -> {
                    return dbFilter.test(id.catalog());
                };
            }
        }
    }

    @FunctionalInterface
    public interface TableIdToStringMapper {
        String toString(TableId var1);
    }
}
