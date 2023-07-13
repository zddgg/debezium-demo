package io.debezium.connector;

import io.debezium.config.CommonConnectorConfig;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

public interface SourceInfoStructMaker<T extends AbstractSourceInfo> {
    void init(String var1, String var2, CommonConnectorConfig var3);

    Schema schema();

    Struct struct(T var1);
}
