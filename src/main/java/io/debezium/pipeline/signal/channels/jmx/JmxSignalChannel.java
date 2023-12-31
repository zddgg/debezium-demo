/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.pipeline.signal.channels.jmx;

import io.debezium.config.CommonConnectorConfig;
import io.debezium.pipeline.signal.SignalRecord;
import io.debezium.pipeline.signal.channels.SignalChannelReader;
import io.debezium.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.debezium.pipeline.JmxUtils.registerMXBean;
import static io.debezium.pipeline.JmxUtils.unregisterMXBean;

public class JmxSignalChannel implements SignalChannelReader, JmxSignalChannelMXBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmxSignalChannel.class);
    private static final String CHANNEL_NAME = "jmx";

    private static final Queue<SignalRecord> SIGNALS = new ConcurrentLinkedQueue<>();
    private CommonConnectorConfig connectorConfig;

    @Override
    public String name() {
        return CHANNEL_NAME;
    }

    @Override
    public void init(CommonConnectorConfig connectorConfig) {

        this.connectorConfig = connectorConfig;

        registerMXBean(this, connectorConfig, "management", "signals");

        LOGGER.info("Registration for Signaling MXBean with the platform server is successfully");

    }

    @Override
    public List<SignalRecord> read() {

        LOGGER.trace("Reading signaling events from queue");

        SignalRecord signalRecord = SIGNALS.poll();
        if (signalRecord == null) {
            return new ArrayList<>();
        }

        return Collections.singletonList(signalRecord);
    }

    @Override
    public void close() {

        unregisterMXBean(connectorConfig, "management", "signals");
    }

    @Override
    public void signal(String id, String type, String data) {

        SIGNALS.add(new SignalRecord(id, type, data, Maps.of()));
    }

}
