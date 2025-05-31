package org.example.entity.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a calendar event.
 */
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendarEvent {
    private String kind;
    private String etag;
    private String id;
    private String status;
    private String htmlLink;
    private String created;
    private String updated;
    // more important field
    private String summary;
    private String description;
    private String location;
    private Creator creator;
    private Organizer organizer;
    private EventDateTime start;
    private EventDateTime end;
    private String iCalUID;
    private int sequence;
    private List<Attendee> attendees;
    private Reminders reminders;
    private String eventType;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CalendarEvent:\n");
        sb.append("id=").append(id).append('\n');
        sb.append("status=").append(status).append('\n');
        sb.append("summary=").append(summary).append('\n');
        sb.append("description=").append(description).append('\n');
        sb.append("location=").append(location).append('\n');
        return sb.toString();
    }


    // Nested classes
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Creator {
        private String email;
        private boolean self;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Organizer {
        private String email;
        private boolean self;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class EventDateTime {
        private String dateTime;
        private String timeZone;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class Attendee {
        private String email;
        private boolean organizer;
        private boolean self;
        private String responseStatus;
    }
    @NoArgsConstructor
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reminders {
        private boolean useDefault;
    }
}
