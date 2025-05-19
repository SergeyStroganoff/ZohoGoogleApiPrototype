package org.example;

import org.example.entity.CalendarEvent;
import org.example.service.GoogleCalendarService;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.example.utils.CredentialsRetriever;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        GoogleTokenManager tokenManager = new GoogleTokenManager(credentialsReader);
        String token = tokenManager.getNewAccessToken();
        // Теперь ты можешь использовать этот токен в своих HTTP запросах
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(token);
        // Получаем список всех событий
        //List<CalendarEvent> events = googleCalendarService.getAllEvents();
        // Получаем события за семь дней начиная с сегодняшнего дня
        String startDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String endDate = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        //String startDate = "2023-10-01T00:00:00Z"; // Пример даты
        //String endDate = "2023-10-02T00:00:00Z"; // Пример даты
        List<CalendarEvent> events = googleCalendarService.getEventsByDate(startDate, endDate);
        // Выводим события
        for (CalendarEvent event : events) {
            System.out.println("Event ID: " + event.getId());
            System.out.println("Event Summary: " + event.getSummary());
            System.out.println("Event Start: " + event.getStart());
            System.out.println("Event End: " + event.getEnd());
            System.out.println("------------------------------");
        }
    }
}
