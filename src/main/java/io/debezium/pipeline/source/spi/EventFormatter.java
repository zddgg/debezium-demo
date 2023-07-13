package io.debezium.pipeline.source.spi;

import io.debezium.data.SchemaUtil;
import org.apache.kafka.connect.data.Struct;

import java.util.Map;

class EventFormatter {
    private Map<String, String> sourcePosition;
    private Object key;
    private Struct value;
    private final StringBuilder string = new StringBuilder();

    private void printStruct(Struct value) {
        this.string.append(SchemaUtil.asDetailedString(value));
    }

    private StringBuilder addDelimiter() {
        return this.string.append(", ");
    }

    private void printSimpleValue(Object key, Object value) {
        this.string.append(key).append(": ").append(value);
    }

    private void removeDelimiter() {
        if (this.string.length() >= 2 && this.string.charAt(this.string.length() - 2) == ',' && this.string.charAt(this.string.length() - 1) == ' ') {
            this.string.delete(this.string.length() - 2, this.string.length());
        }

    }

    public String toString() {
        if (this.value != null) {
            String operation = this.value.getString("op");
            if (operation != null) {
                this.printSimpleValue("operation", operation);
                this.addDelimiter();
            }
        }

        if (this.sourcePosition != null) {
            this.string.append("position: {");
            this.sourcePosition.forEach((k, v) -> {
                this.printSimpleValue(k, v);
                this.addDelimiter();
            });
            this.removeDelimiter();
            this.string.append("}");
            this.addDelimiter();
        }

        if (this.key != null) {
            if (this.key instanceof Struct) {
                this.string.append("key: ");
                this.printStruct((Struct) this.key);
            } else {
                this.printSimpleValue("key", this.key);
            }

            this.addDelimiter();
        }

        if (this.value != null) {
            Struct before = this.value.getStruct("before");
            Struct after = this.value.getStruct("after");
            if (before != null) {
                this.string.append("before: ");
                this.printStruct(before);
                this.addDelimiter();
            }

            if (after != null) {
                this.string.append("after: ");
                this.printStruct(after);
                this.addDelimiter();
            }
        }

        this.removeDelimiter();
        return this.string.toString();
    }

    public EventFormatter sourcePosition(Map<String, String> sourcePosition) {
        this.sourcePosition = sourcePosition;
        return this;
    }

    public EventFormatter key(Object key) {
        this.key = key;
        return this;
    }

    public EventFormatter value(Struct value) {
        this.value = value;
        return this;
    }
}
