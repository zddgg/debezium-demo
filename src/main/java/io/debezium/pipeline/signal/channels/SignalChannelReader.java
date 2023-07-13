package io.debezium.pipeline.signal.channels;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.signal.SignalRecord;

import java.util.List;

public interface SignalChannelReader {
    String name();

    void init(CommonConnectorConfig var1);

    default <T> void reset(T reference) {
    }

    List<SignalRecord> read();

    void close();
}
