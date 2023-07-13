package io.debezium.document;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import io.debezium.annotation.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

@ThreadSafe
final class JacksonReader implements DocumentReader, ArrayReader {
    public static final JacksonReader DEFAULT_INSTANCE = new JacksonReader(false);
    public static final JacksonReader FLOAT_NUMBERS_AS_TEXT_INSTANCE = new JacksonReader(true);
    private static final JsonFactory factory = new JsonFactory();
    private final boolean handleFloatNumbersAsText;

    private JacksonReader(boolean handleFloatNumbersAsText) {
        this.handleFloatNumbersAsText = handleFloatNumbersAsText;
    }

    public Document read(InputStream jsonStream) throws IOException {
        return this.parse(factory.createParser(jsonStream));
    }

    public Document read(Reader jsonReader) throws IOException {
        return this.parse(factory.createParser(jsonReader));
    }

    public Document read(String json) throws IOException {
        return this.parse(factory.createParser(json));
    }

    public Document read(File jsonFile) throws IOException {
        return this.parse(factory.createParser(jsonFile));
    }

    public Document read(URL jsonUrl) throws IOException {
        return this.parse(factory.createParser(jsonUrl));
    }

    public Document read(byte[] rawBytes) throws IOException {
        return this.parse(factory.createParser(rawBytes));
    }

    public Array readArray(InputStream jsonStream) throws IOException {
        return this.parseArray(factory.createParser(jsonStream), false);
    }

    public Array readArray(Reader jsonReader) throws IOException {
        return this.parseArray(factory.createParser(jsonReader), false);
    }

    public Array readArray(URL jsonUrl) throws IOException {
        return this.parseArray(factory.createParser(jsonUrl), false);
    }

    public Array readArray(File jsonFile) throws IOException {
        return this.parseArray(factory.createParser(jsonFile), false);
    }

    public Array readArray(String jsonArray) throws IOException {
        return this.parseArray(factory.createParser(jsonArray), false);
    }

    private Document parse(JsonParser parser) throws IOException {
        Document var2;
        try {
            var2 = this.parseDocument(parser, false);
        } finally {
            parser.close();
        }

        return var2;
    }

    private Document parseDocument(JsonParser parser, boolean nested) throws IOException {
        BasicDocument doc = new BasicDocument();
        JsonToken token = null;
        if (!nested) {
            token = parser.nextToken();
            if (!nested && token != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object, but was " + token);
            }
        }

        String fieldName = null;

        for (token = parser.nextToken(); token != JsonToken.END_OBJECT; token = parser.nextToken()) {
            switch (token) {
                case FIELD_NAME:
                    fieldName = parser.getCurrentName();
                    break;
                case START_OBJECT:
                    doc.setDocument(fieldName, this.parseDocument(parser, true));
                    break;
                case START_ARRAY:
                    doc.setArray(fieldName, this.parseArray(parser, true));
                    break;
                case VALUE_STRING:
                    doc.setString(fieldName, parser.getValueAsString());
                    break;
                case VALUE_TRUE:
                    doc.setBoolean(fieldName, true);
                    break;
                case VALUE_FALSE:
                    doc.setBoolean(fieldName, false);
                    break;
                case VALUE_NULL:
                    doc.setNull(fieldName);
                    break;
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                    switch (parser.getNumberType()) {
                        case FLOAT:
                            if (this.handleFloatNumbersAsText) {
                                doc.setString(fieldName, parser.getText());
                            } else {
                                doc.setNumber(fieldName, parser.getFloatValue());
                            }
                            break;
                        case DOUBLE:
                            if (this.handleFloatNumbersAsText) {
                                doc.setString(fieldName, parser.getText());
                            } else {
                                doc.setNumber(fieldName, parser.getDoubleValue());
                            }
                            break;
                        case BIG_DECIMAL:
                            if (this.handleFloatNumbersAsText) {
                                doc.setString(fieldName, parser.getText());
                            } else {
                                doc.setNumber(fieldName, parser.getDecimalValue());
                            }
                            break;
                        case INT:
                            doc.setNumber(fieldName, parser.getIntValue());
                            break;
                        case LONG:
                            doc.setNumber(fieldName, parser.getLongValue());
                            break;
                        case BIG_INTEGER:
                            doc.setNumber(fieldName, parser.getBigIntegerValue());
                    }
                case VALUE_EMBEDDED_OBJECT:
                default:
                    break;
                case NOT_AVAILABLE:
                    throw new JsonParseException(parser, "Non-blocking parsers are not supported", parser.getCurrentLocation());
                case END_ARRAY:
                    throw new JsonParseException(parser, "Not expecting an END_ARRAY token", parser.getCurrentLocation());
                case END_OBJECT:
                    throw new JsonParseException(parser, "Not expecting an END_OBJECT token", parser.getCurrentLocation());
            }
        }

        return doc;
    }

    private Array parseArray(JsonParser parser, boolean nested) throws IOException {
        BasicArray array = new BasicArray();
        JsonToken token = null;
        if (!nested) {
            token = parser.nextToken();
            if (!nested && token != JsonToken.START_ARRAY) {
                throw new IOException("Expected data to start with an Array, but was " + token);
            }
        }

        for (token = parser.nextToken(); token != JsonToken.END_ARRAY; token = parser.nextToken()) {
            switch (token) {
                case FIELD_NAME:
                    throw new JsonParseException(parser, "Not expecting a FIELD_NAME token", parser.getCurrentLocation());
                case START_OBJECT:
                    array.add(this.parseDocument(parser, true));
                    break;
                case START_ARRAY:
                    array.add(this.parseArray(parser, true));
                    break;
                case VALUE_STRING:
                    array.add(parser.getValueAsString());
                    break;
                case VALUE_TRUE:
                    array.add(true);
                    break;
                case VALUE_FALSE:
                    array.add(false);
                    break;
                case VALUE_NULL:
                    array.addNull();
                    break;
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                    switch (parser.getNumberType()) {
                        case FLOAT:
                            if (this.handleFloatNumbersAsText) {
                                array.add(parser.getText());
                            } else {
                                array.add(parser.getFloatValue());
                            }
                            break;
                        case DOUBLE:
                            if (this.handleFloatNumbersAsText) {
                                array.add(parser.getText());
                            } else {
                                array.add(parser.getDoubleValue());
                            }
                            break;
                        case BIG_DECIMAL:
                            if (this.handleFloatNumbersAsText) {
                                array.add(parser.getText());
                            } else {
                                array.add(parser.getDecimalValue());
                            }
                            break;
                        case INT:
                            array.add(parser.getIntValue());
                            break;
                        case LONG:
                            array.add(parser.getLongValue());
                            break;
                        case BIG_INTEGER:
                            array.add(parser.getBigIntegerValue());
                    }
                case VALUE_EMBEDDED_OBJECT:
                default:
                    break;
                case NOT_AVAILABLE:
                    throw new JsonParseException(parser, "Non-blocking parsers are not supported", parser.getCurrentLocation());
                case END_ARRAY:
                    throw new JsonParseException(parser, "Not expecting an END_ARRAY token", parser.getCurrentLocation());
                case END_OBJECT:
                    throw new JsonParseException(parser, "Not expecting an END_OBJECT token", parser.getCurrentLocation());
            }
        }

        return array;
    }

    static {
        factory.enable(Feature.ALLOW_COMMENTS);
        factory.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
    }
}
