package io.debezium.config;

import io.debezium.annotation.Immutable;
import io.debezium.annotation.ThreadSafe;
import org.apache.kafka.common.config.ConfigDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ThreadSafe
@Immutable
public class ConfigDefinition {
    private final String connectorName;
    private final List<Field> type;
    private final List<Field> connector;
    private final List<Field> history;
    private final List<Field> events;

    ConfigDefinition(String connectorName, List<Field> type, List<Field> connector, List<Field> history, List<Field> events) {
        this.connectorName = connectorName;
        this.type = Collections.unmodifiableList(type);
        this.connector = Collections.unmodifiableList(connector);
        this.history = Collections.unmodifiableList(history);
        this.events = Collections.unmodifiableList(events);
    }

    public static ConfigDefinitionEditor editor() {
        return new ConfigDefinitionEditor();
    }

    public ConfigDefinitionEditor edit() {
        return new ConfigDefinitionEditor(this);
    }

    public Iterable<Field> all() {
        List<Field> all = new ArrayList();
        this.addToList(all, this.type);
        this.addToList(all, this.connector);
        this.addToList(all, this.history);
        this.addToList(all, this.events);
        return all;
    }

    public ConfigDef configDef() {
        ConfigDef config = new ConfigDef();
        this.addToConfigDef(config, this.connectorName, this.type);
        this.addToConfigDef(config, "Connector", this.connector);
        this.addToConfigDef(config, "History Storage", this.history);
        this.addToConfigDef(config, "Events", this.events);
        return config;
    }

    public String connectorName() {
        return this.connectorName;
    }

    public List<Field> type() {
        return this.type;
    }

    public List<Field> connector() {
        return this.connector;
    }

    public List<Field> history() {
        return this.history;
    }

    public List<Field> events() {
        return this.events;
    }

    private void addToList(List<Field> list, List<Field> fields) {
        if (fields != null) {
            list.addAll(fields);
        }

    }

    private void addToConfigDef(ConfigDef configDef, String group, List<Field> fields) {
        if (!fields.isEmpty()) {
            Field.group(configDef, group, (Field[]) fields.toArray(new Field[0]));
        }

    }
}
