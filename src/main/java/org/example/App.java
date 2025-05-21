package org.example;

import org.example.entity.google.CalendarEvent;
import org.example.service.GoogleCalendarService;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.example.utils.UTCTimeConverter;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.List;

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        GoogleTokenManager tokenManager = new GoogleTokenManager(credentialsReader);
        String token = tokenManager.getNewAccessToken();
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(token);
        // Получаем список всех событий
        //List<CalendarEvent> events = googleCalendarService.getAllEvents();
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
