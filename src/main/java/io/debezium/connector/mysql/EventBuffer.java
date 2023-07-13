package io.debezium.connector.mysql;

import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.EventType;
import com.github.shyiko.mysql.binlog.event.QueryEventData;
import io.debezium.pipeline.source.spi.ChangeEventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class EventBuffer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBuffer.class);
    private final int capacity;
    private final Queue<Event> buffer;
    private final MySqlStreamingChangeEventSource streamingChangeEventSource;
    private boolean txStarted = false;
    private final ChangeEventSource.ChangeEventSourceContext changeEventSourceContext;
    private MySqlStreamingChangeEventSource.BinlogPosition largeTxNotBufferedPosition;
    private MySqlStreamingChangeEventSource.BinlogPosition forwardTillPosition;

    EventBuffer(int capacity, MySqlStreamingChangeEventSource streamingChangeEventSource, ChangeEventSource.ChangeEventSourceContext changeEventSourceContext) {
        this.capacity = capacity;
        this.buffer = new ArrayBlockingQueue(capacity);
        this.streamingChangeEventSource = streamingChangeEventSource;
        this.changeEventSourceContext = changeEventSourceContext;
    }

    public void add(MySqlPartition partition, MySqlOffsetContext offsetContext, Event event) {
        if (event != null) {
            if (this.isReplayingEventsBeyondBufferCapacity()) {
                this.streamingChangeEventSource.handleEvent(partition, offsetContext, event);
            } else {
                if (event.getHeader().getEventType() == EventType.QUERY) {
                    QueryEventData command = (QueryEventData) this.streamingChangeEventSource.unwrapData(event);
                    LOGGER.debug("Received query command: {}", event);
                    String sql = command.getSql().trim();
                    if (sql.equalsIgnoreCase("BEGIN")) {
                        this.beginTransaction(partition, offsetContext, event);
                    } else if (sql.equalsIgnoreCase("COMMIT")) {
                        this.completeTransaction(partition, offsetContext, true, event);
                    } else if (sql.equalsIgnoreCase("ROLLBACK")) {
                        this.rollbackTransaction();
                    } else {
                        this.consumeEvent(partition, offsetContext, event);
                    }
                } else if (event.getHeader().getEventType() == EventType.XID) {
                    this.completeTransaction(partition, offsetContext, true, event);
                } else {
                    this.consumeEvent(partition, offsetContext, event);
                }

            }
        }
    }

    private boolean isReplayingEventsBeyondBufferCapacity() {
        if (this.forwardTillPosition != null) {
            if (this.forwardTillPosition.equals(this.streamingChangeEventSource.getCurrentBinlogPosition())) {
                this.forwardTillPosition = null;
            }

            return true;
        } else {
            return false;
        }
    }

    private void addToBuffer(Event event) {
        if (!this.isInBufferFullMode()) {
            if (this.buffer.size() == this.capacity) {
                this.switchToBufferFullMode();
            } else {
                this.buffer.add(event);
            }

        }
    }

    private void switchToBufferFullMode() {
        this.largeTxNotBufferedPosition = this.streamingChangeEventSource.getCurrentBinlogPosition();
        LOGGER.info("Buffer full, will need to re-read part of the transaction from binlog from {}", this.largeTxNotBufferedPosition);
        this.streamingChangeEventSource.getMetrics().onLargeTransaction();
        if (((Event) this.buffer.peek()).getHeader().getEventType() == EventType.TABLE_MAP) {
            this.buffer.remove();
        }

    }

    private boolean isInBufferFullMode() {
        return this.largeTxNotBufferedPosition != null;
    }

    private void consumeEvent(MySqlPartition partition, MySqlOffsetContext offsetContext, Event event) {
        if (this.txStarted) {
            this.addToBuffer(event);
        } else {
            this.streamingChangeEventSource.handleEvent(partition, offsetContext, event);
        }

    }

    private void beginTransaction(MySqlPartition partition, MySqlOffsetContext offsetContext, Event event) {
        if (this.txStarted) {
            LOGGER.warn("New transaction started but the previous was not completed, processing the buffer");
            this.completeTransaction(partition, offsetContext, false, (Event) null);
        } else {
            this.txStarted = true;
        }

        this.addToBuffer(event);
    }

    private void completeTransaction(MySqlPartition partition, MySqlOffsetContext offsetContext, boolean wellFormed, Event event) {
        LOGGER.debug("Committing transaction");
        if (event != null) {
            this.addToBuffer(event);
        }

        if (!this.txStarted) {
            LOGGER.warn("Commit requested but TX was not started before");
            wellFormed = false;
        }

        LOGGER.debug("Executing events from buffer");
        Iterator var5 = this.buffer.iterator();

        while (var5.hasNext()) {
            Event e = (Event) var5.next();
            this.streamingChangeEventSource.handleEvent(partition, offsetContext, e);
        }

        LOGGER.debug("Executing events from binlog that have not fit into buffer");
        if (this.isInBufferFullMode()) {
            this.forwardTillPosition = this.streamingChangeEventSource.getCurrentBinlogPosition();
            this.streamingChangeEventSource.rewindBinaryLogClient(this.changeEventSourceContext, this.largeTxNotBufferedPosition);
        }

        this.streamingChangeEventSource.getMetrics().onCommittedTransaction();
        if (!wellFormed) {
            this.streamingChangeEventSource.getMetrics().onNotWellFormedTransaction();
        }

        this.clear();
    }

    private void rollbackTransaction() {
        LOGGER.debug("Rolling back transaction");
        boolean wellFormed = true;
        if (!this.txStarted) {
            LOGGER.warn("Rollback requested but TX was not started before");
            wellFormed = false;
        }

        this.streamingChangeEventSource.getMetrics().onRolledBackTransaction();
        if (!wellFormed) {
            this.streamingChangeEventSource.getMetrics().onNotWellFormedTransaction();
        }

        this.clear();
    }

    private void clear() {
        this.buffer.clear();
        this.largeTxNotBufferedPosition = null;
        this.txStarted = false;
    }
}
