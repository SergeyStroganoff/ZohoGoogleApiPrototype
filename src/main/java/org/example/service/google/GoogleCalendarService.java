package org.example.service.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.EndPoint;
import org.example.entity.google.CalendarEvent;
import org.example.entity.google.GoogleCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class GoogleCalendarService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    // TODO: 07.07.2025  
    public static final String PRIMARY_EVENTS_END_POINT = "calendars/primary/events";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String TIME_PARAMS_INVALID_ERROR = "Time parameters cannot be null or empty during fetching calendar events. Please provide valid UTC date strings in RFC3339 format. Example: 2023-10-01T00:00:00Z";
    public static final String ITEMS = "items";
    private final String accessToken;
    private static final String CALENDAR_API_URL = EndPoint.GOOGLE_CALENDAR.getUrl();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GoogleCalendarService(String accessToken, HttpClient httpClient, ObjectMapper objectMapper) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<GoogleCalendar> getCalendarList() {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public List<CalendarEvent> getAllEvents() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()

                .uri(URI.create(CALENDAR_API_URL + PRIMARY_EVENTS_END_POINT))
                .header(AUTHORIZATION, BEARER + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            logger.info("Successfully fetched calendar events.");
            logger.debug("Response body: {}", response.body());
        } else {
            logger.error("Failed to fetch calendar events. Status: {}", response.statusCode());
        }
        return parseEvents(response.body());
    }

    /**
     * Fetches calendar events for a specific date range.
     *
     * @param UTCTimeMin date The date in RFC3339 format (e.g., "2023-10-01T00:00:00Z").
     * @param UTCTimeMax date The date in RFC3339 format (e.g., "2023-10-01T00:00:00Z").
     * @return A list of calendar CalendarEvent for the specified date.
     * @throws IOException
     * @throws InterruptedException
     */

    public List<CalendarEvent> getEventsByDate(String UTCTimeMin, String UTCTimeMax) throws IOException, InterruptedException {
        // Validate input parameters
        if (UTCTimeMin == null || UTCTimeMax == null || UTCTimeMin.isEmpty() || UTCTimeMax.isEmpty()) {
            throw new IllegalArgumentException(TIME_PARAMS_INVALID_ERROR);
        }
        StringBuilder urlBuilder = new StringBuilder(CALENDAR_API_URL + PRIMARY_EVENTS_END_POINT);
        urlBuilder.append("?timeMin=").append(UTCTimeMin)
                .append("&timeMax=").append(UTCTimeMax)
                .append("&singleEvents=true")
                .append("&orderBy=startTime");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header(AUTHORIZATION, BEARER + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            logger.info("Successfully fetched calendar events for date range: from: {}, to: {}", UTCTimeMin, UTCTimeMax);
            logger.debug("Response body: {}", response.body());
        } else {
            //todo handle error properly
            logger.error("Failed to fetch calendar events for date range: from: {}, to: {}. Response: {}", UTCTimeMin, UTCTimeMax, response.statusCode());
        }
        return parseEvents(response.body());
    }

    public List<CalendarEvent> parseEvents(String json) throws IOException {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        JsonNode itemsNode = rootNode.get(ITEMS);
        return objectMapper.readerForListOf(CalendarEvent.class).readValue(itemsNode);
    }
}
