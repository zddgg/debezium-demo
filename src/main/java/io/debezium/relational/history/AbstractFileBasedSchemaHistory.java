package io.debezium.relational.history;

import io.debezium.document.DocumentReader;
import io.debezium.document.DocumentWriter;
import io.debezium.util.FunctionalReadWriteLock;
import io.debezium.util.Loggings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class AbstractFileBasedSchemaHistory extends AbstractSchemaHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileBasedSchemaHistory.class);
    protected final FunctionalReadWriteLock lock = FunctionalReadWriteLock.reentrant();
    protected final AtomicBoolean running = new AtomicBoolean();
    protected final DocumentWriter documentWriter = DocumentWriter.defaultWriter();
    protected final DocumentReader documentReader = DocumentReader.defaultReader();
    protected volatile List<HistoryRecord> records = new ArrayList();

    protected void toHistoryRecord(InputStream inputStream) {
        try {
            BufferedReader historyReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            try {
                while (true) {
                    String line = historyReader.readLine();
                    if (line == null) {
                        break;
                    }

                    if (!line.isEmpty()) {
                        this.records.add(new HistoryRecord(this.documentReader.read(line)));
                    }
                }
            } catch (Throwable var6) {
                try {
                    historyReader.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            historyReader.close();
        } catch (IOException var7) {
            throw new SchemaHistoryException("Unable to read object content", var7);
        }
    }

    protected byte[] fromHistoryRecord(HistoryRecord record) {
        LOGGER.trace("Storing record into database history: {}", record);
        this.records.add(record);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            BufferedWriter historyWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            try {
                Iterator var4 = this.records.iterator();

                while (var4.hasNext()) {
                    HistoryRecord r = (HistoryRecord) var4.next();
                    String line = this.documentWriter.write(r.document());
                    if (line != null) {
                        historyWriter.newLine();
                        historyWriter.append(line);
                    }
                }
            } catch (Throwable var8) {
                try {
                    historyWriter.close();
                } catch (Throwable var7) {
                    var8.addSuppressed(var7);
                }

                throw var8;
            }

            historyWriter.close();
        } catch (IOException var9) {
            Loggings.logErrorAndTraceRecord(this.logger, record, "Failed to convert record", (Throwable) var9);
            throw new SchemaHistoryException("Failed to convert record", var9);
        }

        return outputStream.toByteArray();
    }

    protected List<HistoryRecord> getRecords() {
        return this.records;
    }

    public synchronized void start() {
        this.doPreStart();
        this.lock.write(() -> {
            if (this.running.compareAndSet(false, true)) {
                if (!this.storageExists()) {
                    this.initializeStorage();
                }

                this.doStart();
            }

        });
        super.start();
    }

    public synchronized void stop() {
        if (this.running.compareAndSet(true, false)) {
            this.doStop();
        }

        super.stop();
    }

    protected void storeRecord(HistoryRecord record) throws SchemaHistoryException {
        this.doPreStoreRecord(record);
        if (record != null) {
            this.lock.write(() -> {
                if (!this.running.get()) {
                    throw new SchemaHistoryException("The history has been stopped and will not accept more records");
                } else {
                    this.doStoreRecord(record);
                }
            });
        }
    }

    protected void recoverRecords(Consumer<HistoryRecord> records) {
        this.lock.write(() -> {
            this.getRecords().forEach(records);
        });
    }

    public boolean exists() {
        return !this.getRecords().isEmpty();
    }

    protected void doPreStart() {
    }

    protected void doStart() {
    }

    protected void doStop() {
    }

    protected void doPreStoreRecord(HistoryRecord record) {
    }

    protected void doStoreRecord(HistoryRecord record) {
    }
}
