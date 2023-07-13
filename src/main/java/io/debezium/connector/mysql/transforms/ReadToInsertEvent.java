package io.debezium.connector.mysql.transforms;

import io.debezium.config.Configuration;
import io.debezium.data.Envelope.Operation;
import io.debezium.transforms.SmtManager;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ReadToInsertEvent<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReadToInsertEvent.class);
    private SmtManager<R> smtManager;

    public R apply(R record) {
        if (record.value() != null && this.smtManager.isValidEnvelope(record)) {
            Struct originalValueStruct = (Struct) record.value();
            String operation = originalValueStruct.getString("op");
            if (operation.equals(Operation.READ.code())) {
                Struct updatedValueStruct = originalValueStruct.put("op", Operation.CREATE.code());
                return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), record.valueSchema(), updatedValueStruct, record.timestamp());
            } else {
                return record;
            }
        } else {
            return record;
        }
    }

    public ConfigDef config() {
        return new ConfigDef();
    }

    public void close() {
    }

    public void configure(Map<String, ?> props) {
        Configuration config = Configuration.from(props);
        this.smtManager = new SmtManager(config);
    }
}
