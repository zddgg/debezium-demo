package io.debezium.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigDefinitionEditor {
    private String connectorName;
    private List<Field> type = new ArrayList();
    private List<Field> connector = new ArrayList();
    private List<Field> history = new ArrayList();
    private List<Field> events = new ArrayList();

    ConfigDefinitionEditor() {
    }

    ConfigDefinitionEditor(ConfigDefinition template) {
        this.connectorName = template.connectorName();
        this.type.addAll(template.type());
        this.connector.addAll(template.connector());
        this.history.addAll(template.history());
        this.events.addAll(template.events());
    }

    public ConfigDefinitionEditor name(String name) {
        this.connectorName = name;
        return this;
    }

    public ConfigDefinitionEditor type(Field... fields) {
        this.type.addAll(Arrays.asList(fields));
        return this;
    }

    public ConfigDefinitionEditor connector(Field... fields) {
        this.connector.addAll(Arrays.asList(fields));
        return this;
    }

    public ConfigDefinitionEditor history(Field... fields) {
        this.history.addAll(Arrays.asList(fields));
        return this;
    }

    public ConfigDefinitionEditor events(Field... fields) {
        this.events.addAll(Arrays.asList(fields));
        return this;
    }

    public ConfigDefinitionEditor excluding(Field... fields) {
        this.type.removeAll(Arrays.asList(fields));
        this.connector.removeAll(Arrays.asList(fields));
        this.history.removeAll(Arrays.asList(fields));
        this.events.removeAll(Arrays.asList(fields));
        return this;
    }

    public ConfigDefinition create() {
        return new ConfigDefinition(this.connectorName, this.type, this.connector, this.history, this.events);
    }
}
