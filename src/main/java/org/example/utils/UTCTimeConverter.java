package org.example.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class UTCTimeConverter {

    private UTCTimeConverter() {
    }
    public static final String AMERICA_INDIANA_INDIANAPOLIS = "America/Indiana/Indianapolis";
    public static String getUTCDateTimeNow() {
        Instant nowUtc = Instant.now();
        return nowUtc.toString();
    }
    public static String getUTCDateTimeWithOffset(long offset, ChronoUnit unit) {
        Instant nowUtc = Instant.now();
        Instant offsetTime = nowUtc.plus(offset, unit);
        return offsetTime.toString();
    }

    /**
     * Converts a given date and time to UTC format.
     *
     * @param year     the year
     * @param month    the month
     * @param day      the day
     * @param hour     the hour
     * @param minute   the minute
     * @param second   the second
     * @param timeZone the time zone (e.g., "America/Indiana/Indianapolis")
     * @return the UTC date and time in ISO-8601 format
     */
    public static String getUTCDateTime(int year, int month, int day, int hour, int minute, int second, String timeZone) {
        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
        // Валидация параметров
        Objects.requireNonNull(timeZone, "Часовой пояс не может быть null");
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.of(timeZone));
        ZonedDateTime utcTime = zdt.withZoneSameInstant(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_INSTANT.format(utcTime);
    }

    /**
     * Converts a given date and time to UTC format using the default time zone:America/Indiana/Indianapolis.
     *
     * @param year   integer
     * @param month  integer
     * @param day    integer
     * @param hour   integer
     * @param minute integer
     * @param second integer
     * @return the UTC date and time in ISO-8601 format
     */

    public static String getUTCDateTime(int year, int month, int day, int hour, int minute, int second) {
        return getUTCDateTime(year, month, day, hour, minute, second, AMERICA_INDIANA_INDIANAPOLIS);
    }

    /**
     * Converts a Google API time string to LocalDateTime.
     *
     * @param googleApiTime the Google API time string (e.g., "2025-01-01T12:00:00Z")
     * @return the LocalDateTime object
     */

    public static LocalDateTime covertGoogleApiTimeToLocalDateTime(String googleApiTime) {
        // Example Google API time format: "2025-01-01T12:00:00Z"
        // Parsing the Google API time string to LocalDateTime
        // Note: The 'Z' at the end indicates UTC time
        // You can replace this with your actual Google API time string
        //  googleApiTime = "2025-01-01T12:00:00Z";
        return LocalDateTime.parse(googleApiTime, DateTimeFormatter.ISO_INSTANT);
    }
}

