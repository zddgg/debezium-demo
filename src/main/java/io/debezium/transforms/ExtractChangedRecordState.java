package io.debezium.transforms;

import io.debezium.config.Configuration;
import io.debezium.config.Field;
import io.debezium.util.Strings;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtractChangedRecordState<R extends ConnectRecord<R>> implements Transformation<R> {
    public static final Field HEADER_CHANGED_NAME;
    public static final Field HEADER_UNCHANGED_NAME;
    private String headerChangedName = null;
    private String headerUnchangedName = null;
    private Schema changedSchema;
    private Schema unchangedSchema;
    private SmtManager<R> smtManager;

    public void configure(Map<String, ?> configs) {
        Configuration config = Configuration.from(configs);
        this.smtManager = new SmtManager(config);
        if (config.getString(HEADER_CHANGED_NAME) != null) {
            this.headerChangedName = config.getString(HEADER_CHANGED_NAME);
            this.changedSchema = SchemaBuilder.array(SchemaBuilder.OPTIONAL_STRING_SCHEMA).optional().name(this.headerChangedName).build();
        }

        if (config.getString(HEADER_UNCHANGED_NAME) != null) {
            this.headerUnchangedName = config.getString(HEADER_UNCHANGED_NAME);
            this.unchangedSchema = SchemaBuilder.array(SchemaBuilder.OPTIONAL_STRING_SCHEMA).optional().name(this.headerUnchangedName).build();
        }

    }

    public R apply(R record) {
        if (record.value() != null && this.smtManager.isValidEnvelope(record)) {
            Struct value = Requirements.requireStruct(record.value(), "Record value should be struct.");
            Object after = value.get("after");
            Object before = value.get("before");
            if (after != null && before != null) {
                List<String> changedNames = new ArrayList();
                List<String> unchangedNames = new ArrayList();
                Struct afterValue = Requirements.requireStruct(after, "After value should be struct.");
                Struct beforeValue = Requirements.requireStruct(before, "Before value should be struct.");
                afterValue.schema().fields().forEach((field) -> {
                    if (!Objects.equals(afterValue.get(field), beforeValue.get(field))) {
                        changedNames.add(field.name());
                    } else {
                        unchangedNames.add(field.name());
                    }

                });
                if (!Strings.isNullOrBlank(this.headerChangedName)) {
                    record.headers().add(this.headerChangedName, changedNames, this.changedSchema);
                }

                if (!Strings.isNullOrBlank(this.headerUnchangedName)) {
                    record.headers().add(this.headerUnchangedName, unchangedNames, this.unchangedSchema);
                }
            }

            return record;
        } else {
            return record;
        }
    }

    public void close() {
    }

    public ConfigDef config() {
        ConfigDef config = new ConfigDef();
        Field.group(config, (String) null, HEADER_CHANGED_NAME, HEADER_UNCHANGED_NAME);
        return config;
    }

    static {
        HEADER_CHANGED_NAME = Field.create("header.changed.name").withDisplayName("Header change name.").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withDescription("Specify the header changed name, default is null which means not send changes to header.");
        HEADER_UNCHANGED_NAME = Field.create("header.unchanged.name").withDisplayName("Header unchanged name.").withType(Type.STRING).withWidth(Width.LONG).withImportance(Importance.LOW).withDescription("Specify the header unchanged name of schema, default is null which means not send changes to header.");
    }
}
