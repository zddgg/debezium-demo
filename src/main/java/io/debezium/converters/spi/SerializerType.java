package io.debezium.converters.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SerializerType {
    JSON,
    AVRO;

    private static final Map<String, SerializerType> NAME_TO_TYPE;
    private String name;

    public static SerializerType withName(String name) {
        return name == null ? null : (SerializerType) NAME_TO_TYPE.get(name.toLowerCase(Locale.getDefault()));
    }

    private SerializerType() {
        this.name = this.name().toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return this.name;
    }

    // $FF: synthetic method
    private static SerializerType[] $values() {
        return new SerializerType[]{JSON, AVRO};
    }

    static {
        SerializerType[] types = values();
        Map<String, SerializerType> nameToType = new HashMap(types.length);
        SerializerType[] var2 = types;
        int var3 = types.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            SerializerType type = var2[var4];
            nameToType.put(type.name, type);
        }

        NAME_TO_TYPE = Collections.unmodifiableMap(nameToType);
    }
}
