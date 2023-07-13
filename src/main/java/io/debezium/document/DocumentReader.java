package io.debezium.document;

import io.debezium.annotation.ThreadSafe;

import java.io.*;
import java.net.URL;

@ThreadSafe
public interface DocumentReader {
    static DocumentReader defaultReader() {
        return JacksonReader.DEFAULT_INSTANCE;
    }

    static DocumentReader floatNumbersAsTextReader() {
        return JacksonReader.FLOAT_NUMBERS_AS_TEXT_INSTANCE;
    }

    Document read(InputStream var1) throws IOException;

    Document read(Reader var1) throws IOException;

    Document read(String var1) throws IOException;

    default Document read(URL jsonUrl) throws IOException {
        return this.read(jsonUrl.openStream());
    }

    default Document read(File jsonFile) throws IOException {
        return this.read((InputStream) (new BufferedInputStream(new FileInputStream(jsonFile))));
    }

    default Document read(byte[] rawBytes) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(rawBytes);

        Document var3;
        try {
            var3 = defaultReader().read((InputStream) stream);
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
