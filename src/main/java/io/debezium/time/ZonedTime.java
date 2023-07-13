package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;

public class ZonedTime {
    public static final DateTimeFormatter FORMATTER;
    public static final String SCHEMA_NAME = "io.debezium.time.ZonedTime";

    public static SchemaBuilder builder() {
        return SchemaBuilder.string().name("io.debezium.time.ZonedTime").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static String toIsoString(Object value, ZoneId defaultZone, TemporalAdjuster adjuster) {
        if (value instanceof OffsetTime) {
            return toIsoString((OffsetTime) value, adjuster);
        } else if (value instanceof OffsetDateTime) {
            return toIsoString((OffsetDateTime) value, adjuster);
        } else if (value instanceof java.util.Date) {
            return toIsoString((java.util.Date) value, defaultZone, adjuster);
        } else {
            throw new IllegalArgumentException("Unable to convert to OffsetTime from unexpected value '" + value + "' of type " + value.getClass().getName());
        }
    }

    public static String toIsoString(OffsetDateTime timestamp, TemporalAdjuster adjuster) {
        if (adjuster != null) {
            timestamp = timestamp.with(adjuster);
        }

        return timestamp.toOffsetTime().format(FORMATTER);
    }

    public static String toIsoString(OffsetTime timestamp, TemporalAdjuster adjuster) {
        if (adjuster != null) {
            timestamp = timestamp.with(adjuster);
        }

        return timestamp.format(FORMATTER);
    }

    public static String toIsoString(java.util.Date timestamp, ZoneId zoneId, TemporalAdjuster adjuster) {
        if (timestamp instanceof java.sql.Timestamp) {
            return toIsoString((java.sql.Timestamp) timestamp, zoneId, adjuster);
        } else if (timestamp instanceof java.sql.Date) {
            return toIsoString((java.sql.Date) timestamp, zoneId, adjuster);
        } else {
            return timestamp instanceof java.sql.Time ? toIsoString((java.sql.Time) timestamp, zoneId, adjuster) : timestamp.toInstant().atZone(zoneId).format(FORMATTER);
        }
    }

    public static String toIsoString(java.sql.Timestamp timestamp, ZoneId zoneId, TemporalAdjuster adjuster) {
        ZonedDateTime zdt = timestamp.toInstant().atZone(zoneId);
        if (adjuster != null) {
            zdt = zdt.with(adjuster);
        }

        return zdt.format(FORMATTER);
    }

    public static String toIsoString(java.sql.Date date, ZoneId zoneId, TemporalAdjuster adjuster) {
        LocalDate localDate = date.toLocalDate();
        if (adjuster != null) {
            localDate = localDate.with(adjuster);
        }

        ZonedDateTime zdt = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, zoneId);
        return zdt.format(FORMATTER);
    }

    public static String toIsoString(java.sql.Time time, ZoneId zoneId, TemporalAdjuster adjuster) {
        LocalTime localTime = time.toLocalTime();
        if (adjuster != null) {
            localTime = localTime.with(adjuster);
        }

        ZonedDateTime zdt = ZonedDateTime.of(Conversions.EPOCH, localTime, zoneId);
        return zdt.format(FORMATTER);
    }

    private ZonedTime() {
    }

    static {
        FORMATTER = DateTimeFormatter.ISO_OFFSET_TIME;
    }
}
