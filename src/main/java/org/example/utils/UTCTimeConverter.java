package org.example.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UTCTimeConverter {

    public static final String AMERICA_INDIANA_INDIANAPOLIS = "America/Indiana/Indianapolis";

    public String getUTCDateTime(int year, int month, int day, int hour, int minute, int second, String timeZone) {
        LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
        if (timeZone == null) {
            timeZone = AMERICA_INDIANA_INDIANAPOLIS;
        }
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(timeZone));
        // String rfc3339 = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zdt);
        ZonedDateTime utcTime = zdt.withZoneSameInstant(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_INSTANT.format(utcTime);
    }

    public LocalDateTime covertGoogleApiTimeToLocalDateTime(String googleApiTime) {
        // Example Google API time format: "2025-01-01T12:00:00Z"
        // Parsing the Google API time string to LocalDateTime
        // Note: The 'Z' at the end indicates UTC time
        // You can replace this with your actual Google API time string
        // String googleApiTime = "2025-01-01T12:00:00Z";
        return LocalDateTime.parse(googleApiTime, DateTimeFormatter.ISO_INSTANT);
    }
}
