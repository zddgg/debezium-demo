package io.debezium.pipeline.source.spi;

import io.debezium.relational.ChangeTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ChangeTableResultSet<C extends ChangeTable, T extends Comparable<T>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeTableResultSet.class);
    private final C changeTable;
    private final ResultSet resultSet;
    private final int columnDataOffset;
    private boolean completed = false;
    private T currentChangePosition;
    private T previousChangePosition;

    public ChangeTableResultSet(C changeTable, ResultSet resultSet, int columnDataOffset) {
        this.changeTable = changeTable;
        this.resultSet = resultSet;
        this.columnDataOffset = columnDataOffset;
    }

    public C getChangeTable() {
        return this.changeTable;
    }

    public T getChangePosition() throws SQLException {
        return this.currentChangePosition;
    }

    protected T getPreviousChangePosition() {
        return this.previousChangePosition;
    }

    public int getOperation() throws SQLException {
        return this.getOperation(this.resultSet);
    }

    public boolean isCurrentPositionSmallerThanPreviousPosition() {
        return this.previousChangePosition != null && this.previousChangePosition.compareTo(this.currentChangePosition) > 0;
    }

    public boolean next() throws SQLException {
        this.completed = !this.resultSet.next();
        this.previousChangePosition = this.currentChangePosition;
        this.currentChangePosition = this.getNextChangePosition(this.resultSet);
        if (this.completed) {
            LOGGER.trace("Closing result set of change tables for table {}", this.changeTable);
            this.resultSet.close();
        }

        return !this.completed;
    }

    public Object[] getData() throws SQLException {
        int dataColumnCount = this.resultSet.getMetaData().getColumnCount() - (this.columnDataOffset - 1);
        Object[] data = new Object[dataColumnCount];

        for (int i = 0; i < dataColumnCount; ++i) {
            data[i] = this.getColumnData(this.resultSet, this.columnDataOffset + i);
        }

        return data;
    }

    protected Object getColumnData(ResultSet resultSet, int columnIndex) throws SQLException {
        return resultSet.getObject(columnIndex);
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public int compareTo(ChangeTableResultSet<C, T> other) throws SQLException {
        return this.getChangePosition().compareTo(other.getChangePosition());
    }

    public String toString() {
        return "ChangeTableResultSet{changeTable=" + this.changeTable + ", resultSet=" + this.resultSet + ", completed=" + this.completed + ", currentChangePosition=" + this.currentChangePosition + "}";
    }

    protected abstract int getOperation(ResultSet var1) throws SQLException;

    protected abstract T getNextChangePosition(ResultSet var1) throws SQLException;
}
