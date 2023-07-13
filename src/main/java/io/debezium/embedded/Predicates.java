package io.debezium.embedded;

import io.debezium.DebeziumException;
import io.debezium.config.Configuration;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.transforms.predicates.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Predicates implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Predicates.class);
    private static final String TYPE_SUFFIX = ".type";
    private final Map<String, Predicate<SourceRecord>> predicates = new HashMap();

    public Predicates(Configuration config) {
        String predicateList = config.getString(EmbeddedEngine.PREDICATES);
        if (predicateList != null) {
            String[] var3 = predicateList.split(",");
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String predicateName = var3[var5];
                predicateName = predicateName.trim();
                Predicate<SourceRecord> predicate = createPredicate(config, predicateName);
                this.predicates.put(predicateName, predicate);
            }

        }
    }

    public Predicate<SourceRecord> getPredicate(String name) {
        return (Predicate) this.predicates.get(name);
    }

    private static Predicate<SourceRecord> createPredicate(Configuration config, String name) {
        String predicatePrefix = predicateConfigNamespace(name);

        Predicate predicate;
        try {
            predicate = (Predicate) config.getInstance(predicatePrefix + ".type", Predicate.class);
        } catch (Exception var5) {
            throw new DebeziumException("Error while instantiating predicate '" + name + "'", var5);
        }

        if (predicate == null) {
            throw new DebeziumException("Cannot instantiate predicate '" + name + "'");
        } else {
            predicate.configure(config.subset(predicatePrefix, true).asMap());
            return predicate;
        }
    }

    private static String predicateConfigNamespace(String name) {
        String var10000 = EmbeddedEngine.PREDICATES.name();
        return var10000 + "." + name;
    }

    public void close() throws IOException {
        Iterator var1 = this.predicates.values().iterator();

        while (var1.hasNext()) {
            Predicate<SourceRecord> p = (Predicate) var1.next();

            try {
                p.close();
            } catch (Exception var4) {
                LOGGER.warn("Error while closing predicate", var4);
            }
        }

    }
}
