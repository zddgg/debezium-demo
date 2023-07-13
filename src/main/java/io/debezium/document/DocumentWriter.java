package io.debezium.document;

import io.debezium.annotation.ThreadSafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

@ThreadSafe
public interface DocumentWriter {
    static DocumentWriter defaultWriter() {
        return JacksonWriter.INSTANCE;
    }

    static DocumentWriter prettyWriter() {
        return JacksonWriter.PRETTY_WRITER;
    }

    default byte[] writeAsBytes(Document document) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            byte[] var3;
            try {
                this.write(document, (OutputStream) stream);
                var3 = stream.toByteArray();
            } catch (Throwable var6) {
                try {
                    stream.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            stream.close();
            return var3;
        } catch (IOException var7) {
            var7.printStackTrace();
            return new byte[0];
        }
    }

    void write(Document var1, OutputStream var2) throws IOException;

    void write(Document var1, Writer var2) throws IOException;

    String write(Document var1) throws IOException;
}
