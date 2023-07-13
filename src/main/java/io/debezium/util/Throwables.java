package io.debezium.util;

public class Throwables {
    public static Throwable getRootCause(Throwable throwable) {
        while (true) {
            Throwable cause = throwable.getCause();
            if (cause == null) {
                return throwable;
            }

            throwable = cause;
        }
    }
}
