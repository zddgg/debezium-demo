package io.debezium.relational.ddl;

import io.debezium.relational.Column;
import io.debezium.relational.ColumnEditor;
import io.debezium.relational.SystemVariables;
import io.debezium.relational.TableId;
import io.debezium.text.MultipleParsingExceptions;
import io.debezium.text.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractDdlParser implements DdlParser {
    protected final boolean skipViews;
    protected final boolean skipComments;
    protected DdlChanges ddlChanges;
    protected SystemVariables systemVariables;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String currentSchema = null;

    public AbstractDdlParser(boolean includeViews, boolean includeComments) {
        this.skipViews = !includeViews;
        this.skipComments = !includeComments;
        this.ddlChanges = new DdlChanges();
        this.systemVariables = this.createNewSystemVariablesInstance();
    }

    public void setCurrentSchema(String schemaName) {
        this.currentSchema = schemaName;
    }

    public void setCurrentDatabase(String databaseName) {
        this.currentSchema = databaseName;
    }

    public DdlChanges getDdlChanges() {
        return this.ddlChanges;
    }

    public SystemVariables systemVariables() {
        return this.systemVariables;
    }

    protected abstract SystemVariables createNewSystemVariablesInstance();

    public String currentSchema() {
        return this.currentSchema;
    }

    public TableId resolveTableId(String schemaName, String tableName) {
        return new TableId(schemaName, (String) null, tableName);
    }

    protected boolean skipComments() {
        return true;
    }

    protected void signalChangeEvent(DdlParserListener.Event event) {
        this.ddlChanges.handle(event);
    }

    protected void signalSetVariable(String variableName, String variableValue, int order, String statement) {
        this.signalChangeEvent(new DdlParserListener.SetVariableEvent(variableName, variableValue, this.currentSchema, order, statement));
    }

    protected void signalUseDatabase(String statement) {
        this.signalChangeEvent(new DdlParserListener.DatabaseSwitchedEvent(this.currentSchema, statement));
    }

    protected void signalCreateDatabase(String databaseName, String statement) {
        this.signalChangeEvent(new DdlParserListener.DatabaseCreatedEvent(databaseName, statement));
    }

    protected void signalAlterDatabase(String databaseName, String previousDatabaseName, String statement) {
        this.signalChangeEvent(new DdlParserListener.DatabaseAlteredEvent(databaseName, previousDatabaseName, statement));
    }

    protected void signalDropDatabase(String databaseName, String statement) {
        this.signalChangeEvent(new DdlParserListener.DatabaseCreatedEvent(databaseName, statement));
    }

    protected void signalCreateTable(TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableCreatedEvent(id, statement, false));
    }

    protected void signalAlterTable(TableId id, TableId previousId, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableAlteredEvent(id, previousId, statement, false));
    }

    protected void signalDropTable(TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableDroppedEvent(id, statement, false));
    }

    protected void signalTruncateTable(TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableTruncatedEvent(id, statement, false));
    }

    protected void signalCreateView(TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableCreatedEvent(id, statement, true));
    }

    protected void signalAlterView(TableId id, TableId previousId, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableAlteredEvent(id, previousId, statement, true));
    }

    protected void signalDropView(TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableDroppedEvent(id, statement, true));
    }

    protected void signalCreateIndex(String indexName, TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableIndexCreatedEvent(indexName, id, statement));
    }

    protected void signalDropIndex(String indexName, TableId id, String statement) {
        this.signalChangeEvent(new DdlParserListener.TableIndexDroppedEvent(indexName, id, statement));
    }

    protected String removeLineFeeds(String input) {
        return input.replaceAll("[\\n|\\t]", "");
    }

    public static String withoutQuotes(String possiblyQuoted) {
        return isQuoted(possiblyQuoted) ? possiblyQuoted.substring(1, possiblyQuoted.length() - 1) : possiblyQuoted;
    }

    public static boolean isQuoted(String possiblyQuoted) {
        if (possiblyQuoted.length() < 2) {
            return false;
        } else if (possiblyQuoted.startsWith("`") && possiblyQuoted.endsWith("`")) {
            return true;
        } else if (possiblyQuoted.startsWith("'") && possiblyQuoted.endsWith("'")) {
            return true;
        } else {
            return possiblyQuoted.startsWith("\"") && possiblyQuoted.endsWith("\"");
        }
    }

    public static boolean isQuote(char c) {
        return c == '\'' || c == '"' || c == '`';
    }

    public static Collection<ParsingException> accumulateParsingFailure(ParsingException e, Collection<ParsingException> list) {
        if (e == null) {
            return (Collection) list;
        } else {
            if (list == null) {
                list = new ArrayList();
            }

            ((Collection) list).add(e);
            return (Collection) list;
        }
    }

    protected Collection<ParsingException> accumulateParsingFailure(MultipleParsingExceptions e, Collection<ParsingException> list) {
        if (e == null) {
            return (Collection) list;
        } else {
            if (list == null) {
                list = new ArrayList();
            }

            ((Collection) list).addAll(e.getErrors());
            return (Collection) list;
        }
    }

    protected Column createColumnFromConstant(String columnName, String constantValue) {
        ColumnEditor column = Column.editor().name(columnName);

        try {
            if (!constantValue.startsWith("'") && !constantValue.startsWith("\"")) {
                if (!constantValue.equalsIgnoreCase("TRUE") && !constantValue.equalsIgnoreCase("FALSE")) {
                    this.setTypeInfoForConstant(constantValue, column);
                } else {
                    column.type("BOOLEAN");
                    column.jdbcType(16);
                }
            } else {
                column.type("CHAR");
                column.jdbcType(1);
                column.length(constantValue.length() - 2);
            }
        } catch (Throwable var5) {
            this.logger.debug("Unable to create an artificial column for the constant: {}", constantValue);
        }

        return column.create();
    }

    protected void setTypeInfoForConstant(String constantValue, ColumnEditor column) {
        try {
            Integer.parseInt(constantValue);
            column.type("INTEGER");
            column.jdbcType(4);
        } catch (NumberFormatException var11) {
        }

        try {
            Long.parseLong(constantValue);
            column.type("BIGINT");
            column.jdbcType(-5);
        } catch (NumberFormatException var10) {
        }

        try {
            Float.parseFloat(constantValue);
            column.type("FLOAT");
            column.jdbcType(6);
        } catch (NumberFormatException var9) {
        }

        try {
            Double.parseDouble(constantValue);
            column.type("DOUBLE");
            column.jdbcType(8);
            int precision = 0;
            int scale = 0;
            boolean foundDecimalPoint = false;

            for (int i = 0; i < constantValue.length(); ++i) {
                char c = constantValue.charAt(i);
                if (c != '+' && c != '-') {
                    if (c == '.') {
                        foundDecimalPoint = true;
                    } else {
                        if (!Character.isDigit(c)) {
                            break;
                        }

                        if (foundDecimalPoint) {
                            ++scale;
                        } else {
                            ++precision;
                        }
                    }
                }
            }

            column.length(precision);
            column.scale(scale);
        } catch (NumberFormatException var12) {
        }

        try {
            BigDecimal decimal = new BigDecimal(constantValue);
            column.type("DECIMAL");
            column.jdbcType(3);
            column.length(decimal.precision());
            column.scale(decimal.precision());
        } catch (NumberFormatException var8) {
        }

    }

    protected void debugParsed(String statement) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("PARSED:  {}", statement);
        }

    }

    protected void debugSkipped(String statement) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("SKIPPED: {}", statement);
        }

    }

    protected void commentParsed(String comment) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("COMMENT: {}", comment);
        }

    }
}
