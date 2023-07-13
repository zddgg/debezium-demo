package io.debezium.relational.history;

public interface SchemaHistoryListener {
    SchemaHistoryListener NOOP = new SchemaHistoryListener() {
        public void stopped() {
        }

        public void started() {
        }

        public void recoveryStopped() {
        }

        public void recoveryStarted() {
        }

        public void onChangeFromHistory(HistoryRecord record) {
        }

        public void onChangeApplied(HistoryRecord record) {
        }
    };

    void started();

    void stopped();

    void recoveryStarted();

    void recoveryStopped();

    void onChangeFromHistory(HistoryRecord var1);

    void onChangeApplied(HistoryRecord var1);
}
