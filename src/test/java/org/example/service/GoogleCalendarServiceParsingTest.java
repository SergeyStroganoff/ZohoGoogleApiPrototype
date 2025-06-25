package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.google.CalendarEvent;
import org.example.utils.JsonUtils;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoogleCalendarServiceParsingTest {
    HttpClient httpClient = HttpClient.newHttpClient();
    ObjectMapper objectMapper =  JsonUtils.OBJECT_MAPPER;

    String jsonGoogleCalendarEvent = """
            {"items": [{
                "kind": "calendar#event",
                "etag": "\\"3473770390520000\\"",
                "id": "frf34f34r343r34r343434300000",
                "status": "confirmed",
                "htmlLink": "https://www.google.com/calendar/event?eid=888888888888888888888888888888888888888888888888888888888888888888888888888",
                "created": "2025-01-14T18:22:27.000Z",
                "updated": "2025-01-14T20:06:35.260Z",
                "summary": "Alex Smith and Appliance Solutions",
                "description": "Event Name: Diagnostic visit\\n\\nLocation: 1534 Okley drive Chicago\\n\\nPlease share anything that will help prepare for our meeting.: Fridge\\n\\nNeed to make changes to this event?\\nCancel: https://calendly.com/cancellations/f42108d7-3242-4088-91f6-6194954ff66c\\nReschedule: https://calendly.com/reschedulings/f42108d7-3242-4088-91f6-6194954ff66c\\n\\nPowered by Calendly.com\\n",
                "location": "1534 Okley drive Chicago",
                "creator": {
                 "email": "manysolutions.llc@gmail.com",
                 "self": true
                },
                "organizer": {
                 "email": "manysolutions.llc@gmail.com",
                 "self": true
                },
                "start": {
                 "dateTime": "2025-01-14T17:45:00-05:00",
                 "timeZone": "America/Indiana/Indianapolis"
                },
                "end": {
                 "dateTime": "2025-01-14T18:15:00-05:00",
                 "timeZone": "America/Indiana/Indianapolis"
                },
                "iCalUID": "frf34f34r343r34r343434300000@google.com",
                "sequence": 0,
                "attendees": [
                 {
                  "email": "snowman999@gmail.com",
                  "responseStatus": "accepted"
                 },
                 {
                  "email": "manysolutions.llc@gmail.com",
                  "organizer": true,
                  "self": true,
                  "responseStatus": "accepted"
                 }
                ],
                "reminders": {
                 "useDefault": true
                },
                "eventType": "default"
             }]}
             """;

    @Test
    void parseEvents_ShouldReturnCorrectlyMappedEntity() {
        // Arrange
        String json = jsonGoogleCalendarEvent;
        GoogleCalendarService googleCalendarService = new GoogleCalendarService("access_token", httpClient, objectMapper);

        // Expected values
        String expectedId = "frf34f34r343r34r343434300000";
        String expectedSummary = "Alex Smith and Appliance Solutions";
        String expectedLocation = "1534 Okley drive Chicago";
        String expectedCreated = "2025-01-14T18:22:27.000Z";
        String expectedOrganizerEmail = "manysolutions.llc@gmail.com";
        int expectedAttendeesCount = 2;

        // Act
        List<CalendarEvent> events = googleCalendarService.parseEvents(json);

        // Assert
        assertNotNull(events, "Events list should not be null");
        assertEquals(1, events.size(), "Should parse exactly one event");

        CalendarEvent event = events.get(0);
        assertAll("Event properties",
                () -> assertEquals(expectedId, event.getId(), "Event ID should match"),
                () -> assertEquals(expectedSummary, event.getSummary(), "Summary should match"),
                () -> assertEquals(expectedLocation, event.getLocation(), "Location should match"),
                () -> assertEquals(expectedCreated, event.getCreated(), "Creation timestamp should match"),
                () -> assertEquals("confirmed", event.getStatus(), "Status should be confirmed"),
                () -> assertNotNull(event.getStart(), "Start time should not be null"),
                () -> assertNotNull(event.getEnd(), "End time should not be null")
        );

        // Test organizer
        assertNotNull(event.getOrganizer(), "Organizer should not be null");
        assertEquals(expectedOrganizerEmail, event.getOrganizer().getEmail(), "Organizer email should match");

        // Test attendees
        assertNotNull(event.getAttendees(), "Attendees list should not be null");
        assertEquals(expectedAttendeesCount, event.getAttendees().size(), "Should have correct number of attendees");

        // Test first attendee
        CalendarEvent.Attendee firstAttendee = event.getAttendees().get(0);
        assertEquals("snowman999@gmail.com", firstAttendee.getEmail(), "First attendee email should match");
        assertEquals("accepted", firstAttendee.getResponseStatus(), "First attendee status should be accepted");
    }

    @Test
    void parseEvents_ShouldHandleEmptyJson() {
        // Arrange
        String emptyJson = "{}";
        GoogleCalendarService googleCalendarService = new GoogleCalendarService("access_token", httpClient, objectMapper);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> googleCalendarService.parseEvents(emptyJson),
                "Should throw exception for empty JSON");
    }

    @Test
    void parseEvents_ShouldHandleMalformedJson() {
        // Arrange
        String malformedJson = "{invalid json}";
        GoogleCalendarService googleCalendarService = new GoogleCalendarService("access_token", httpClient, objectMapper);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> googleCalendarService.parseEvents(malformedJson),
                "Should throw exception for malformed JSON");
    }
}