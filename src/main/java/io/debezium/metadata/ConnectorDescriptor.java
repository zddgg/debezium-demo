package io.debezium.metadata;

public class ConnectorDescriptor {
    private final String id;
    private final String name;
    private final String className;
    private final String version;

    public ConnectorDescriptor(String id, String name, String className, String version) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.version = version;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getClassName() {
        return this.className;
    }

    public String getVersion() {
        return this.version;
    }
}
