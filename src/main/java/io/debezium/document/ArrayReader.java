package io.debezium.document;

import io.debezium.annotation.ThreadSafe;

import java.io.*;
import java.net.URL;

@ThreadSafe
public interface ArrayReader {
    static ArrayReader defaultReader() {
        return JacksonReader.DEFAULT_INSTANCE;
    }

    Array readArray(InputStream var1) throws IOException;

    Array readArray(Reader var1) throws IOException;

    Array readArray(String var1) throws IOException;

    default Array readArray(URL jsonUrl) throws IOException {
        return this.readArray(jsonUrl.openStream());
    }

    default Array readArray(File jsonFile) throws IOException {
        return this.readArray((InputStream) (new BufferedInputStream(new FileInputStream(jsonFile))));
    }

    default Array readArray(byte[] rawBytes) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(rawBytes);

        Array var3;
        try {
            var3 = defaultReader().readArray((InputStream) stream);
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
    }
}
