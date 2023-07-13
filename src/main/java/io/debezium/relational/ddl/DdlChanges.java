package io.debezium.relational.ddl;

import io.debezium.annotation.NotThreadSafe;
import io.debezium.relational.RelationalTableFilters;
import io.debezium.relational.TableId;
import io.debezium.relational.Tables;

import java.util.*;
import java.util.function.Predicate;

@NotThreadSafe
public class DdlChanges implements DdlParserListener {
    protected final List<Event> events = new ArrayList();
    private final Set<String> databaseNames = new HashSet();

    public DdlChanges reset() {
        this.events.clear();
        this.databaseNames.clear();
        return this;
    }

    public void handle(Event event) {
        this.events.add(event);
        this.databaseNames.add(this.getDatabase(event));
    }

    public void getEventsByDatabase(DatabaseEventConsumer consumer) {
        if (!this.isEmpty()) {
            if (this.databaseNames.size() <= 1) {
                consumer.consume((String) this.databaseNames.iterator().next(), this.events);
            } else {
                List<Event> dbEvents = new ArrayList();
                String currentDatabase = null;
                Iterator var4 = this.events.iterator();

                while (true) {
                    while (var4.hasNext()) {
                        Event event = (Event) var4.next();
                        String dbName = this.getDatabase(event);
                        if (currentDatabase != null && !dbName.equals(currentDatabase)) {
                            consumer.consume(currentDatabase, dbEvents);
                            dbEvents = new ArrayList();
                            currentDatabase = dbName;
                            dbEvents.add(event);
                        } else {
                            currentDatabase = dbName;
                            dbEvents.add(event);
                        }
                    }

                    if (!dbEvents.isEmpty()) {
                        consumer.consume(currentDatabase, dbEvents);
                    }

                    return;
                }
            }
        }
    }

    protected String getDatabase(Event event) {
        switch (event.type()) {
            case CREATE_TABLE:
            case ALTER_TABLE:
            case DROP_TABLE:
            case TRUNCATE_TABLE:
                TableEvent tableEvent = (TableEvent) event;
                return tableEvent.tableId().catalog();
            case CREATE_INDEX:
            case DROP_INDEX:
                TableIndexEvent tableIndexEvent = (TableIndexEvent) event;
                return tableIndexEvent.tableId().catalog();
            case CREATE_DATABASE:
            case ALTER_DATABASE:
            case DROP_DATABASE:
            case USE_DATABASE:
                DatabaseEvent dbEvent = (DatabaseEvent) event;
                return dbEvent.databaseName();
            case SET_VARIABLE:
                SetVariableEvent varEvent = (SetVariableEvent) event;
                return (String) varEvent.databaseName().orElse("");
            default:
                assert false : "Should never happen";

                return null;
        }
    }

    public boolean isEmpty() {
        return this.events.isEmpty();
    }

    public String toString() {
        return this.events.toString();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean anyMatch(Predicate<String> databaseFilter, Predicate<TableId> tableFilter) {
        return this.events.stream().anyMatch((event) -> {
            return event instanceof DatabaseEvent && databaseFilter.test(((DatabaseEvent) event).databaseName()) || event instanceof TableEvent && tableFilter.test(((TableEvent) event).tableId()) || event instanceof SetVariableEvent && (!((SetVariableEvent) event).databaseName().isPresent() || databaseFilter.test((String) ((SetVariableEvent) event).databaseName().get()));
        });
    }

    public boolean anyMatch(RelationalTableFilters filters) {
        Predicate<String> databaseFilter = filters.databaseFilter();
        Tables.TableFilter tableFilter = filters.dataCollectionFilter();
        return this.events.stream().anyMatch((event) -> {
            return event instanceof DatabaseEvent && databaseFilter.test(((DatabaseEvent) event).databaseName()) || event instanceof TableEvent && tableFilter.isIncluded(((TableEvent) event).tableId()) || event instanceof SetVariableEvent && (!((SetVariableEvent) event).databaseName().isPresent() || databaseFilter.test((String) ((SetVariableEvent) event).databaseName().get()));
        });
    }

    public interface DatabaseEventConsumer {
        void consume(String var1, List<Event> var2);
    }
}
