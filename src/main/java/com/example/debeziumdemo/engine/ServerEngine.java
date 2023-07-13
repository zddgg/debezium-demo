package com.example.debeziumdemo.engine;

import com.alibaba.fastjson2.JSONObject;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ServerEngine {

    private DebeziumEngine<RecordChangeEvent<SourceRecord>> engine;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void startEngine() {
        engine = DebeziumEngine
                .create(ChangeEventFormat.of(Connect.class))
                .using(redisFile())
                .notifying(this::handleChangeEvent)
                .build();
        // Run the engine asynchronously ...
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
    }

    @PreDestroy
    private void preDestroy() throws IOException {
        if (engine != null) {
            engine.close();
        }
    }

    private Properties redisFile() {
        final Properties props = new Properties();

        // Redis
        props.setProperty("name", "engine");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("topic.prefix", "cdc-test12333");

        props.setProperty("offset.storage", "io.debezium.redis.offset.RedisOffsetBackingStore");
        props.setProperty("offset.storage.redis.address", "127.0.0.1:6379");
        props.setProperty("offset.storage.redis.user", "default");
        props.setProperty("offset.storage.redis.password", "123456");
        props.setProperty("offset.flush.interval.ms", "3000");

        /* begin connector properties */
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.server.id", "85744");
        props.setProperty("database.server.name", "my-app-connector");


        props.setProperty("database.history", "io.debezium.relational.history.MemoryDatabaseHistory");
        return props;
    }

    private Properties file() {
        final Properties props = new Properties();

        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("name", "engine");
        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", "/tmp/offsets.dat");
        props.setProperty("offset.flush.interval.ms", "60000");
        /* begin connector properties */
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.server.id", "85744");
        props.setProperty("database.server.name", "my-app-connector");
        props.setProperty("database.history",
                "io.debezium.relational.history.MemoryDatabaseHistory");
        props.setProperty("database.history.file.filename",
                "/path/to/storage/dbhistory.dat");

        return props;
    }

    private Properties redisProps() {
        final Properties props = new Properties();

        // Redis
        props.setProperty("name", "engine");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("topic.prefix", "cdc-test12333");

        props.setProperty("offset.storage", "io.debezium.storage.redis.offset.RedisOffsetBackingStore");
        props.setProperty("offset.storage.redis.address", "127.0.0.1:6379");
        props.setProperty("offset.storage.redis.user", "default");
        props.setProperty("offset.storage.redis.password", "123456");
        props.setProperty("offset.flush.interval.ms", "10000");


        /* begin connector properties */
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.server.id", "85744");
        props.setProperty("database.server.name", "my-app-connector");


        props.setProperty("database.history", "io.debezium.storage.redis.history.RedisSchemaHistory");
        props.setProperty("database.history.redis.address", "127.0.0.1:6379");
        props.setProperty("database.history.redis.user", "default");
        props.setProperty("database.history.redis.password", "123456");
        return props;
    }

    private Properties kafkaProps() {
        final Properties props = new Properties();
        props.setProperty("name", "engine");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("topic.prefix", "debezium");
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");

        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.KafkaOffsetBackingStore");
        props.setProperty("offset.storage.topic", "cdc-offset");
        props.setProperty("offset.storage.partitions", "1");
        props.setProperty("offset.storage.replication.factor", "1");
        props.setProperty("offset.flush.interval.ms", "60000");


        /* begin connector properties */
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.server.id", "85744");
        props.setProperty("database.server.name", "my-app-connector");


        props.setProperty("schema.history.internal", "io.debezium.storage.kafka.history.KafkaSchemaHistory");
        props.setProperty("schema.history.internal.kafka.topic", "cdc-history");
        props.setProperty("schema.history.internal.kafka.bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("schema.history.internal.redis.password", "123456");

        return props;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        JSONObject snapshot = getSnapshot(recordChangeEvent);
//        System.out.println("发送数据: " + snapshot);
//        if (snapshot != null) {
//            String url = "http://localhost:8081/sendMsg";
//            JSONObject map = new JSONObject();
//            map.put("data", snapshot.toString());
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<JSONObject> request = new HttpEntity<>(map, headers);
//            ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(url,request,JSONObject.class);
//            System.out.println(responseEntity.getStatusCode());
//        }
    }

    private JSONObject getSnapshot(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        JSONObject snapshot = new JSONObject();
        try {
            SourceRecord sourceRecord = recordChangeEvent.record();
            Struct value = (Struct) sourceRecord.value();
            Envelope.Operation operation = Envelope.Operation.forCode((String) value.get(Envelope.FieldName.OPERATION));
            String topic = sourceRecord.topic();
            String[] split = topic.split("\\.");
            String dbName = split[1];
            String tableName = split[2];
            Struct beforeStruct = value.getStruct(Envelope.FieldName.BEFORE);
            Struct afterStruct = value.getStruct(Envelope.FieldName.AFTER);

            JSONObject beforeJsonData = new JSONObject();
            JSONObject afterJsonData = new JSONObject();

            // update
            if (beforeStruct != null && afterStruct != null) {
                for (Field field : beforeStruct.schema().fields()) {
                    beforeJsonData.put(field.name(), beforeStruct.get(field));
                }
                for (Field field : afterStruct.schema().fields()) {
                    afterJsonData.put(field.name(), afterStruct.get(field));
                }
                operation = Envelope.Operation.UPDATE;
            }
            // create & snapshot
            else if (afterStruct != null) {
                for (Field field : afterStruct.schema().fields()) {
                    afterJsonData.put(field.name(), afterStruct.get(field));
                }
                operation = Envelope.Operation.CREATE;
            }
            // delete
            else if (beforeStruct != null) {
                for (Field field : beforeStruct.schema().fields()) {
                    beforeJsonData.put(field.name(), beforeStruct.get(field));
                }
                operation = Envelope.Operation.DELETE;
            }

            snapshot.put("dbName", dbName);
            snapshot.put("tableName", tableName);
            snapshot.put("operation", operation.toString().toLowerCase());
            snapshot.put("beforeJsonData", beforeJsonData);
            snapshot.put("afterJsonData", afterJsonData);
            System.out.println("生产数据: " + snapshot);
            return snapshot;
        } catch (Exception e) {
            return null;
        }
    }
}
