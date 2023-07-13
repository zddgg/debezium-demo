package io.debezium.document;

import io.debezium.annotation.ThreadSafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

@ThreadSafe
public interface ArrayWriter {
    static ArrayWriter defaultWriter() {
        return JacksonWriter.INSTANCE;
    }

    static ArrayWriter prettyWriter() {
        return JacksonWriter.PRETTY_WRITER;
    }

    default byte[] writeAsBytes(Array array) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            byte[] var3;
            try {
                this.write(array, (OutputStream) stream);
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

    void write(Array var1, OutputStream var2) throws IOException;

    void write(Array var1, Writer var2) throws IOException;

    String write(Array var1) throws IOException;
}
