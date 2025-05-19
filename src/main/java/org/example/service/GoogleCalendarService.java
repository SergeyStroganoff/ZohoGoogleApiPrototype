package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.entity.CalendarEvent;
import org.example.entity.GoogleCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@AllArgsConstructor
public class GoogleCalendarService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    private final String accessToken;
    private static final String CALENDAR_API_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    //todo: добавить метод для получения списка календарей
    public List<GoogleCalendar> getCalendarList() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://example.com"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, GoogleCalendar.class));
    }

    public List<CalendarEvent> getEvents() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CALENDAR_API_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        //todo: добавить обработку ошибок убрать вывод в консоль
        if (response.statusCode() == 200) {
            logger.info("Successfully fetched calendar events.");
            logger.debug("Response body: {}", response.body());
        } else {
            System.err.println("Failed to fetch calendar events. Status: " + response.statusCode());
            System.err.println("Response body: " + response.body());
        }
        return parseEvents(response.body());
    }

    public List<CalendarEvent> parseEvents(String json) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        JsonNode itemsNode = rootNode.get("items");
        // Конвертируем JSON массив в список Java объектов
        try {
            return objectMapper.readerForListOf(CalendarEvent.class).readValue(itemsNode);
        } catch (IOException e) {
            logger.error("Error converting JSON to CalendarEvent list: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
