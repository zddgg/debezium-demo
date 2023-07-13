package io.debezium.transforms;

import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.data.Envelope;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class SmtManager<R extends ConnectRecord<R>> {
    private static final String RECORD_ENVELOPE_KEY_SCHEMA_NAME_SUFFIX = ".Key";
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtManager.class);

    public SmtManager(Configuration config) {
    }

    public static boolean isGenericOrTruncateMessage(SourceRecord originalRecord) {
        return Envelope.Operation.TRUNCATE.equals(Envelope.operationFor(originalRecord)) || Envelope.Operation.MESSAGE.equals(Envelope.operationFor(originalRecord));
    }

    public boolean isValidEnvelope(R record) {
        if (record.valueSchema() != null && record.valueSchema().name() != null && Envelope.isEnvelopeSchema(record.valueSchema())) {
            return true;
        } else {
            LOGGER.debug("Expected Envelope for transformation, passing it unchanged");
            return false;
        }
    }

    public boolean isValidKey(R record) {
        if (record.keySchema() != null && record.keySchema().name() != null && record.keySchema().name().endsWith(".Key")) {
            return true;
        } else {
            LOGGER.debug("Expected Key Schema for transformation, passing it unchanged. Message key: \"{}\"", record.key());
            return false;
        }
    }

    public void validate(Configuration configuration, Field.Set fields) {
        Map<String, ConfigValue> validations = configuration.validate(fields);
        Iterator var4 = validations.entrySet().iterator();

        Map.Entry entry;
        do {
            if (!var4.hasNext()) {
                return;
            }

            entry = (Map.Entry) var4.next();
        } while (((ConfigValue) entry.getValue()).errorMessages().isEmpty());

        ConfigValue value = (ConfigValue) entry.getValue();
        throw new ConfigException(value.name(), configuration.getString(value.name()), (String) value.errorMessages().get(0));
    }
}
