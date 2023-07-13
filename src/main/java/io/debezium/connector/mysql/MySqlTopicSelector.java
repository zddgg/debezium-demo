package io.debezium.connector.mysql;

import io.debezium.annotation.ThreadSafe;
import io.debezium.relational.TableId;
import io.debezium.schema.TopicSelector;

/**
 * @deprecated
 */
@Deprecated
@ThreadSafe
public class MySqlTopicSelector {
    public static TopicSelector<TableId> defaultSelector(String prefix, String heartbeatPrefix) {
        return TopicSelector.defaultSelector(prefix, heartbeatPrefix, ".", (t, pref, delimiter) -> {
            return String.join(delimiter, pref, t.catalog(), t.table());
        });
    }

    public static TopicSelector<TableId> defaultSelector(MySqlConnectorConfig connectorConfig) {
        return TopicSelector.defaultSelector(connectorConfig, (tableId, prefix, delimiter) -> {
            return String.join(delimiter, prefix, tableId.catalog(), tableId.table());
        });
    }
}
