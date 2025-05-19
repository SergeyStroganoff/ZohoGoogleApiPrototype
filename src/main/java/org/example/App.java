package org.example;

import org.example.entity.CalendarEvent;
import org.example.service.GoogleCalendarService;
import org.example.utils.CredentialsRetriever;
import org.example.utils.CredentialsFileRetrieverImpl;
import org.slf4j.Logger;

import java.util.List;

public class App {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(App.class);
    public static void main(String[] args) throws Exception {
        CredentialsRetriever credentialsReader = new CredentialsFileRetrieverImpl();
        GoogleTokenManager tokenManager = new GoogleTokenManager(credentialsReader);
        String token = tokenManager.getNewAccessToken();
        // Теперь ты можешь использовать этот токен в своих HTTP запросах
        GoogleCalendarService googleCalendarService = new GoogleCalendarService(token);
        // Получаем список событий
        List<CalendarEvent> events = googleCalendarService.getEvents();
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
