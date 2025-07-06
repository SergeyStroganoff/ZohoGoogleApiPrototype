package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.google.CalendarEvent;
import org.example.service.OAuthTokenRefresher;
import org.example.service.google.GoogleCalendarService;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.example.utils.JsonUtils;
import org.example.utils.UTCTimeConverter;
import org.slf4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

//todo: распарсить евент извлечь customer
// сходить в базу данных и проверить обработан ли event
// пойти в ZOHO и проверить есть ли такой customer
// если нет, то создать его
// обратиться в гуг map и посчитать расстояние от работы до клиента и обратно
// создать запись пробега в базе данных или Exell

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        OAuthTokenRefresher oAuthTokenRefresher = new OAuthTokenRefresher(httpClient, JsonUtils.OBJECT_MAPPER);
        TokenManager tokenManager = new TokenManager(credentialsReader, oAuthTokenRefresher);
        String token = tokenManager.getGoogleCalendarAccessToken();

        ObjectMapper objectMapper = JsonUtils.OBJECT_MAPPER;
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(token, httpClient, objectMapper);
        // Получаем список всех событий
        // List<CalendarEvent> events = googleCalendarService.getAllEvents();
        // Получаем события за семь дней начиная с сегодняшнего дня
        String startDate = UTCTimeConverter.getUTCDateTimeNow();
        logger.info("Get all events with Start date: {}", startDate);
        String endDate = UTCTimeConverter.getUTCDateTimeWithOffset(7, ChronoUnit.DAYS);
        List<CalendarEvent> events = googleCalendarService.getEventsByDate(startDate, endDate);
        // Выводим события
        for (CalendarEvent event : events) {
            System.out.println(event.toString());
            System.out.println("------------------------------");
        }
    }
}
