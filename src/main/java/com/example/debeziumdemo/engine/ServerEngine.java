package com.example.debeziumdemo.engine;

import com.alibaba.fastjson2.JSONObject;
import io.debezium.data.Envelope;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static io.debezium.data.Envelope.FieldName.*;

@Slf4j
@Component
public class ServerEngine {

    private DebeziumEngine<RecordChangeEvent<SourceRecord>> engine;
    private DebeziumEngine<RecordChangeEvent<SourceRecord>> engine1;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void startEngine() {
        engine = DebeziumEngine
                .create(ChangeEventFormat.of(Connect.class))
                .using(redis())
                .notifying(this::handleChangeEvent)
                .build();
        engine1 = DebeziumEngine
                .create(ChangeEventFormat.of(Connect.class))
                .using(redis2())
                .notifying(this::handleChangeEvent)
                .build();
        // Run the engine asynchronously ...
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(engine);
        executor.execute(engine1);
    }

    @PreDestroy
    private void preDestroy() throws IOException {
        if (engine != null) {
            engine.close();
        }
        if (engine1 != null) {
            engine1.close();
        }
    }

    //格式化数据并处理，提交offset
    public void handlePayload(List<RecordChangeEvent<SourceRecord>> recordChangeEvents, DebeziumEngine.RecordCommitter<RecordChangeEvent<SourceRecord>> recordRecordCommitter) {
        System.out.println("size " + recordChangeEvents.size());
        recordChangeEvents.forEach(recordRecordChangeEvent -> {
            handleChangeEvent(recordRecordChangeEvent);
            try {
                recordRecordCommitter.markProcessed(recordRecordChangeEvent);
                recordRecordCommitter.markBatchFinished();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Properties redis() {
        final Properties props = new Properties();

        // Redis
        props.setProperty("name", "debezium-connector-1");
        props.setProperty("topic.prefix", "my-app-connector122222");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");

//        props.setProperty("snapshot.mode", "schema_only");
//        props.setProperty("snapshot.mode", "initial");
        props.setProperty("snapshot.mode", "initial");

        props.setProperty("offset.storage", "io.debezium.storage.redis.offset.RedisOffsetBackingStore");
        props.setProperty("offset.storage.redis.address", "127.0.0.1:6379");
        props.setProperty("offset.storage.redis.password", "123456");
        props.setProperty("offset.flush.interval.ms", "5000");

        /* begin connector properties */
        props.setProperty("database.server.id", "11111");
        props.setProperty("database.server.name", "debezium-connector-1");
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.dbname", "biz_service_goods");
        props.setProperty("table.include.list", "biz_service_goods.goods1, biz_service_goods.goods2, biz_service_goods.goods3");


        props.setProperty("schema.history.internal", "io.debezium.relational.history.MemorySchemaHistory");
//        props.setProperty("schema.history.internal", "io.debezium.storage.redis.history.RedisSchemaHistory");
        props.setProperty("schema.history.internal.redis.address", "127.0.0.1:6379");
        props.setProperty("schema.history.internal.redis.password", "123456");
        props.setProperty("schema.history.store.only.captured.tables.ddl", "true");
        return props;
    }

    private Properties redis2() {
        final Properties props = new Properties();

        // Redis
        props.setProperty("name", "debezium-connector-2");
        props.setProperty("topic.prefix", "debezium-connector-2");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");

//        props.setProperty("snapshot.mode", "schema_only");
//        props.setProperty("snapshot.mode", "initial");
        props.setProperty("snapshot.mode", "initial");

        props.setProperty("offset.storage", "io.debezium.storage.redis.offset.RedisOffsetBackingStore");
        props.setProperty("offset.storage.redis.address", "127.0.0.1:6379");
        props.setProperty("offset.storage.redis.password", "123456");
        props.setProperty("offset.flush.interval.ms", "5000");

        /* begin connector properties */
        props.setProperty("database.server.id", "22222");
        props.setProperty("database.server.name", "debezium-connector-2");
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.dbname", "biz_service_order");
        props.setProperty("table.include.list", "biz_service_order.order1, biz_service_order.order2, biz_service_order.order3");


        props.setProperty("schema.history.internal", "io.debezium.relational.history.MemorySchemaHistory");
//        props.setProperty("schema.history.internal", "io.debezium.storage.redis.history.RedisSchemaHistory");
        props.setProperty("schema.history.internal.redis.address", "127.0.0.1:6379");
        props.setProperty("schema.history.internal.redis.password", "123456");
        props.setProperty("schema.history.store.only.captured.tables.ddl", "true");
        return props;
    }

    private Properties kafka() {
        final Properties props = new Properties();

        // Redis
        props.setProperty("name", "debezium-connector-1");
        props.setProperty("topic.prefix", "my-app-connector122222");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");

//        props.setProperty("snapshot.mode", "schema_only");
//        props.setProperty("snapshot.mode", "initial");
        props.setProperty("snapshot.mode", "initial");

        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.KafkaOffsetBackingStore");
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("offset.storage.topic", "cdc-offset-1");
        props.setProperty("offset.storage.partitions", "1");
        props.setProperty("offset.storage.replication.factor", "1");
        props.setProperty("offset.flush.interval.ms", "5000");

        /* begin connector properties */
        props.setProperty("database.server.id", "11111");
        props.setProperty("database.server.name", "debezium-connector-1");
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "root");
        props.setProperty("database.password", "123456");
        props.setProperty("database.allowPublicKeyRetrieval", "true");
        props.setProperty("database.dbname", "test");
        props.setProperty("table.include.list", "test.user_ttt, test.user_qqq2");


        props.setProperty("schema.history.internal", "io.debezium.storage.kafka.history.KafkaSchemaHistory");
        props.setProperty("schema.history.internal.kafka.topic", "cdc-history-1");
        props.setProperty("schema.history.internal.kafka.bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("schema.history.store.only.captured.tables.ddl", "true");
        return props;
    }

    private final LongAdder a = new LongAdder();

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        a.increment();
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

        System.out.println("sub " + a.longValue());
    }

    private JSONObject getSnapshot(RecordChangeEvent<SourceRecord> recordChangeEvent) {
        JSONObject snapshot = new JSONObject();
        try {
            SourceRecord sourceRecord = recordChangeEvent.record();
            Struct value = (Struct) sourceRecord.value();
            Envelope.Operation operation = Envelope.Operation.forCode((String) value.get(OPERATION));
            String topic = sourceRecord.topic();
            String[] split = topic.split("\\.");
            String dbName = split[1];
            String tableName = split[2];
            Struct beforeStruct = value.getStruct(BEFORE);
            Struct afterStruct = value.getStruct(AFTER);

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
            if (operation != null) {
                snapshot.put("operation", operation.toString().toLowerCase());
            }
            snapshot.put("beforeJsonData", beforeJsonData);
            snapshot.put("afterJsonData", afterJsonData);
            System.out.println("生产数据: " + snapshot);
            return snapshot;
        } catch (Exception e) {
            return null;
        }
    }
}
