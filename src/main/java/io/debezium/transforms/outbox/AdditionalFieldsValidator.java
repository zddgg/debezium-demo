package io.debezium.transforms.outbox;

import io.debezium.config.Configuration;
import io.debezium.config.Field;

import java.util.Iterator;
import java.util.List;

public class AdditionalFieldsValidator {
    public static int isListOfStringPairs(Configuration config, Field field, Field.ValidationOutput problems) {
        List<String> value = config.getStrings(field, ",");
        int errors = 0;
        if (value == null) {
            return errors;
        } else {
            Iterator var5 = value.iterator();

            while (var5.hasNext()) {
                String mapping = (String) var5.next();
                String[] parts = mapping.split(":");
                if (parts.length != 2 && parts.length != 3) {
                    problems.accept(field, value, "A comma-separated list of valid String pairs or trios is expected but got: " + value);
                    ++errors;
                }
            }

            return errors;
        }
    }
}
