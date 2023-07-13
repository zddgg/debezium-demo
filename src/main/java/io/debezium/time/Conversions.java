package io.debezium.time;

import java.time.*;
import java.util.concurrent.TimeUnit;

public final class Conversions {
    static final long MILLISECONDS_PER_SECOND;
    static final long MICROSECONDS_PER_SECOND;
    static final long MICROSECONDS_PER_MILLISECOND;
    static final long NANOSECONDS_PER_MILLISECOND;
    static final long NANOSECONDS_PER_MICROSECOND;
    static final long NANOSECONDS_PER_SECOND;
    static final long NANOSECONDS_PER_DAY;
    static final long SECONDS_PER_DAY;
    static final long MICROSECONDS_PER_DAY;
    static final LocalDate EPOCH;

    private Conversions() {
    }

    protected static LocalDate toLocalDate(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof LocalDate) {
            return (LocalDate) obj;
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalDate();
        } else if (obj instanceof java.sql.Date) {
            return ((java.sql.Date) obj).toLocalDate();
        } else if (obj instanceof java.sql.Time) {
            throw new IllegalArgumentException("Unable to convert to LocalDate from a java.sql.Time value '" + obj + "'");
        } else if (obj instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) obj;
            return LocalDate.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate());
        } else if (obj instanceof Long) {
            return LocalDate.ofEpochDay((Long) obj);
        } else if (obj instanceof Integer) {
            return LocalDate.ofEpochDay((long) (Integer) obj);
        } else {
            throw new IllegalArgumentException("Unable to convert to LocalDate from unexpected value '" + obj + "' of type " + obj.getClass().getName());
        }
    }

    protected static LocalTime toLocalTime(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof LocalTime) {
            return (LocalTime) obj;
        } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalTime();
        } else if (obj instanceof java.sql.Date) {
            throw new IllegalArgumentException("Unable to convert to LocalDate from a java.sql.Date value '" + obj + "'");
        } else {
            long millis;
            int nanosOfSecond;
            if (obj instanceof java.sql.Time) {
                java.sql.Time time = (java.sql.Time) obj;
                millis = (long) ((int) (time.getTime() % MILLISECONDS_PER_SECOND));
                nanosOfSecond = (int) (millis * NANOSECONDS_PER_MILLISECOND);
                return LocalTime.of(time.getHours(), time.getMinutes(), time.getSeconds(), nanosOfSecond);
            } else if (obj instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) obj;
                return LocalTime.of(timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getNanos());
            } else if (obj instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) obj;
                millis = (long) ((int) (date.getTime() % MILLISECONDS_PER_SECOND));
                nanosOfSecond = (int) (millis * NANOSECONDS_PER_MILLISECOND);
                return LocalTime.of(date.getHours(), date.getMinutes(), date.getSeconds(), nanosOfSecond);
            } else if (obj instanceof Duration) {
                Long value = ((Duration) obj).toNanos();
                if (value >= 0L && value <= NANOSECONDS_PER_DAY) {
                    return LocalTime.ofNanoOfDay(value);
                } else {
                    throw new IllegalArgumentException("Time values must use number of milliseconds greater than 0 and less than 86400000000000");
                }
            } else {
                throw new IllegalArgumentException("Unable to convert to LocalTime from unexpected value '" + obj + "' of type " + obj.getClass().getName());
            }
        }
    }

    protected static LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof OffsetDateTime) {
            return ((OffsetDateTime) obj).toLocalDateTime();
        } else if (obj instanceof Instant) {
            return ((Instant) obj).atOffset(ZoneOffset.UTC).toLocalDateTime();
        } else if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        } else if (obj instanceof LocalDate) {
            LocalDate date = (LocalDate) obj;
            return LocalDateTime.of(date, LocalTime.MIDNIGHT);
        } else {
            LocalTime localTime;
            if (obj instanceof LocalTime) {
                localTime = (LocalTime) obj;
                return LocalDateTime.of(EPOCH, localTime);
            } else if (obj instanceof java.sql.Date) {
                java.sql.Date sqlDate = (java.sql.Date) obj;
                LocalDate date = sqlDate.toLocalDate();
                return LocalDateTime.of(date, LocalTime.MIDNIGHT);
            } else if (obj instanceof java.sql.Time) {
                localTime = toLocalTime(obj);
                return LocalDateTime.of(EPOCH, localTime);
            } else if (obj instanceof java.sql.Timestamp) {
                java.sql.Timestamp timestamp = (java.sql.Timestamp) obj;
                return LocalDateTime.of(timestamp.getYear() + 1900, timestamp.getMonth() + 1, timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(), timestamp.getSeconds(), timestamp.getNanos());
            } else if (obj instanceof java.util.Date) {
                java.util.Date date = (java.util.Date) obj;
                long millis = (long) ((int) (date.getTime() % MILLISECONDS_PER_SECOND));
                if (millis < 0L) {
                    millis += MILLISECONDS_PER_SECOND;
                }

                int nanosOfSecond = (int) (millis * NANOSECONDS_PER_MILLISECOND);
                return LocalDateTime.of(date.getYear() + 1900, date.getMonth() + 1, date.getDate(), date.getHours(), date.getMinutes(), date.getSeconds(), nanosOfSecond);
            } else {
                throw new IllegalArgumentException("Unable to convert to LocalTime from unexpected value '" + obj + "' of type " + obj.getClass().getName());
            }
        }
    }

    public static long toEpochMicros(Instant instant) {
        return TimeUnit.SECONDS.toMicros(instant.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros((long) instant.getNano());
    }

    public static Instant toInstantFromMicros(long microsSinceEpoch) {
        return Instant.ofEpochSecond(TimeUnit.MICROSECONDS.toSeconds(microsSinceEpoch), TimeUnit.MICROSECONDS.toNanos(microsSinceEpoch % TimeUnit.SECONDS.toMicros(1L)));
    }

    public static Instant toInstantFromMillis(long millisecondSinceEpoch) {
        return Instant.ofEpochSecond(TimeUnit.MILLISECONDS.toSeconds(millisecondSinceEpoch), TimeUnit.MILLISECONDS.toNanos(millisecondSinceEpoch % TimeUnit.SECONDS.toMillis(1L)));
    }

    static {
        MILLISECONDS_PER_SECOND = TimeUnit.SECONDS.toMillis(1L);
        MICROSECONDS_PER_SECOND = TimeUnit.SECONDS.toMicros(1L);
        MICROSECONDS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toMicros(1L);
        NANOSECONDS_PER_MILLISECOND = TimeUnit.MILLISECONDS.toNanos(1L);
        NANOSECONDS_PER_MICROSECOND = TimeUnit.MICROSECONDS.toNanos(1L);
        NANOSECONDS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L);
        NANOSECONDS_PER_DAY = TimeUnit.DAYS.toNanos(1L);
        SECONDS_PER_DAY = TimeUnit.DAYS.toSeconds(1L);
        MICROSECONDS_PER_DAY = TimeUnit.DAYS.toMicros(1L);
        EPOCH = LocalDate.ofEpochDay(0L);
    }
}
