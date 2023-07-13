package io.debezium.relational.ddl;

import io.debezium.relational.SystemVariables;
import io.debezium.relational.Tables;

public interface DdlParser {
    void parse(String var1, Tables var2);

    void setCurrentDatabase(String var1);

    void setCurrentSchema(String var1);

    DdlChanges getDdlChanges();

    SystemVariables systemVariables();
}
