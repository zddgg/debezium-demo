package io.debezium.embedded;

import io.debezium.DebeziumException;
import io.debezium.config.Configuration;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.predicates.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Transformations implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transformations.class);
    private static final String TYPE_SUFFIX = ".type";
    private static final String PREDICATE_SUFFIX = ".predicate";
    private static final String NEGATE_SUFFIX = ".negate";
    private final Configuration config;
    private final List<Transformation<SourceRecord>> transforms = new ArrayList();
    private final Predicates predicates;

    public Transformations(Configuration config) {
        this.config = config;
        this.predicates = new Predicates(config);
        String transformationList = config.getString(EmbeddedEngine.TRANSFORMS);
        if (transformationList != null) {
            String[] var3 = transformationList.split(",");
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                String transfName = var3[var5];
                transfName = transfName.trim();
                Transformation<SourceRecord> transformation = this.getTransformation(transfName);
                this.transforms.add(transformation);
            }

        }
    }

    private static String transformationConfigNamespace(String name) {
        String var10000 = EmbeddedEngine.TRANSFORMS.name();
        return var10000 + "." + name;
    }

    Transformation<SourceRecord> getTransformation(String name) {
        String transformPrefix = transformationConfigNamespace(name);

        Transformation transformation;
        try {
            transformation = (Transformation) this.config.getInstance(transformPrefix + ".type", Transformation.class);
        } catch (Exception var7) {
            throw new DebeziumException("Error while instantiating transformation '" + name + "'", var7);
        }

        if (transformation == null) {
            throw new DebeziumException("Cannot instantiate transformation '" + name + "'");
        } else {
            transformation.configure(this.config.subset(transformPrefix, true).asMap());
            String predicateName = this.config.getString(transformPrefix + ".predicate");
            if (predicateName != null) {
                Boolean negate = this.config.getBoolean(transformPrefix + ".negate");
                Predicate<SourceRecord> predicate = this.predicates.getPredicate(predicateName);
                transformation = createPredicateTransformation(negate != null && negate, predicate, transformation);
            }

            return transformation;
        }
    }

    public SourceRecord transform(SourceRecord record) {
        Iterator var2 = this.transforms.iterator();

        while (var2.hasNext()) {
            Transformation<SourceRecord> t = (Transformation) var2.next();
            record = (SourceRecord) t.apply(record);
            if (record == null) {
                break;
            }
        }

        return record;
    }

    private static Transformation<SourceRecord> createPredicateTransformation(final boolean negate, final Predicate<SourceRecord> predicate, final Transformation<SourceRecord> transformation) {
        return new Transformation<SourceRecord>() {
            public SourceRecord apply(SourceRecord sourceRecord) {
                return negate ^ predicate.test(sourceRecord) ? (SourceRecord) transformation.apply(sourceRecord) : sourceRecord;
            }

            public ConfigDef config() {
                return null;
            }

            public void close() {
                try {
                    transformation.close();
                } catch (Exception var2) {
                    throw new RuntimeException(var2);
                }
            }

            public void configure(Map<String, ?> map) {
            }
        };
    }

    public void close() throws IOException {
        Iterator var1 = this.transforms.iterator();

        while (var1.hasNext()) {
            Transformation<SourceRecord> t = (Transformation) var1.next();

            try {
                t.close();
            } catch (Exception var4) {
                LOGGER.warn("Error while closing transformation", var4);
            }
        }

        this.predicates.close();
    }
}
