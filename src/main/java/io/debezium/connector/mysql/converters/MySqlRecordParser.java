package io.debezium.connector.mysql.converters;

import io.debezium.converters.spi.RecordParser;
import io.debezium.util.Collect;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.DataException;

import java.util.Set;

public class MySqlRecordParser extends RecordParser {
    static final String TABLE_NAME_KEY = "table";
    static final String SERVER_ID_KEY = "server_id";
    static final String GTID_KEY = "gtid";
    static final String BINLOG_FILENAME_OFFSET_KEY = "file";
    static final String BINLOG_POSITION_OFFSET_KEY = "pos";
    static final String BINLOG_ROW_IN_EVENT_OFFSET_KEY = "row";
    static final String THREAD_KEY = "thread";
    static final String QUERY_KEY = "query";
    static final Set<String> MYSQL_SOURCE_FIELDS = Collect.unmodifiableSet(new String[]{"table", "server_id", "gtid", "file", "pos", "row", "thread", "query"});

    public MySqlRecordParser(Schema schema, Struct record) {
        super(schema, record, new String[]{"before", "after"});
    }

    public Object getMetadata(String name) {
        if (SOURCE_FIELDS.contains(name)) {
            return this.source().get(name);
        } else if (MYSQL_SOURCE_FIELDS.contains(name)) {
            return this.source().get(name);
        } else {
            throw new DataException("No such field \"" + name + "\" in the \"source\" field of events from MySQL connector");
        }
    }
}
