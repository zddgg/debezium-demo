package io.debezium.time;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjuster;
import java.util.Locale;

public class ZonedTimestamp {
    public static final DateTimeFormatter FORMATTER;
    public static final String SCHEMA_NAME = "io.debezium.time.ZonedTimestamp";

    private static DateTimeFormatter getDateTimeFormatter(Integer fractionalWidth) {
        if (fractionalWidth != null && fractionalWidth > 0 && fractionalWidth <= 9) {
            DateTimeFormatter timeFormatter = (new DateTimeFormatterBuilder()).appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2).optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, fractionalWidth, fractionalWidth, true).toFormatter(Locale.ENGLISH);
            DateTimeFormatter dateTimeFormatter = (new DateTimeFormatterBuilder()).parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral('T').append(timeFormatter).toFormatter(Locale.ENGLISH);
            return (new DateTimeFormatterBuilder()).parseCaseInsensitive().append(dateTimeFormatter).parseLenient().appendOffsetId().parseStrict().toFormatter(Locale.ENGLISH);
        } else {
            return FORMATTER;
        }
    }

    public static SchemaBuilder builder() {
        return SchemaBuilder.string().name("io.debezium.time.ZonedTimestamp").version(1);
    }

    public static Schema schema() {
        return builder().build();
    }

    public static String toIsoString(Object value, ZoneId defaultZone, TemporalAdjuster adjuster, Integer fractionalWidth) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof OffsetDateTime) {
            return toIsoString((OffsetDateTime) value, adjuster);
        } else if (value instanceof ZonedDateTime) {
            return toIsoString((ZonedDateTime) value, adjuster, fractionalWidth);
        } else if (value instanceof OffsetTime) {
            return toIsoString((OffsetTime) value, adjuster);
        } else if (value instanceof java.util.Date) {
            return toIsoString((java.util.Date) value, defaultZone, adjuster);
        } else {
            throw new IllegalArgumentException("Unable to convert to OffsetDateTime from unexpected value '" + value + "' of type " + value.getClass().getName());
        }
    }

    public static String toIsoString(OffsetDateTime timestamp, TemporalAdjuster adjuster) {
        if (adjuster != null) {
            timestamp = timestamp.with(adjuster);
        }

        return timestamp.format(FORMATTER);
    }

    public static String toIsoString(ZonedDateTime timestamp, TemporalAdjuster adjuster, Integer fractionalWidth) {
        if (adjuster != null) {
            timestamp = timestamp.with(adjuster);
        }

        return timestamp.format(getDateTimeFormatter(fractionalWidth));
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
        Instant instant = timestamp.toInstant();
        if (adjuster != null) {
            instant = instant.with(adjuster);
        }

        ZonedDateTime zdt = instant.atZone(zoneId);
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

    private ZonedTimestamp() {
    }

    static {
        FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    }
}
