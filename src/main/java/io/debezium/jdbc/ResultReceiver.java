package io.debezium.jdbc;

public interface ResultReceiver {
    void deliver(Object var1);

    boolean hasReceived();

    Object get();

    static ResultReceiver create() {
        return new ResultReceiver() {
            private boolean received = false;
            private Object object = null;

            public void deliver(Object o) {
                this.received = true;
                this.object = o;
            }

            public boolean hasReceived() {
                return this.received;
            }

            public Object get() {
                return this.object;
            }

            public String toString() {
                return "[received = " + this.received + ", object = " + this.object + "]";
            }
        };
    }
}
