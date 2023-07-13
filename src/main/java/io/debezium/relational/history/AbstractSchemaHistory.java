package io.debezium.relational.history;

import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.document.Array;
import io.debezium.document.Document;
import io.debezium.function.Predicates;
import io.debezium.relational.Tables;
import io.debezium.relational.ddl.DdlParser;
import io.debezium.text.MultipleParsingExceptions;
import io.debezium.text.ParsingException;
import io.debezium.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractSchemaHistory implements SchemaHistory {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static Field.Set ALL_FIELDS;
    protected Configuration config;
    private HistoryRecordComparator comparator;
    private boolean skipUnparseableDDL;
    private Predicate<String> ddlFilter;
    private SchemaHistoryListener listener;
    private boolean useCatalogBeforeSchema;
    private boolean preferDdl;
    private final TableChanges.TableChangesSerializer<Array> tableChangesSerializer;

    protected AbstractSchemaHistory() {
        this.comparator = HistoryRecordComparator.INSTANCE;
        this.ddlFilter = (x) -> {
            return false;
        };
        this.listener = SchemaHistoryListener.NOOP;
        this.preferDdl = false;
        this.tableChangesSerializer = new JsonTableChangeSerializer();
    }

    public void configure(Configuration config, HistoryRecordComparator comparator, SchemaHistoryListener listener, boolean useCatalogBeforeSchema) {
        this.config = config;
        this.comparator = comparator != null ? comparator : HistoryRecordComparator.INSTANCE;
        this.skipUnparseableDDL = config.getBoolean(SKIP_UNPARSEABLE_DDL_STATEMENTS);
        String ddlFilter = config.getString(DDL_FILTER);
        this.ddlFilter = ddlFilter != null ? Predicates.includes(ddlFilter, 34) : (x) -> {
            return false;
        };
        this.listener = listener;
        this.useCatalogBeforeSchema = useCatalogBeforeSchema;
        this.preferDdl = config.getBoolean(INTERNAL_PREFER_DDL);
    }

    public void start() {
        this.listener.started();
    }

    public final void record(Map<String, ?> source, Map<String, ?> position, String databaseName, String ddl) throws SchemaHistoryException {
        this.record(source, position, databaseName, (String) null, ddl, (TableChanges) null, Clock.SYSTEM.currentTimeAsInstant());
    }

    public final void record(Map<String, ?> source, Map<String, ?> position, String databaseName, String schemaName, String ddl, TableChanges changes, Instant timestamp) throws SchemaHistoryException {
        HistoryRecord record = new HistoryRecord(source, position, databaseName, schemaName, ddl, changes, timestamp);
        this.storeRecord(record);
        this.listener.onChangeApplied(record);
    }

    public void recover(Map<Map<String, ?>, Map<String, ?>> offsets, Tables schema, DdlParser ddlParser) {
        this.listener.recoveryStarted();
        Map<Document, HistoryRecord> stopPoints = new HashMap();
        offsets.forEach((source, position) -> {
            Document srcDocument = Document.create();
            if (source != null) {
                Objects.requireNonNull(srcDocument);
                source.forEach(srcDocument::set);
            }

            stopPoints.put(srcDocument, new HistoryRecord(source, position, (String) null, (String) null, (String) null, (TableChanges) null, (Instant) null));
        });
        this.recoverRecords((recovered) -> {
            this.listener.onChangeFromHistory(recovered);
            Document srcDocument = recovered.document().getDocument("source");
            if (stopPoints.containsKey(srcDocument) && this.comparator.isAtOrBefore(recovered, (HistoryRecord) stopPoints.get(srcDocument))) {
                Array tableChanges = recovered.tableChanges();
                String ddl = recovered.ddl();
                if (!this.preferDdl && tableChanges != null && !tableChanges.isEmpty()) {
                    TableChanges changes = this.tableChangesSerializer.deserialize(tableChanges, this.useCatalogBeforeSchema);
                    Iterator var9 = changes.iterator();

                    while (var9.hasNext()) {
                        TableChanges.TableChange entry = (TableChanges.TableChange) var9.next();
                        if (entry.getType() == TableChanges.TableChangeType.CREATE) {
                            schema.overwriteTable(entry.getTable());
                        } else if (entry.getType() == TableChanges.TableChangeType.ALTER) {
                            if (entry.getPreviousId() != null) {
                                schema.removeTable(entry.getPreviousId());
                            }

                            schema.overwriteTable(entry.getTable());
                        } else {
                            schema.removeTable(entry.getId());
                        }
                    }

                    this.listener.onChangeApplied(recovered);
                } else if (ddl != null && ddlParser != null) {
                    if (recovered.databaseName() != null) {
                        ddlParser.setCurrentDatabase(recovered.databaseName());
                    }

                    if (recovered.schemaName() != null) {
                        ddlParser.setCurrentSchema(recovered.schemaName());
                    }

                    if (this.ddlFilter.test(ddl)) {
                        this.logger.info("a DDL '{}' was filtered out of processing by regular expression '{}'", ddl, this.config.getString(DDL_FILTER));
                        return;
                    }

                    try {
                        this.logger.debug("Applying: {}", ddl);
                        ddlParser.parse(ddl, schema);
                        this.listener.onChangeApplied(recovered);
                    } catch (MultipleParsingExceptions | ParsingException var11) {
                        if (!this.skipUnparseableDDL) {
                            throw var11;
                        }

                        this.logger.warn("Ignoring unparseable statements '{}' stored in database schema history", ddl, var11);
                    }
                }
            } else {
                this.logger.debug("Skipping: {}", recovered.ddl());
            }

        });
        this.listener.recoveryStopped();
    }

    protected abstract void storeRecord(HistoryRecord var1) throws SchemaHistoryException;

    protected abstract void recoverRecords(Consumer<HistoryRecord> var1);

    public void stop() {
        this.listener.stopped();
    }

    public void initializeStorage() {
    }

    static {
        ALL_FIELDS = Field.setOf(NAME, INTERNAL_CONNECTOR_CLASS, INTERNAL_CONNECTOR_ID);
    }
}
