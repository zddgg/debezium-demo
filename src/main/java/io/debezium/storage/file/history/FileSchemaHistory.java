package io.debezium.storage.file.history;

import io.debezium.DebeziumException;
import io.debezium.annotation.ThreadSafe;
import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.relational.history.*;
import io.debezium.util.Collect;
import io.debezium.util.Loggings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.Objects;

@ThreadSafe
public final class FileSchemaHistory extends AbstractFileBasedSchemaHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSchemaHistory.class);
    public static final Field FILE_PATH = Field.create("schema.history.internal.file.filename").withDescription("The path to the file that will be used to record the database schema history").required();
    public static Collection<Field> ALL_FIELDS;
    private Path path;

    public void configure(Configuration config, HistoryRecordComparator comparator, SchemaHistoryListener listener, boolean useCatalogBeforeSchema) {
        Collection var10001 = ALL_FIELDS;
        Logger var10002 = this.logger;
        Objects.requireNonNull(var10002);
        if (!config.validateAndRecord(var10001, var10002::error)) {
            throw new DebeziumException("Error configuring an instance of " + this.getClass().getSimpleName() + "; check the logs for details");
        } else if (this.running.get()) {
            throw new SchemaHistoryException("Database schema history file already initialized to " + this.path);
        } else {
            super.configure(config, comparator, listener, useCatalogBeforeSchema);
            this.path = Paths.get(config.getString(FILE_PATH));
        }
    }

    protected void doStoreRecord(HistoryRecord record) {
        try {
            LOGGER.trace("Storing record into database history: {}", record);
            this.records.add(record);
            String line = this.documentWriter.write(record.document());

            try {
                BufferedWriter historyWriter = Files.newBufferedWriter(this.path, StandardOpenOption.APPEND);

                try {
                    try {
                        historyWriter.append(line);
                        historyWriter.newLine();
                    } catch (IOException var7) {
                        Loggings.logErrorAndTraceRecord(this.logger, record, "Failed to add record to history at {}", new Object[]{this.path, var7});
                    }
                } catch (Throwable var8) {
                    if (historyWriter != null) {
                        try {
                            historyWriter.close();
                        } catch (Throwable var6) {
                            var8.addSuppressed(var6);
                        }
                    }

                    throw var8;
                }

                if (historyWriter != null) {
                    historyWriter.close();
                }
            } catch (IOException var9) {
                throw new SchemaHistoryException("Unable to create writer for history file " + this.path + ": " + var9.getMessage(), var9);
            }
        } catch (IOException var10) {
            Loggings.logErrorAndTraceRecord(this.logger, record, "Failed to convert record to string", var10);
        }

    }

    protected void doStart() {
        try {
            this.toHistoryRecord(Files.newInputStream(this.path));
        } catch (IOException var2) {
            throw new SchemaHistoryException("Can't retrieve file with schema history", var2);
        }
    }

    public boolean storageExists() {
        return Files.exists(this.path, new LinkOption[0]);
    }

    public boolean exists() {
        boolean exists = false;
        if (this.storageExists()) {
            try {
                if (Files.size(this.path) > 0L) {
                    exists = true;
                }
            } catch (IOException var3) {
                this.logger.error("Unable to determine if history file empty " + this.path + ": " + var3.getMessage(), var3);
            }
        }

        return exists;
    }

    public void initializeStorage() {
        try {
            if (this.path.getParent() != null && !Files.exists(this.path.getParent(), new LinkOption[0])) {
                Files.createDirectories(this.path.getParent());
            }

            try {
                Files.createFile(this.path);
            } catch (FileAlreadyExistsException var2) {
            }

        } catch (IOException var3) {
            throw new SchemaHistoryException("Unable to create history file at " + this.path + ": " + var3.getMessage(), var3);
        }
    }

    public String toString() {
        Object var10000 = this.path != null ? this.path : "(unstarted)";
        return "file " + var10000;
    }

    static {
        ALL_FIELDS = Collect.arrayListOf(FILE_PATH, new Field[0]);
    }
}
