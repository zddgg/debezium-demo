package io.debezium.document;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import io.debezium.annotation.ThreadSafe;

import java.io.*;

@ThreadSafe
final class JacksonWriter implements DocumentWriter, ArrayWriter {
    public static final JacksonWriter INSTANCE = new JacksonWriter(false);
    public static final JacksonWriter PRETTY_WRITER = new JacksonWriter(true);
    private static final JsonFactory factory = new JsonFactory();
    private final boolean pretty;

    private JacksonWriter(boolean pretty) {
        this.pretty = pretty;
    }

    public void write(Document document, OutputStream jsonStream) throws IOException {
        JsonGenerator jsonGenerator = factory.createGenerator(jsonStream);

        try {
            this.configure(jsonGenerator);
            this.writeDocument(document, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

    }

    public void write(Document document, Writer jsonWriter) throws IOException {
        JsonGenerator jsonGenerator = factory.createGenerator(jsonWriter);

        try {
            this.configure(jsonGenerator);
            this.writeDocument(document, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

    }

    public String write(Document document) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = factory.createGenerator(writer);

        try {
            this.configure(jsonGenerator);
            this.writeDocument(document, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

        return writer.getBuffer().toString();
    }

    public byte[] writeAsBytes(Document document) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            byte[] var11;
            try {
                JsonGenerator jsonGenerator = factory.createGenerator(stream, JsonEncoding.UTF8);

                try {
                    this.configure(jsonGenerator);
                    this.writeDocument(document, jsonGenerator);
                } catch (Throwable var8) {
                    if (jsonGenerator != null) {
                        try {
                            jsonGenerator.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (jsonGenerator != null) {
                    jsonGenerator.close();
                }

                var11 = stream.toByteArray();
            } catch (Throwable var9) {
                try {
                    stream.close();
                } catch (Throwable var6) {
                    var9.addSuppressed(var6);
                }

                throw var9;
            }

            stream.close();
            return var11;
        } catch (IOException var10) {
            throw new RuntimeException(var10);
        }
    }

    public void write(Array array, OutputStream jsonStream) throws IOException {
        JsonGenerator jsonGenerator = factory.createGenerator(jsonStream);

        try {
            this.configure(jsonGenerator);
            this.writeArray(array, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

    }

    public void write(Array array, Writer jsonWriter) throws IOException {
        JsonGenerator jsonGenerator = factory.createGenerator(jsonWriter);

        try {
            this.configure(jsonGenerator);
            this.writeArray(array, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

    }

    public String write(Array array) throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator jsonGenerator = factory.createGenerator(writer);

        try {
            this.configure(jsonGenerator);
            this.writeArray(array, jsonGenerator);
        } catch (Throwable var7) {
            if (jsonGenerator != null) {
                try {
                    jsonGenerator.close();
                } catch (Throwable var6) {
                    var7.addSuppressed(var6);
                }
            }

            throw var7;
        }

        if (jsonGenerator != null) {
            jsonGenerator.close();
        }

        return writer.getBuffer().toString();
    }

    protected void configure(JsonGenerator generator) {
        if (this.pretty) {
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
        }

    }

    protected void writeDocument(Document document, JsonGenerator generator) throws IOException {
        generator.writeStartObject();

        try {
            document.stream().forEach((field) -> {
                try {
                    generator.writeFieldName(field.getName().toString());
                    this.writeValue(field.getValue(), generator);
                } catch (IOException var4) {
                    throw new WritingError(var4);
                }
            });
            generator.writeEndObject();
        } catch (WritingError var4) {
            throw var4.wrapped();
        }
    }

    protected void writeArray(Array array, JsonGenerator generator) throws IOException {
        generator.writeStartArray();

        try {
            array.streamValues().forEach((value) -> {
                try {
                    this.writeValue(value, generator);
                } catch (IOException var4) {
                    throw new WritingError(var4);
                }
            });
            generator.writeEndArray();
        } catch (WritingError var4) {
            throw var4.wrapped();
        }
    }

    protected void writeValue(Value value, JsonGenerator generator) throws IOException {
        switch (value.getType()) {
            case NULL:
                generator.writeNull();
                break;
            case STRING:
                generator.writeString(value.asString());
                break;
            case BOOLEAN:
                generator.writeBoolean(value.asBoolean());
                break;
            case BINARY:
                generator.writeBinary(value.asBytes());
                break;
            case INTEGER:
                generator.writeNumber(value.asInteger());
                break;
            case LONG:
                generator.writeNumber(value.asLong());
                break;
            case FLOAT:
                generator.writeNumber(value.asFloat());
                break;
            case DOUBLE:
                generator.writeNumber(value.asDouble());
                break;
            case BIG_INTEGER:
                generator.writeNumber(value.asBigInteger());
                break;
            case DECIMAL:
                generator.writeNumber(value.asBigDecimal());
                break;
            case DOCUMENT:
                this.writeDocument(value.asDocument(), generator);
                break;
            case ARRAY:
                this.writeArray(value.asArray(), generator);
        }

    }

    protected static final class WritingError extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final IOException wrapped;

        protected WritingError(IOException wrapped) {
            this.wrapped = wrapped;
        }

        public IOException wrapped() {
            return this.wrapped;
        }
    }
}
