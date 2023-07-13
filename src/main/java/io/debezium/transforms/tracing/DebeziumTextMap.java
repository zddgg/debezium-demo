/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.transforms.tracing;

import io.debezium.DebeziumException;
import io.opentracing.propagation.TextMap;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class DebeziumTextMap implements TextMap {

    private final Properties props = new Properties();

    public DebeziumTextMap() {
    }

    public DebeziumTextMap(String exportedSpan) {
        load(exportedSpan);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Iterator<Entry<String, String>> iterator() {
        return ((Map) props).entrySet().iterator();
    }

    @Override
    public void put(String key, String value) {
        props.put(key, value);
    }

    public String export() {
        try (Writer sw = new StringWriter()) {
            props.store(sw, null);
            return sw.toString();
        } catch (IOException e) {
            throw new DebeziumException(e);
        }
    }

    public void load(String span) {
        try (Reader sr = new StringReader(span)) {
            props.load(sr);
        } catch (IOException e) {
            throw new DebeziumException(e);
        }
    }
}
