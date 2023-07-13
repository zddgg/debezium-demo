package io.debezium.time;

import java.time.Duration;

public class Temporals {
    public static Duration max(Duration d1, Duration d2) {
        return d1.compareTo(d2) == 1 ? d1 : d2;
    }

    public static Duration min(Duration d1, Duration d2) {
        return d1.compareTo(d2) == 1 ? d2 : d1;
    }
}
